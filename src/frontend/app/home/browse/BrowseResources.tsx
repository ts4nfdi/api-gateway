import React, {useEffect, useRef, useState} from 'react';
import {Loading} from "@/components/Loading";
import {Input} from "@/components/ui/input";
import {BrowseCard} from "@/app/home/browse/BrowseCard";
import {PaginatedCardList} from "@/components/PaginatedCardList";
import DatabaseSelector from "@/components/DatabaseSelector";
import APIUrlInput from "@/components/APIUrlInput";
import {CollectionResponse, collectionRestClient} from "@/app/auth/lib/CollectionsRestClient";
import {CardContent, CardHeader} from "@/components/ui/card";
import CollectionSelector from "@/components/CollectionsSelector";

type Artefact = {
    label: string;
    description?: string;
    backend_type: string;
    source_name: string;
    source: string;
    short_form: string;
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
    const [searchQuery, setSearchQuery] = useState("");
    const isInitialMount = useRef(true);
    const [selectedSources, setSelectedSources] = useState<string[]>([]);
    const [selectedCollection, setSelectedCollection] = useState<string[]>([]);
    const [collections, setCollections] = useState<CollectionResponse[]>([]);
    const [url, setApiUrl] = useState(apiUrl);
    let [filteredItems, setFilteredItems] = useState<any[]>([]);


    const fetchArtefacts = async () => {
        try {
            const response = await fetch(apiUrl);
            const responseJson = await response.json();
            const data: Artefact[] = responseJson.collection;

            setItems(data);
            setResponseConfig(responseJson.responseConfig);
        } catch (error) {
            console.error("Error fetching artefacts:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        collectionRestClient.getAllCollections().then((x: any) => {
            setCollections(x.data);
        }).catch((e: any) => {
            setCollections([]);
        })
    }, []);

    useEffect(() => {
        if (isInitialMount.current) {
            fetchArtefacts();
            isInitialMount.current = false;
        }
    }, [apiUrl, fetchArtefacts]);

    const collectionTerminologies = (id: string) => {
        const collection = collections.find((x) => x.id === id);
        return collection ? collection.terminologies.map(x => x.toLowerCase()) : [];
    }

    useEffect(() => {
        let filtered = items.filter((item: Artefact) => {
            const matchesSearch =
                item.short_form?.toLowerCase().includes(searchQuery.toLowerCase()) ||
                item.label?.toString().toLowerCase().includes(searchQuery.toLowerCase());

            const matchesSource =
                selectedSources.length === 0 || selectedSources.includes(item.source_name);

            const matchesCollection =
                selectedCollection.length === 0 || collectionTerminologies(selectedCollection[0]).includes(item.short_form.toLowerCase());

            return matchesSearch && matchesSource && matchesCollection;
        });
        setFilteredItems(filtered);
        setApiUrl(`${apiUrl}&database=${selectedSources.join(',')}&collectionId=${selectedCollection.join(',')}`);
    }, [searchQuery, items, selectedSources, selectedCollection, apiUrl]);


    /*
        const groupedBySourceName = filteredItems.reduce<Record<string, { count: number; time?: number }>>((acc, item) => {
            const count = (acc[item.source_name]?.count || 0) + 1;
            const time = responseConfig.databases.find((x) => x.url.includes(item.source))?.responseTime;

            acc[item.source_name] = {count, time};

            return acc;
        }, {});*/


    return (
        <>
            <CardHeader>
                <div className="flex items-center space-x-2">
                    <h2 className='text-2xl font-semibold text-gray-900'>Browse resources</h2>
                    <APIUrlInput url={url} variant={'badge'}/>
                </div>
            </CardHeader>
            <CardContent>
                <div>
                    <div className="space-y-4">

                        <Input
                            placeholder="Search by label or description"
                            value={searchQuery}
                            onChange={(e: any) => setSearchQuery(e.target.value)}
                        />
                        <div className="flex items-center space-x-1">
                            <div className="w-1/2">
                                <DatabaseSelector onChange={setSelectedSources}/>

                            </div>
                            <div className="w-1/2">
                                <CollectionSelector onChange={setSelectedCollection}/>
                            </div>
                        </div>

                        <p>
                            Found {filteredItems.length} results
                        </p>

                        {/*

                <ul>
                    {Object.entries(groupedBySourceName).map(([sourceName, {count, time}]) => (
                        <li key={sourceName}>
                            {sourceName}: {count} {count > 1 ? "results" : "result"} ({prettyMilliseconds(time || 0)})
                        </li>
                    ))}
                </ul> */}
                    </div>

                    {loading && <Loading/>}
                    {!loading && filteredItems.length !== 0 && (
                        <PaginatedCardList
                            itemsPerPage={12}
                            items={filteredItems.map(x => {
                                return {
                                    artefact: x,
                                };
                            }).concat()}
                            CardComponent={(props: any) => (
                                <BrowseCard
                                    {...props}
                                    onTagClick={() => {
                                    }}/>
                            )}/>)}

                </div>
            </CardContent>
        </>
    );
};

export default ArtefactsTable;
