import React, {useEffect, useRef, useState} from 'react';
import {
    EuiBadge,
    EuiBasicTable,
    EuiComboBox,
    EuiFieldSearch, EuiFlexGroup, EuiFlexItem,
    EuiFormRow,
    EuiLoadingChart,
    EuiSpacer,
    EuiText,
} from '@elastic/eui';
import '@elastic/eui/dist/eui_theme_light.css';
import ArtefactModal from './Modal';
import {any, number} from "prop-types";

function prettyMilliseconds(ms: number) {
    const seconds = Math.floor(ms / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);

    const remainingMilliseconds = ms % 1000;
    const remainingSeconds = seconds % 60;
    const remainingMinutes = minutes % 60;

    let result = "";

    if (hours) result += `${hours}h `;
    if (remainingMinutes) result += `${remainingMinutes}m `;
    if (remainingSeconds) result += `${remainingSeconds}s `;
    if (remainingMilliseconds && ms < 1000) result += `${remainingMilliseconds}ms`;

    return result.trim();
}

const ArtefactsTable = (props: {apiUrl: string}) => {
    const [items, setItems] = useState([]);
    const [responseConfig, setResponseConfig] = useState({
        databases: Array<any>,
        totalResponseTime: number
    });
    let totalResponseTime = 0
    const [loading, setLoading] = useState(true);
    const [sortField, setSortField] = useState('label');
    const [sortDirection, setSortDirection] = useState("asc" as "asc" | "desc");
    const [pageIndex, setPageIndex] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedSources, setSelectedSources] = useState([]);
    const [sourceOptions, setSourceOptions] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedArtefact, setSelectedArtefact] = useState(null);
    const isInitialMount = useRef(true);

    const fetchArtefacts = async (apiUrl: string) => {
        try {
            const response = await fetch(`${apiUrl}`);
            const response_json = await response.json();
            const data = response_json.collection
            // @ts-ignore
            const uniqueSourceNames = [...new Set(data.map(item => item.source_name))];

            setItems(data);
            setResponseConfig(response_json.responseConfig);
            totalResponseTime = response_json.totalResponseTime;

            // @ts-ignore
            setSourceOptions(uniqueSourceNames.map(sourceName => ({
                label: sourceName
            })))
        } catch (error) {
            console.error('Error fetching artefacts:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (isInitialMount.current) {
            fetchArtefacts(props.apiUrl);
            isInitialMount.current = false;
        }
    }, [props.apiUrl]);

    // Define the columns for the table
    const columns: any = [
        {

            name: 'Label',
            sortable: true,
            render: (item: any) => (
                <span style={{cursor: 'pointer'}} onClick={() => openModal(item)}>
                    <EuiText> {item.description} ({item.label.toString().toUpperCase()})</EuiText>
                </span>
            )
        },
        {
            name: 'Source',
            render: (item: any) => (
                <div>
                    <p>
                        <EuiBadge color="hollow">Backend Type: {item.backend_type}</EuiBadge>{' '}
                    </p>
                    <p>
                        <EuiBadge color="default">Source: {item.source_name} (<a href={item.source} target="_blank"
                                                                                 rel="noopener noreferrer">{item.source}</a>)</EuiBadge>{' '}
                    </p>
                </div>
            ),
        }
    ];

    const onSourceChange = (selectedOptions: any) => {
        setSelectedSources(selectedOptions.map((option: any) => option.label));
    };


    // Handle table sorting and pagination changes
    const onTableChange = ({page = {}, sort = {}}) => {
        // @ts-ignore
        const {index: newPageIndex, size: newPageSize} = page;
        // @ts-ignore
        const {field: newSortField, direction: newSortDirection} = sort;

        setPageIndex(newPageIndex);
        setPageSize(newPageSize);
        setSortField(newSortField);
        setSortDirection(newSortDirection);

        // Sort and paginate the items
        const sortedItems = sortItems(items, newSortField, newSortDirection);
        setItems(sortedItems);
    };

    // Sort items based on the selected field and direction
    const sortItems = (items: never[], field: string | number, direction: string) => {
        return [...items].sort((a, b) => {
            const aValue = a[field];
            const bValue = b[field];
            let result = 0;

            if (aValue < bValue) {
                result = -1;
            } else if (aValue > bValue) {
                result = 1;
            }

            return direction === 'asc' ? result : -result;
        });
    };

    const filteredItems = items.filter((item: any) => {
        const matchesLabelOrSourceName = item.label.toLowerCase().includes(searchQuery.toLowerCase()) ||
            item.description?.toString().toLowerCase().includes(searchQuery.toLowerCase());

        // @ts-ignore
        const matchesSource = selectedSources.length === 0 || selectedSources.includes(item.source_name?.toString().toLowerCase());

        return matchesLabelOrSourceName && matchesSource;
    });

    let groupedBySourceName = {}

    const openModal = (artefact: any) => {
        setSelectedArtefact(artefact);
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setSelectedArtefact(null);
    };

    groupedBySourceName = filteredItems.reduce((acc, item) => {
        // @ts-ignore
        let count = acc[item.source_name]?.count
        count = (count || 0) + 1;


        // @ts-ignore
        let time = responseConfig.databases.filter((x: any) => x.url.includes(item.source))[0]?.responseTime
        // @ts-ignore
        acc[item.source_name] = {count: count, time: time};

        return acc;
    }, {});


    // Pagination logic (only show items for the current page)
    const paginatedItems = filteredItems.slice(pageIndex * pageSize, (pageIndex + 1) * pageSize);

    // Custom spinner container style
    const spinnerContainerStyle: any = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        flexDirection: 'column',
        height: '200px', // Height of the container where the spinner will show
    };


    const pagination = {
        pageIndex: pageIndex,
        pageSize: pageSize,
        totalItemCount: filteredItems.length,
        pageSizeOptions: [5, 10, 20],
    };

    // @ts-ignore
    // @ts-ignore
    return (
        <>
            {loading ? (
                <div style={spinnerContainerStyle}>
                    <EuiLoadingChart size="xl"/>
                    <EuiText>Loading resources</EuiText>
                </div>
            ) : (
                <>
                    <EuiSpacer size={'l'}></EuiSpacer>
                    <EuiFormRow label="Gateway artefacts endpoint" fullWidth={true}>
                        <EuiFieldSearch
                            placeholder="Search this"
                            value={props.apiUrl}
                            isClearable={true}
                            disabled={true}
                            fullWidth={true}
                        />
                    </EuiFormRow>
                    <EuiSpacer size={'l'}></EuiSpacer>
                    <EuiFlexGroup alignItems="center" justifyContent={'center'}>
                        <EuiFlexItem grow={2}>
                            <EuiFieldSearch
                                placeholder="Filter by acronym and name"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                isClearable
                                fullWidth
                            />

                            <EuiSpacer></EuiSpacer>

                            <EuiComboBox
                                placeholder="Select one or more source names"
                                options={sourceOptions}
                                selectedOptions={selectedSources.map(source => ({label: source}))}
                                onChange={onSourceChange}
                                isClearable
                                fullWidth
                            />
                        </EuiFlexItem>
                        <div>
                            <EuiText>
                                <p>Number of
                                    Results: {filteredItems.length} ({prettyMilliseconds(totalResponseTime)})</p>
                            </EuiText>

                            <EuiText size={"s"}>
                                <ul>
                                    {Object.entries(groupedBySourceName).map(([sourceName, count]: [any, any]) => (
                                        <li key={sourceName}>
                                            {sourceName}: {count.count} {count.count > 1 ? 'results' : 'result'} ({prettyMilliseconds(count.time)})
                                        </li>
                                    ))}
                                </ul>
                            </EuiText>
                            <EuiText>
                                {}
                            </EuiText>

                        </div>
                    </EuiFlexGroup>

                    <EuiSpacer></EuiSpacer>

                    <EuiBasicTable
                        items={paginatedItems}
                        columns={columns}
                        loading={loading}
                        sorting={{sort: {field: sortField, direction: sortDirection}}}
                        onChange={onTableChange}
                        pagination={pagination}
                    />
                    {isModalOpen && (
                        <ArtefactModal artefact={selectedArtefact} onClose={closeModal}/>
                    )}
                </>
            )}


        </>
    );
};

export default ArtefactsTable;
