import MultipleSelector, {SelectorOption} from "@/components/MultipleSelector";
import React, {useEffect, useState} from "react";
import {configurationRestClient} from "@/lib/ConfigurationRestClient";
import {collectionRestClient} from "@/app/auth/lib/CollectionsRestClient";

export default function CollectionSelector({selected, onChange}: any) {
    const [sourceOptions, setSourceOptions] = useState<SelectorOption[]>([]);
    const [selectedSources, setSelectedSources] = useState<string[]>(selected || []);
    const [isLoading, setIsLoading] = useState<boolean>(true);

    useEffect(() => {
        collectionRestClient.getAllCollections().then((x: any) => {
            const options = x.data.map((db: any) => ({
                label: db.label,
                value: db.id
            }));
            setSourceOptions(options);
            setIsLoading(false);
        }).catch(error => {
            console.error("Failed to load databases:", error);
            setIsLoading(false);
        });
    }, []);

    const handleChanges = (e: SelectorOption[]) => {
        const values = e.map((o: any) => o.value);
        setSelectedSources(values);
        if(onChange) onChange(values);
    }

    // Convert any incoming selected values to SelectorOption format
    const selectedOptions = selectedSources
        .map(value => {
            // Check if it exists in our loaded options
            const existingOption = sourceOptions.find(opt => opt.value === value);
            if (existingOption) return existingOption;

            // If not found but we have a value, create a temporary option
            return {label: value, value};
        });

    if (isLoading) {
        return <MultipleSelector
            placeholder={"Loading databases..."}
            emptyIndicator={
                <p className="text-center text-lg leading-10 text-gray-600 dark:text-gray-400">
                    Loading options...
                </p>
            }
        />
    }
    return (
        <div className="relative">
            <MultipleSelector
                defaultOptions={sourceOptions}
                placeholder={"Select a collection..."}
                onChange={handleChanges}
                value={selectedOptions}
                maxSelected={1}
                emptyIndicator={
                    <p className="text-center text-lg leading-10 text-gray-600 dark:text-gray-400">
                        No results found.
                    </p>
                }
            />
        </div>
    );
}
