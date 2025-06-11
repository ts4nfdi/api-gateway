import React, {useEffect, useState} from 'react';
import {Loading} from "@/components/Loading";
import {Input} from "@/components/ui/input";
import {BrowseCard} from "@/app/home/browse/BrowseCard";
import {PaginatedCardList} from "@/components/PaginatedCardList";
import {CardContent, CardHeader} from "@/components/ui/card";
import {Artefact, useArtefacts} from "@/app/api/ArtefactsRestClient";
import {CollectionResponse, collectionRestClient} from "@/app/api/CollectionsRestClient";
import {APICallDebug} from "@/app/home/MainPage";


const ArtefactsPage = ({url, sources, collection, query}: {
    url: string;
    sources: string[];
    collection: CollectionResponse;
    query: string;
}) => {
    const {items, loading, responseConfig} = useArtefacts(sources, collection);
    let [filteredItems, setFilteredItems] = useState<any[]>([]);

    useEffect(() => {
        if (!items)
            return;

        let filtered = items.filter((item: Artefact) => {
            return item.short_form?.toLowerCase().includes(query.toLowerCase()) ||
                item.label?.toString().toLowerCase().includes(query.toLowerCase());
        });
        setFilteredItems(filtered);
    }, [items, query, sources, collection]);


    return <div>
        <APICallDebug apiUrl={url} selectedSources={sources}
                      selectedCollection={collection}
                      resultsCount={filteredItems.length}
                      responseTime={responseConfig?.totalResponseTime || -1} />

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
}
const ArtefactsTable = ({apiUrl, selectedSources, selectedCollection}: {
    apiUrl: string;
    selectedSources: string[];
    selectedCollection: CollectionResponse;
}) => {
    const [searchQuery, setSearchQuery] = useState("");
    const [collections, setCollections] = useState<CollectionResponse[]>([]);
    const [url, setApiUrl] = useState(apiUrl);

    useEffect(() => {
        collectionRestClient.getAllCollections().then((x: any) => {
            setCollections(x.data);
        }).catch((e: any) => {
            setCollections([]);
        })
    }, []);

    return (
        <>
            <CardHeader>
                <div className="flex items-center space-x-2">
                    <h2 className='text-2xl font-semibold text-gray-900'>Browse resources</h2>
                </div>
            </CardHeader>
            <CardContent>
                <div className={"space-y-2"}>
                        <Input
                            placeholder="Search by label or description"
                            value={searchQuery}
                            onChange={(e: any) => setSearchQuery(e.target.value)}
                        />
                    <ArtefactsPage
                        url={url}
                        sources={selectedSources || []}
                        collection={selectedCollection}
                        query={searchQuery}/>

                </div>
            </CardContent>
        </>
    );
};

export default ArtefactsTable;
