import {Artefact, useArtefacts} from "@/app/api/ArtefactsRestClient";
import React, {useMemo, useState} from "react";
import {Loading} from "@/components/Loading";
import {ScrollArea} from "@/components/ui/scroll-area";
import {QueryInput} from "@/components/CollectionBuilder";

export const DatabasesList = ({selectedSources}: { selectedSources: string[] }) => {
    return (
        <div className="flex flex-wrap gap-2">
            {selectedSources.map((source, index) => (
                <span key={index} className="text-xs bg-gray-100 text-primary py-1 px-2 rounded-full">
                    {source}
                </span>
            ))}
        </div>
    );
}
export const ArtefactsList = ({selectedArtefacts}: any) => {
    const [showAll, setShowAll] = useState(false);
    const maxVisible = 20;

    // Determine which items to display
    const displayedItems = showAll ? selectedArtefacts : selectedArtefacts.slice(0, maxVisible);
    const hasMore = selectedArtefacts.length > maxVisible;

    return (
        <div className="space-y-2">
            {/* Artifacts display */}
            <div className="flex flex-wrap gap-2">
                {displayedItems.map((x: Artefact, i: number) => (
                    <span key={i} className="text-xs bg-blue-100 text-blue-800 py-1 px-2 rounded-full">
            {x.short_form}
          </span>
                ))}
            </div>

            {/* See more/less button */}
            {hasMore && (
                <button
                    onClick={() => setShowAll(!showAll)}
                    className="text-blue-600 hover:text-blue-800 text-sm font-medium underline"
                >
                    {showAll
                        ? `Show less (${selectedArtefacts.length - maxVisible} hidden)`
                        : `See more (${selectedArtefacts.length - maxVisible} more)`
                    }
                </button>
            )}
        </div>
    );
};

export function ArtefactsSelectorList({selectedSources, selectedArtefacts, setSelectedArtefacts, collection}: any) {
    const [searchTerm, setSearchTerm] = useState("");
    const {items, loading} = useArtefacts(selectedSources, collection);

    const filteredItems = useMemo(() => {
        if (!items) return [];
        return items.filter(item =>
            item.label?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            item.short_form.toLowerCase().includes(searchTerm.toLowerCase())
        );
    }, [items, searchTerm]);

    const sameArtefact = (a: Artefact, short_form: string, source: string) =>
        a.short_form.toLowerCase() === short_form.toLowerCase() &&
        artefactSource(a)?.toLowerCase() === source.toLowerCase()

    const isSelected = (short_form: string, source: string) => {
        return selectedArtefacts?.some((x: Artefact) => sameArtefact(x, short_form, source))
    }

    const artefactSource = (artefact: Artefact) => {
        return artefact.source_name || artefact.source || "";
    }

    const selectArtefact = (artefact: Artefact) => {
        if (!setSelectedArtefacts || !selectedArtefacts) return;

        const alreadySelected = selectedArtefacts.some((x: Artefact) =>
            sameArtefact(x, artefact.short_form, artefactSource(artefact))
        );

        setSelectedArtefacts(
            alreadySelected
                ? selectedArtefacts.filter((x: Artefact) =>
                    !sameArtefact(x, artefact.short_form, artefactSource(artefact)))
                : [...selectedArtefacts, artefact]
        );
    }

    const handleSelectAll = () => {
        setSelectedArtefacts(
            selectedArtefacts.length === filteredItems.length ? [] : filteredItems
        );
    }

    const isAllSelected = useMemo(() =>
        filteredItems.length > 0 &&
        filteredItems.every(item =>
            selectedArtefacts.some((x: Artefact) => x === item)
        ), [filteredItems, selectedArtefacts]);

    return <div className="space-y-4 h-full flex flex-col">
        <div className="flex justify-between items-center">
            <h3 className="flex text-sm font-medium text-gray-700">
                Available Artefacts
            </h3>
            {filteredItems.length > 0 && (
                <div className="text-xs text-gray-400 flex items-center">
                    Total: {filteredItems.length} artefacts
                </div>
            )}
        </div>

        <div className={"flex items-center gap-1 flex-wrap text-xs text-gray-400 items-center'"}>
            <span>Selected: </span>
            <ArtefactsList selectedArtefacts={selectedArtefacts}/>
        </div>

        <div className="relative mb-3 flex items-center gap-2">
            <div className="flex-1">
                <QueryInput
                    query={searchTerm}
                    setQuery={setSearchTerm}
                    placeholder={"Search artefacts..."}
                />
            </div>

            {filteredItems.length > 0 && (
                <button
                    onClick={handleSelectAll}
                    className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                        isAllSelected
                            ? 'bg-blue-600 text-white hover:bg-blue-700'
                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200 border border-gray-300'
                    }`}
                >
                    {isAllSelected ? 'Deselect All' : 'Select All'}
                </button>
            )}
        </div>

        {loading ? (
            <Loading/>
        ) : (
            <div className="flex-1">
                <ScrollArea className="h-96 rounded-md border bg-white">
                    {filteredItems && filteredItems.length > 0 ? (
                        filteredItems.map((item, key) => {
                            const selected = isSelected(item.short_form, artefactSource(item));
                            return (
                                <div
                                    key={key}
                                    onClick={() => selectArtefact(item)}
                                    className={`flex justify-between items-center p-3 border-b hover:bg-gray-50 cursor-pointer transition-colors ${selected ? 'bg-blue-50' : ''}`}
                                >
                                    <div className="flex flex-grow items-center">
                                        <div
                                            className={`flex-shrink-0 mr-3 w-5 h-5 border rounded ${selected ? 'bg-blue-500 border-blue-500' : 'border-gray-300'}`}>
                                            {selected && (
                                                <svg className="w-5 h-5 text-white" viewBox="0 0 20 20"
                                                     fill="currentColor">
                                                    <path fillRule="evenodd"
                                                          d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                                                          clipRule="evenodd"/>
                                                </svg>
                                            )}
                                        </div>
                                        <div
                                            className={`flex flex-col flex-grow ${selected ? 'text-blue-700' : ''}`}>
                                            <span className="font-medium text-sm">{item.label}</span>
                                            <div className="flex gap-2 text-xs text-gray-500">
                                                                <span
                                                                    className="truncate max-w-xs">{item.short_form}</span>
                                                {item.source_name && (
                                                    <span
                                                        className="truncate max-w-xs text-gray-400">• {item.source_name}</span>
                                                )}
                                            </div>
                                            {item.descriptions && (
                                                <p className="text-xs text-gray-500 truncate max-w-lg mt-1">{item.descriptions}</p>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            );
                        })
                    ) : (
                        <div className="flex flex-col items-center justify-center h-64 text-gray-500">
                            {searchTerm ? (
                                <>
                                    <p className="text-sm mb-1">No results found</p>
                                    <p className="text-xs text-gray-400">Try a different search term</p>
                                </>
                            ) : (
                                <>
                                    <p className="text-sm mb-1">No items available</p>
                                    <p className="text-xs text-gray-400">Try selecting a different
                                        source</p>
                                </>
                            )}
                        </div>
                    )}
                </ScrollArea>
            </div>
        )}
    </div>
}
