'use client'
import React, {useState} from 'react';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '@/components/ui/card';
import {
    CheckCircleIcon,
    DatabaseIcon,
    EditIcon,
    ExternalLinkIcon,
    FileIcon,
    ListIcon,
    PlusIcon,
    RefreshCwIcon
} from "lucide-react";
import {TopNav} from "@/app/TopNav";
import DataMergeGraph from "@/app/home/Animation";
import DialogWrapper from "@/components/Dialog";
import {CollectionBuilder} from "@/components/CollectionBuilder";
import useCollectionStore from "@/store/useCollectionStore";
import CollectionsSelectorTable from "@/components/CollectionsSelectorTable";
import {CollectionResponse, useCollections} from "@/app/api/CollectionsRestClient";
import {Loading} from "@/components/Loading";
import {DatabasesList} from "@/components/ArtefactsSelectorList";
import {getSourcesFromArtefacts} from "@/lib/utils";
import {useDatabases} from "@/app/api/ConfigurationRestClient";

function BigLinkToAPI() {
   return <Card className="transition-colors hover:bg-muted/90">
        <CardContent className="p-6">
            <a href={process.env.API_GATEWAY_URL} target="_blank"
               className="flex items-center justify-between">
                <p className="text-sm text-muted-foreground">
                    2️⃣ Access the API endpoint for the documentation and usage
                </p>
                <ExternalLinkIcon className="h-5 w-5"/>
            </a>
        </CardContent>
    </Card>
}

enum ModalMode {
    CREATE = 'CREATE',
    LIST = 'LIST',
}

function AllDatabasesList() {
    const {databases, loading, error} = useDatabases()
    if (loading) return <span>Loading databases names</span>;
    if (error) return <div className="text-red-500">Error loading databases: {error}</div>;
    return <DatabasesList selectedSources={databases.map(x => x.name)}/>
}

function SelectCollection({handleSelection}: any) {
    const {collections, loading} = useCollections()
    if (loading) return <Loading/>;
    return <CollectionsSelectorTable collections={collections} handleSelection={handleSelection}/>
}


function SelectorCreateCollection() {
    const [dialogOpen, setDialogOpen] = useState(false);
    const {data, setCollection, setSources, setArtefacts} = useCollectionStore()
    const [modalMode, setModalMode] = useState<ModalMode>(ModalMode.CREATE);

    const renderSelectedCollectionState = () => {
        const artefacts = data.artefacts.length > 0 ? data.artefacts : data.collection.terminologies;
        const sources = data.sources.length > 0 ? data.sources : getSourcesFromArtefacts(data.collection.terminologies as any);
        const collectionLabel = (collection: CollectionResponse) => {
            if (collection.label) return collection.label;
            if (collection.id) return `Collection ${collection.id}`;
            return 'Unnamed Collection';
        };

        return <div className="flex items-center justify-between w-full text-sm">
            <div className="flex items-center gap-3">
                <div className="flex items-center gap-2">
                    <CheckCircleIcon className="h-5 w-5 text-green-600"/>
                    <div className="font-medium text-foreground">
                        Collection: {collectionLabel(data.collection)}
                    </div>
                </div>
                <div className="flex items-center gap-4 text-sm text-muted-foreground">
                    {sources?.length > 0 && (
                        <div className="flex items-center gap-1">
                            <DatabaseIcon className="h-4 w-4"/>
                            {sources.length} source{sources.length !== 1 ? 's' : ''}
                        </div>
                    )}
                    {artefacts?.length > 0 && (
                        <div className="flex items-center gap-1">
                            <FileIcon className="h-4 w-4"/>
                            {artefacts.length} artefact{artefacts.length !== 1 ? 's' : ''}
                        </div>
                    )}
                </div>
            </div>
            <div className="flex items-center gap-2">
                <button
                    type="button"
                    onClick={(e) => {
                        e.stopPropagation();
                        openModal(ModalMode.CREATE);
                    }}
                    className="inline-flex items-center gap-1 px-3 py-1 text-xs bg-primary/10 text-primary hover:bg-primary/20 rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-primary/20"
                >
                    <EditIcon className="h-3 w-3"/>
                    Edit
                </button>
                <button
                    type="button"
                    onClick={(e) => {
                        e.stopPropagation();
                        openModal(ModalMode.LIST);
                    }}
                    className="inline-flex items-center gap-1 px-3 py-1 text-xs bg-secondary text-secondary-foreground hover:bg-secondary/80 rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-secondary/20"
                >
                    <RefreshCwIcon className="h-3 w-3"/>
                    Switch
                </button>
            </div>
        </div>
    }

    const handleSelection = (collection: CollectionResponse) => {
        setCollection(collection);
        setDialogOpen(false);
    }

    const openModal = (mode: ModalMode) => {
        setModalMode(mode);
        if (mode === ModalMode.CREATE) {
            setCollection({} as CollectionResponse);
        }
        setDialogOpen(true);
    }

    const isCreatingCollection = () => (data.sources.length > 0 || data.artefacts.length > 0) && !data.collection.id;

    return <span className={"flex"}>
        <Card className="">
            <CardContent className="flex space-x-2 items-center justify-between p-6">
                {!data.collection.id && data.sources.length === 0 && data.artefacts.length === 0 && (
                    <div className="flex items-center justify-between w-full space-x-2">
                        <div className="flex flex-wrap items-center gap-1 text-sm text-muted-foreground">
                            <span>1️⃣ Customize your experience by</span>
                            <button
                                type="button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    openModal(ModalMode.LIST);
                                }}
                                className="inline-flex items-center gap-1 text-primary hover:underline focus:outline-none focus:ring-2 focus:ring-primary/20 rounded"
                            >
                                browsing available collections <ListIcon/>
                            </button>
                            <span>or</span>
                            <button
                                type="button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    openModal(ModalMode.CREATE);
                                }}
                                className="inline-flex items-center gap-1 text-primary hover:underline focus:outline-none focus:ring-2 focus:ring-primary/20 rounded"
                            >
                                creating your own <PlusIcon/>
                            </button>
                            <span>, using only the resources you need across our federated databases: </span>
                            <AllDatabasesList/>
                        </div>
                    </div>
                )}

                {data.collection.id || isCreatingCollection() ? renderSelectedCollectionState() : null}
            </CardContent>
        </Card>

        <DialogWrapper showFooter={false} title={"Build Your Collection"} isOpen={dialogOpen} setIsOpen={setDialogOpen}>
            {modalMode == ModalMode.LIST && <SelectCollection handleSelection={handleSelection}/>}
            {modalMode == ModalMode.CREATE && (
                <CollectionBuilder
                    selectedArtefacts={data.artefacts}
                    setSelectedArtefacts={setArtefacts}
                    selectedSources={data.sources}
                    setSelectedSources={setSources}
                    onSave={(collection, isNew) => {
                        setDialogOpen(false)
                        setCollection(collection)
                    }}
                />
            )}
        </DialogWrapper>
    </span>
}


export const Banner = () => {
    return (
        <div className="bg-gradient-to-r from-blue-600 to-indigo-600">
            <Card className="container mx-auto bg-transparent border-0 shadow-none text-white">
                <CardHeader className="py-16 space-y-2">
                    <CardTitle className="text-5xl font-bold max-w-1xl">Welcome to TSNFDI API Gateway</CardTitle>
                    <CardDescription className="flex mt-5 flex-wrap">
                        <div className={'text-xl text-white/80 w-1/2 space-y-4 flex flex-col'}>
                            <p className={'text-justify'}>
                                The TS4NFDI Federated Service is an advanced, dynamic solution designed to perform
                                federated
                                calls across multiple Terminology Services (TS) within NFDI. It is particularly tailored
                                for
                                environments where integration and aggregation of diverse data sources are essential.
                                The
                                service offers search capabilities, enabling users to refine search results based on
                                specific criteria, and supports responses in both JSON and JSON-LD formats.
                            </p>
                            <div>
                                <SelectorCreateCollection/>
                            </div>
                            <div>
                                <BigLinkToAPI/>
                            </div>
                        </div>

                        <div className={'p-3'}>
                            <DataMergeGraph width={500} height={400}/>
                        </div>

                    </CardDescription>
                </CardHeader>
            </Card>
        </div>
    )
}


export default function Header() {
    return (
        <div>
            <TopNav/>
            <Banner/>
        </div>
    )
}
