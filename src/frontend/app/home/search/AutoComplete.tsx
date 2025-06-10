'use client';

import React, {useEffect} from "react";
import {useModal} from "@/lib/modal";
import {AutoCompleteResult} from "@/app/home/search/AutoCompleteResult";
import {Label} from "@/components/ui/label";
import {CardContent, CardHeader} from "@/components/ui/card";
import TextInput from "@/components/TextInput";
import {Loading} from "@/components/Loading";
import {APICallDebug} from "@/app/home/MainPage";
import {CollectionResponse} from "@/app/api/CollectionsRestClient";
import {useSearch} from "@/app/api/SearchRestClient";


const ListItem = ({onClick, children}: any) => {
    return (
        <button
            onClick={onClick}
            className="w-full text-left p-3 border-b border-gray-200 hover:bg-gray-50 transition-colors duration-150 last:border-b-0 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
            {children}
        </button>
    );
};

const SearchResultsCard = ({suggestions, isLoading, errorMessage, responseTime, openModal}: any) => {
    return (
        <div className="w-full">
            {(isLoading || errorMessage || suggestions?.length > 0) && (
                <div className="w-full">
                    {isLoading && (
                        <div className=''>
                            <CardContent className="p-4">
                                <Loading/>
                            </CardContent>
                        </div>
                    )}

                    {errorMessage && (
                        <div className=''>
                            <CardContent className="p-4">
                                <div className="text-red-500">
                                    <p>Error: {errorMessage}</p>
                                </div>
                            </CardContent>
                        </div>
                    )}

                    {suggestions?.length > 0 && !isLoading && !errorMessage && (
                        <div className="space-y-2">
                            <div className="shadow-lg overflow-hidden max-h-96 overflow-y-auto">
                                <div className="w-full">
                                    {suggestions.map((suggestion: any, index: any) => (
                                        <ListItem key={index}>
                                            <AutoCompleteResult suggestion={suggestion}/>
                                        </ListItem>
                                    ))}
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default function Autocomplete(props: {
    apiUrl: string,
    selectedSources: string[],
    selectedCollection: CollectionResponse
}) {
    const {openModal} = useModal();
    const {selectedSources, selectedCollection} = props;

    const {
        debouncedFetchSuggestions, inputValue,
        suggestions, isLoading, errorMessage,
        responseConfig, handleInputChange
    } = useSearch({databases: selectedSources, collection: selectedCollection})

    useEffect(() => {
        debouncedFetchSuggestions(inputValue);
    }, [debouncedFetchSuggestions, inputValue]);

    return (
        <>
            <CardHeader>
                <div className="flex items-center space-x-2">
                    <h2 className='text-2xl font-semibold text-gray-900'>Find a concept</h2>
                </div>
            </CardHeader>
            <CardContent>
                <div className="space-y-2 w-full my-5">
                    <div className="space-y-2">
                        <Label htmlFor="search">Search</Label>
                        <TextInput
                            id="search"
                            placeholder="Search a term"
                            value={inputValue}
                            isClearable={true}
                            onChange={handleInputChange}
                        />
                    </div>
                    {!isLoading && !errorMessage && suggestions != null && (
                        <APICallDebug apiUrl={props.apiUrl}
                                      selectedSources={selectedSources}
                                      selectedCollection={selectedCollection}
                                      resultsCount={suggestions.length}
                                      responseTime={responseConfig?.totalResponseTime || -1}
                        />
                    )}
                    <SearchResultsCard openModal={openModal} suggestions={suggestions} isLoading={isLoading}
                                       errorMessage={errorMessage}/>
                </div>
            </CardContent>
        </>
    );
}
