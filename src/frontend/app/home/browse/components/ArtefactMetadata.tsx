import React, {useCallback, useMemo, useState} from 'react';
import {Check, Copy, Eye, EyeOff} from 'lucide-react';
import {renderValue} from '@/components/TermViewer';

export const CopyButton = ({text, size}: any) => {
    const [copied, setCopied] = useState(false);

    const copyToClipboard = useCallback(async (e) => {
        e.preventDefault();
        e.stopPropagation();
        try {
            await navigator.clipboard.writeText(text);
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        } catch (err) {
            console.error('Failed to copy:', err);
        }
    }, [text]);

    const iconSize = size === "sm" ? "w-2.5 h-2.5" : "w-3 h-3";

    return (
        <button
            onClick={copyToClipboard}
            className={`p-1 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded transition-all duration-200 ${
                copied ? 'text-green-600 bg-green-50' : ''
            }`}
            title={copied ? "Copied!" : "Copy"}
        >
            {copied ? <Check className={iconSize}/> : <Copy className={iconSize}/>}
        </button>
    );
}

export default function ArtefactMetadata({
                                             data,
                                             showGoToButtons = true,
                                         }) {
    const [showTechnicalFields, setShowTechnicalFields] = useState(false);

    const toggleTechnicalFields = useCallback(() => {
        setShowTechnicalFields(prev => !prev);
    }, []);


    const fieldCategories = useMemo(() => {
        const primaryFields = ['iri', 'label', '@id', 'prefLabel', 'labels'];
        const descriptiveFields = ['synonyms', 'descriptions', 'synonym', 'description', 'definition'];
        const metadataFields = ['short_form', 'created', 'modified', 'obsolete', 'version', 'status'];
        const technicalFields = Object.keys(data).filter(key =>
            !primaryFields.includes(key) &&
            !descriptiveFields.includes(key) &&
            !metadataFields.includes(key)
        );

        return {primaryFields, descriptiveFields, metadataFields, technicalFields};
    }, [data]);

    const filteredData = useMemo(() => {
        let fields = [];
        fields.push(...fieldCategories.primaryFields);
        fields.push(...fieldCategories.descriptiveFields);
        fields.push(...fieldCategories.metadataFields);

        if (showTechnicalFields) {
            fields.push(...fieldCategories.technicalFields);
        }

        return fields;
    }, [data, fieldCategories, showTechnicalFields]);

    return (
        <div className={"max-h-[60vh] overflow-y-auto"}>
            {/* Data Table */}
            <div className="p-4">
                <div className="overflow-x-auto">
                    <table className="w-full">
                        <tbody className="divide-y divide-gray-100">
                        {filteredData.map(key => {
                            const value = data[key];
                            if (value === undefined || value === null || (Array.isArray(value) && value.length === 0)) {
                                return null;
                            }

                            return (
                                <tr key={key} className="hover:bg-gray-50 transition-colors group">
                                    <td className="px-2 py-3 text-xs font-medium text-gray-700 align-top">
                                        <div className="flex items-center gap-2">
                                            <p
                                                className="font-mono bg-gray-100 px-1.5 py-0.5 rounded group-hover:bg-gray-200 transition-colors">
                                                {key}
                                            </p>
                                            {Array.isArray(value) && (
                                                <p
                                                    className="bg-blue-100 text-blue-700 px-1.5 py-0.5 rounded-full text-xs font-medium">
                                                    array
                                                </p>
                                            )}
                                            <CopyButton text={value}/>
                                        </div>
                                    </td>
                                    <td className="px-2 py-3 text-xs text-gray-900">
                                        {renderValue(value)}
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>


                {/* Results count */}
                <div className="mt-4 text-xs text-gray-500 text-center flex items-center justify-between">
                    <span>Showing {filteredData.length} of {Object.keys(data).length} fields</span>
                    {fieldCategories.technicalFields.length > 0 && (
                        <button
                            onClick={toggleTechnicalFields}
                            className={`flex items-center gap-2 text-xs font-medium px-3 py-1.5 rounded-md transition-colors ${
                                showTechnicalFields
                                    ? 'bg-blue-100 text-blue-700 hover:bg-blue-200'
                                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                            }`}
                        >
                            {showTechnicalFields ? <EyeOff className="w-3 h-3"/> : <Eye className="w-3 h-3"/>}
                            {showTechnicalFields ? 'Hide' : 'Show'} All Fields
                            ({fieldCategories.technicalFields.length})
                        </button>
                    )}
                </div>

                {/* Go To Buttons */}
                {showGoToButtons && (
                    <div className="mt-4 flex flex-wrap gap-2">
                        {data.iri && (
                            <a
                                href={data.iri}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="inline-flex items-center px-3 py-1.5 text-xs font-medium text-blue-700 bg-blue-100 hover:bg-blue-200 rounded-md transition-colors"
                            >
                                Go to IRI
                            </a>
                        )}
                        {data.source_name && (
                            <a
                                href={`https://example.com/source/${data.source_name}`}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="inline-flex items-center px-3 py-1.5 text-xs font-medium text-green-700 bg-green-100 hover:bg-green-200 rounded-md transition-colors"
                            >
                                Go to Source
                            </a>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
