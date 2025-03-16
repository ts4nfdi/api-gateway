import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import React from "react";
import {Button} from "@/components/ui/button";
import Link from "next/link";
import JSONViewer from "@/components/JSONViewer";

function gotToURI(uri: string, label: string) {
    if (!uri) return null;

    return <Button title={`Go ${uri}`}><Link target={'_blank'} href={uri}>Go to {label}</Link></Button>
}

function renderValue(value: any) {
    if (value === null) {
        return <span className="text-gray-400">null</span>;
    } else if (typeof value === 'boolean') {
        return value.toString();
    } else if (Array.isArray(value)) {
        if (typeof value[0] === 'object' && value[0] !== null) {
            return <JSONViewer data={value}/>;
        } else if (value.length !== 0) {
            return value.join(', ');
        } else {
            return <span className="text-gray-400">[]</span>;
        }
    } else if (typeof value === 'object') {
        return <JSONViewer data={value}/>;
    } else {
        return value.toString();
    }
}

export default function TermViewer({data, showGotoButtons = true}: any) {
    const keyOrder = [
        "iri", "label", "synonyms", "descriptions",
        '@id', 'prefLabel', 'labels', "synonym", 'description', 'definition',
        'short_form',
        'ontology', 'ontology_iri',
        'created', 'modified',
        'obsolete'
    ];

    const sortedData = Object.entries(data).sort(([keyA], [keyB]) => {
        const indexA = keyOrder.indexOf(keyA);
        const indexB = keyOrder.indexOf(keyB);

        if (indexA === -1 && indexB === -1) return keyA.localeCompare(keyB);
        if (indexA === -1) return 1;
        if (indexB === -1) return -1;
        return indexA - indexB;
    });

    return (
        <>
            <Table className={'w-full'}>
                <TableHeader>
                    <TableRow>
                        <TableHead className="w-1/3">Key</TableHead>
                        <TableHead>Value</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {sortedData.map(([key, value]: any) => (
                        <TableRow key={key}>
                            <TableCell className="font-medium">{key} {Array.isArray(value) ? '(array)' : ''}</TableCell>
                            <TableCell>{renderValue(value)}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
            {showGotoButtons &&
                <div className={'flex space-x-2 items-center'}>
                    {gotToURI(data.iri, 'original URI')}
                    {gotToURI(data.source_url, data.source_name)}
                </div>
            }
        </>
    );
}
