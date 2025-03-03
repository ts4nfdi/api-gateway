import React, {useEffect, useRef, useState} from 'react';
import {Loader} from "@/components/Loading";
import {Input} from "@/components/ui/input";
import ModalContainer from "@/lib/modal";
import MultipleSelector, {SelectorOption} from "@/components/MultipleSelector";
import {BrowseCard} from "@/app/home/browse/BrowseCard";
import {PaginatedCardList} from "@/components/PaginatedCardList";
import DatabaseSelector from "@/components/DatabaseSelector";

type Artefact = {
    label: string;
    description?: string;
    backend_type: string;
    source_name: string;
    source: string;
};

type ResponseConfig = {
    databases: Array<{ url: string; responseTime: number }>;
    totalResponseTime: number;
};

function prettyMilliseconds(ms: number): string {
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

type ArtefactsTableProps = {
    apiUrl: string;
};

const ArtefactsTable: React.FC<ArtefactsTableProps> = ({apiUrl}) => {
    const [items, setItems] = useState<Artefact[]>([]);
    const [responseConfig, setResponseConfig] = useState<ResponseConfig>({
        databases: [],
        totalResponseTime: 0,
    });
    const [loading, setLoading] = useState(true);
    const [sortField, setSortField] = useState<keyof Artefact>("label");
    const [sortDirection, setSortDirection] = useState<"asc" | "desc">("asc");
    const [pageIndex, setPageIndex] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedSources, setSelectedSources] = useState<string[]>([]);
    const [sourceOptions, setSourceOptions] = useState<SelectorOption[]>([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedObject, setSelectedObject] = useState<Artefact | null>(null);
    const isInitialMount = useRef(true);

    let [filteredItems, setFilteredItems] = useState<any[]>([]);

    const fetchArtefacts = async () => {
        try {
            const response = await fetch(apiUrl);
            const responseJson = await response.json();
            const data: Artefact[] = responseJson.collection;

            let uniqueSourceNames: any[] = data.map((item) => item.source_name);
            // @ts-ignore
            uniqueSourceNames = [...new Set(uniqueSourceNames)]
            uniqueSourceNames = uniqueSourceNames.map(x => {
                return {label: x, value: x}
            }).concat()

            setItems(data);
            setResponseConfig(responseJson.responseConfig);
            setSourceOptions(uniqueSourceNames);
        } catch (error) {
            console.error("Error fetching artefacts:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (isInitialMount.current) {
            fetchArtefacts();
            isInitialMount.current = false;
        }
    }, [apiUrl]);


    useEffect(() => {
        let filtered = items.filter((item) => {
            const matchesSearch =
                item.label.toLowerCase().includes(searchQuery.toLowerCase()) ||
                item.description?.toString().toLowerCase().includes(searchQuery.toLowerCase());

            const matchesSource =
                selectedSources.length === 0 || selectedSources.includes(item.source_name);

            return matchesSearch && matchesSource;
        });
        setFilteredItems(filtered);
    }, [searchQuery, items, selectedSources]);

    /*
        const groupedBySourceName = filteredItems.reduce<Record<string, { count: number; time?: number }>>((acc, item) => {
            const count = (acc[item.source_name]?.count || 0) + 1;
            const time = responseConfig.databases.find((x) => x.url.includes(item.source))?.responseTime;

            acc[item.source_name] = {count, time};

            return acc;
        }, {});*/

    const openModal = (item: Artefact) => {
        setSelectedObject(item);
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setSelectedObject(null);
        setIsModalOpen(false);
    };

    if (loading) {
        return <Loader/>;
    }

    return (
        <div>
            <div className="space-y-4">
                <Input
                    placeholder="Search by label or description"
                    value={searchQuery}
                    onChange={(e: any) => setSearchQuery(e.target.value)}
                />
                <div className="w-1/2">
                    <DatabaseSelector sourceOptions={sourceOptions} setSelectedSources={setSelectedSources}/>
                </div>
                {/*
                <p>
                    Results: {filteredItems.length} ({prettyMilliseconds(responseConfig.totalResponseTime)})
                </p>

                <ul>
                    {Object.entries(groupedBySourceName).map(([sourceName, {count, time}]) => (
                        <li key={sourceName}>
                            {sourceName}: {count} {count > 1 ? "results" : "result"} ({prettyMilliseconds(time || 0)})
                        </li>
                    ))}
                </ul> */}
            </div>

            <PaginatedCardList
                itemsPerPage={12}
                items={filteredItems.map(x => {
                    return {title: `${x.description} (${x.label})`, sourceUrl: x.source_url, tags: [x.source_name]};
                }).concat()}
                CardComponent={(props: any) => (
                    <BrowseCard
                        {...props}
                        onTagClick={() => {
                        }}/>
                )}/>

            <ModalContainer isOpen={isModalOpen} artefact={selectedObject}
                            onClose={closeModal}/>
        </div>
    );
};

export default ArtefactsTable;
