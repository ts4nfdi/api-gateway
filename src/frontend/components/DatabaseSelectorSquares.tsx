import {useEffect, useState} from 'react';
import {Check, Database} from 'lucide-react';
import {configurationRestClient} from "@/app/api/ConfigurationRestClient";

interface SelectorOption {
    label: string;
    value: string;
}

export default function DatabaseSelectorSquares({selected, onChange}: any) {
    const [sourceOptions, setSourceOptions] = useState<SelectorOption[]>([]);
    const [selectedSources, setSelectedSources] = useState<string[]>(selected ?? []);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [filterText, setFilterText] = useState<string>('');

    useEffect(() => {
        configurationRestClient.getAllDatabases().then((x: any) => {
            const options = x.data.map((db: any) => ({
                label: db.name,
                value: db.name
            }));
            setSourceOptions(options);
            setIsLoading(false);
        }).catch(error => {
            console.error("Failed to load databases:", error);
            setIsLoading(false);
        });
    }, []);

    const handleToggle = (value: string) => {
        const newSelection = selectedSources.includes(value)
            ? selectedSources.filter(item => item !== value)
            : [...selectedSources, value];

        setSelectedSources(newSelection);
        if(onChange) onChange(newSelection);
    };

    const handleSelectAll = () => {
        const allValues = filteredOptions.map(option => option.value);
        const newSelection = selectedSources.length === filteredOptions.length &&
        filteredOptions.every(option => selectedSources.includes(option.value))
            ? [] // Deselect all if all are selected
            : allValues; // Select all

        setSelectedSources(newSelection);
        if(onChange) onChange(newSelection);
    };


    // Filter options based on search text
    const filteredOptions = sourceOptions.filter(option =>
        option.label.toLowerCase().includes(filterText.toLowerCase())
    );

    const isAllSelected = filteredOptions.length > 0 &&
        filteredOptions.every(option => selectedSources.includes(option.value));

    if (isLoading) {
        return (
            <div className="w-full p-4 border border-gray-200 rounded-lg">
                <div className="text-center text-lg leading-10 text-gray-600 dark:text-gray-400">
                    Loading databases...
                </div>
            </div>
        );
    }

    return (
        <div className="w-full flex flex-col overflow-y-auto">
            {/* Filter Input and Select All */}
            <div className="mb-4 flex gap-2 items-center">
                <input
                    type="text"
                    placeholder="Filter databases..."
                    value={filterText}
                    onChange={(e) => setFilterText(e.target.value)}
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                {filteredOptions.length > 0 && (
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

            {/* Checkbox Grid */}
            <div className="border border-gray-200 rounded-lg p-4  flex-1 overflow-y-auto">
                {filteredOptions.length === 0 ? (
                    <p className="text-center text-lg leading-10 text-gray-600 dark:text-gray-400">
                        {filterText ? 'No databases match your filter.' : 'No databases found.'}
                    </p>
                ) : (
                    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
                        {filteredOptions.map((option) => (
                            <div
                                key={option.value}
                                onClick={() => handleToggle(option.value)}
                                className={`
                                    relative p-4 border-2 rounded-lg cursor-pointer transition-all duration-200 hover:shadow-md
                                    ${selectedSources.includes(option.value)
                                    ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                                    : 'border-gray-200 hover:border-gray-300 bg-white dark:bg-gray-800'
                                }
                                `}
                            >
                                {/* Selection indicator */}
                                {selectedSources.includes(option.value) && (
                                    <div className="absolute top-2 right-2 w-5 h-5 bg-blue-500 rounded-full flex items-center justify-center">
                                        <Check className="w-3 h-3 text-white" />
                                    </div>
                                )}

                                {/* Database icon */}
                                <div className="flex flex-col items-center text-center space-y-2">
                                    <Database
                                        className={`w-8 h-8 ${
                                            selectedSources.includes(option.value)
                                                ? 'text-blue-600'
                                                : 'text-gray-600'
                                        }`}
                                    />
                                    <span className={`text-sm font-medium leading-tight ${
                                        selectedSources.includes(option.value)
                                            ? 'text-blue-700 dark:text-blue-300'
                                            : 'text-gray-700 dark:text-gray-300'
                                    }`}>
                                        {option.label}
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Selected Count */}
            {selectedSources.length > 0 && (
                <div className="mt-2 text-sm text-gray-600">
                    {selectedSources.length} database{selectedSources.length !== 1 ? 's' : ''} selected
                </div>
            )}
        </div>
    );
}
