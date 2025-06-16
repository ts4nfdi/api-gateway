import React from 'react';
import {Separator} from "@/components/ui/separator";
import ArtefactsTable from "@/app/home/browse/BrowseResources";
import Autocomplete from "@/app/home/search/AutoComplete";
import Header from "@/app/home/Header";
import {CollectionResponse} from "@/app/api/CollectionsRestClient";
import {msToSeconds} from "@/lib/utils";
import useSelectedCollectionStore from "@/store/useCollectionStore";


export function APICallDebug({
                                 apiUrl,
                                 selectedSources,
                                 selectedCollection,
                                 resultsCount,
                                 responseTime
                             }: {
    apiUrl: string,
    selectedSources: string[],
    selectedCollection?: CollectionResponse,
    resultsCount?: number,
    responseTime?: number
}) {
    if (!apiUrl) return null;

    const fullUrl = new URL(apiUrl);
    if (selectedCollection?.id) {
        fullUrl.searchParams.set('collectionId', selectedCollection.id);
    }
    if (selectedSources?.length > 0) {
        fullUrl.searchParams.set('databases', selectedSources.join(','));
    }

    return (
        <div className="flex justify-between text-xs text-gray-500 p-2">
            <div className="font-mono">
                <span className="text-gray-400 mr-1">API Call:</span>
                <a
                    href={fullUrl.toString()}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-blue-600 hover:text-blue-800 underline hover:no-underline"
                >
                    {fullUrl.toString()}
                </a>
            </div>
            <div>
                <div className="flex items-center gap-3 mt-1">
                    {resultsCount !== undefined && (
                        <div className="flex items-center gap-1">
                            <span className="w-2 h-2 bg-green-500 rounded-full"></span>
                            <span className="text-gray-400">Results:</span>
                            <span>{resultsCount}</span>
                        </div>
                    )}
                    {responseTime && responseTime > 0 && (
                        <div className="flex items-center gap-1">
                            <span className="w-2 h-2 bg-yellow-500 rounded-full"></span>
                            <span className="text-gray-400">Time:</span>
                            <span>{msToSeconds(responseTime)}</span>
                        </div>
                    )}
                    {selectedCollection?.id && (
                        <div>
                            <span className="text-gray-400">Collection:</span>
                            <span className="ml-1">{selectedCollection.label || selectedCollection.id}</span>
                        </div>
                    )}
                    {selectedSources?.length > 0 && (
                        <div>
                            <span className="text-gray-400">Sources:</span>
                            <span className="ml-1">{selectedSources.join(',')}</span>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}


export function MainPage({apiUrl}: { apiUrl: string }) {
    const {data} = useSelectedCollectionStore();
    const selectedSources = data.sources || [];
    const selectedCollection = data.collection || null;

    return (<>
            <Header/>
            <div className="bg-white w-full">
                <div className='container mx-auto space-y-2'>
                    <Autocomplete apiUrl={`${apiUrl}/search?query=`}
                                  selectedSources={selectedSources}
                                  selectedCollection={selectedCollection}/>
                </div>
                <Separator/>
                <div className='bg-white w-full min-h-[50vh]'>
                    <div className='container mx-auto space-y-2'>
                        <ArtefactsTable apiUrl={`${apiUrl}/artefacts?showResponseConfiguration=true`}
                                        selectedSources={selectedSources}
                                        selectedCollection={selectedCollection}/>
                    </div>
                </div>
            </div>
        </>
    )
}
