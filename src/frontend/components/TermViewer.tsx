import React from "react";
import {Button} from "@/components/ui/button";
import Link from "next/link";
import JSONViewer from "@/components/JSONViewer";
import {useArtefactConceptTree} from "@/app/api/ArtefactsRestClient";
import {ArtefactTerm} from "@/app/api/SearchRestClient";
import {Loading} from "@/components/Loading";
import ArtefactConcepts from "@/app/home/browse/components/ArtefactConcepts";

export function gotToURI(uri: string, label: string) {
    if (!uri) return null;

    return <Button title={`Go ${uri}`}><Link target={'_blank'} href={uri}>Go to {label}</Link></Button>
}

export function renderValue(value: any) {
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

export default function TermViewer({data, showGotoButtons = true}: {
    data: ArtefactTerm,
    showGotoButtons?: boolean
}) {


    let acronym = data.ontology;

    const {tree, loading, error} = useArtefactConceptTree(acronym, data.iri, data.source_name);

    if (!data) {
        return <div className="p-4 text-gray-500">No data available.</div>;
    }

    if (loading) {
        return <Loading/>
    }

    if (error) {
        return <div className="p-4 text-red-500">Error loading data: {error}</div>;
    }

    return <ArtefactConcepts concepts={tree} selected={data} showGoToButtons={showGotoButtons} />
}
