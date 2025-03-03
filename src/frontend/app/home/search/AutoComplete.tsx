'use client';

import React, {useEffect, useState} from "react";
import {useSearch} from "@/lib/search";
import ModalContainer, {useModal} from "@/lib/modal";
import {AutoCompleteResult} from "@/app/home/search/AutoCompleteResult";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Card, CardContent} from "@/components/ui/card";
import TextInput from "@/components/TextInput";
import JsonViewerModal from "@/components/JsonVieweModal";
import DatabaseSelector from "@/components/DatabaseSelector";
import {Loading} from "@/components/Loading";
import CollectionSelector from "@/components/CollectionsSelector";


const StatCard = ({title, description, titleColor}: any) => {
    // Map EUI color values to Tailwind classes
    const getTitleColorClass = (color: string) => {
        switch (color) {
            case 'success':
                return 'text-green-600';
            case 'danger':
                return 'text-red-600';
            case 'primary':
                return 'text-blue-600';
            case 'warning':
                return 'text-yellow-600';
            default:
                return 'text-gray-900';
        }
    };

    return (
        <div className="text-center">
            <p className="text-sm text-gray-500">{description}</p>
            <p className={`text-lg font-bold ${titleColor ? getTitleColorClass(titleColor) : ''}`}>
                {title}
            </p>
        </div>
    );
};


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
            {/* Conditionally render content */}
            {(isLoading || errorMessage || suggestions.length > 0) && (
                <div className="w-full">
                    {/* Loader */}
                    {isLoading && (
                        <Card className=''>
                            <CardContent className="p-4">
                                <Loading/>
                            </CardContent>
                        </Card>
                    )}

                    {/* Error message */}
                    {errorMessage && (
                        <Card className=''>
                            <CardContent className="p-4">
                                <div className="text-red-500">
                                    <p>Error: {errorMessage}</p>
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    {/* Results */}
                    {suggestions.length > 0 && !isLoading && !errorMessage && (
                        <div className="space-y-2">
                            {/* Stats Card */}
                            <Card className="">
                                <CardContent className="p-1">
                                    <div className="grid grid-cols-2 gap-4">
                                        <StatCard
                                            title={responseTime + 's'}
                                            description="Response Time:"
                                        />
                                        <StatCard
                                            title={suggestions.length}
                                            description="Results count"
                                            titleColor="success"
                                        />
                                    </div>
                                </CardContent>
                            </Card>

                            {/* Results List */}
                            <Card className="shadow-lg overflow-hidden max-h-96 overflow-y-auto">
                                <div className="w-full">
                                    {suggestions.map((suggestion: any, index: any) => (
                                        <ListItem
                                            key={index}
                                            onClick={() => openModal(suggestion)}
                                        >
                                            <AutoCompleteResult suggestion={suggestion}/>
                                        </ListItem>
                                    ))}
                                </div>
                            </Card>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default function Autocomplete(props: { apiUrl: string }) {
    const {
        apiUrl,
        suggestions,
        inputValue,
        responseTime,
        isLoading,
        errorMessage,
        handleInputChange,
        handleApiUrlChange,
        setApiUrl,
        debouncedFetchSuggestions
    } = useSearch(props);

    const [selectedSources, setSelectedSources] = useState<string[]>([]);
    const [selectedCollection, setSelectedCollection] = useState<string[]>([]);
    const {isModalOpen, selectedObject, openModal, closeModal} = useModal();


    useEffect(() => {
        setApiUrl(`${props.apiUrl}${inputValue}&database=${selectedSources.join(',')}&collectionId=${selectedCollection.join(',')}`);
    }, [selectedSources, inputValue, props.apiUrl, selectedCollection, setApiUrl]);

    useEffect(() => {
        debouncedFetchSuggestions(inputValue);
    }, [apiUrl, debouncedFetchSuggestions]);

    return (
        <>
            <div className="space-y-6 w-full">
                <div className="space-y-2">
                    <div className="flex space-x-1 items-center">
                        <Label htmlFor="apiUrl">Gateway search endpoint</Label>
                        <JsonViewerModal url={apiUrl}/>
                    </div>

                    <Input
                        id="apiUrl"
                        value={apiUrl}
                        onChange={handleApiUrlChange}
                        placeholder="Enter API URL"
                        className="w-full"
                    />
                </div>

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
                <div className="flex items-center space-x-1">
                    <div className="w-1/2">
                        <DatabaseSelector onChange={setSelectedSources}/>
                    </div>
                    <div className="w-1/2">
                        <CollectionSelector onChange={setSelectedCollection}/>
                    </div>
                </div>

                <SearchResultsCard openModal={openModal} suggestions={suggestions} isLoading={isLoading}
                                   errorMessage={errorMessage} responseTime={responseTime}/>
            </div>
            <ModalContainer onClose={closeModal} artefact={selectedObject} isOpen={isModalOpen}></ModalContainer>
        </>
    );
}
