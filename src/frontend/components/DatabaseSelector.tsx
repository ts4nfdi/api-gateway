import MultipleSelector, {SelectorOption} from "@/components/MultipleSelector";
import React from "react";

export default function DatabaseSelector({sourceOptions, setSelectedSources}: any) {
    return (
        <MultipleSelector
            defaultOptions={sourceOptions}
            placeholder="Select by source..."
            onChange={(e: SelectorOption[]) => setSelectedSources(e.map((o: any) => o.value))}
            emptyIndicator={
                <p className="text-center text-lg leading-10 text-gray-600 dark:text-gray-400">
                    no results found.
                </p>
            }
        />)
}