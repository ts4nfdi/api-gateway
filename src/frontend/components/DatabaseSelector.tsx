import MultipleSelector, {SelectorOption} from "@/components/MultipleSelector";
import React, {useState} from "react";
import {useDatabases} from "@/app/api/ConfigurationRestClient";

export default function DatabaseSelector({selected, onChange}: any) {
    const [selectedSources, setSelectedSources] = useState<string[]>(selected ?? []);
    const {databases, loading, error} = useDatabases()


    const handleChanges = (e: SelectorOption[]) => {
        const values = e.map((o: any) => o.value);
        setSelectedSources(values);
        if (onChange) onChange(values);
    }

    // Convert any incoming selected values to SelectorOption format



    if (loading) {
        return <MultipleSelector
            placeholder={"Loading databases..."}
            emptyIndicator={
                <p className="text-center text-lg leading-10 text-gray-600 dark:text-gray-400">
                    Loading options...
                </p>
            }
        />
    }

    const sourceOptions: SelectorOption[] = databases.map((db) => ({
        label: db.name,
        value: db.name,
    }))
    const selectedOptions = selectedSources
        .map(value => {
            // Check if it exists in our loaded options
            const existingOption = sourceOptions.find(opt => opt.value === value);
            if (existingOption) return existingOption;

            // If not found but we have a value, create a temporary option
            return {label: value, value};
        });

    return (
        <div className="relative">
            <MultipleSelector
                defaultOptions={sourceOptions}
                placeholder={"Select a source..."}
                onChange={handleChanges}
                value={selectedOptions}
                emptyIndicator={
                    <p className="text-center text-lg leading-10 text-gray-600 dark:text-gray-400">
                        No results found.
                    </p>
                }
            />
        </div>
    );
}
