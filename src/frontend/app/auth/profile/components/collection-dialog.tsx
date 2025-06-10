import React, {memo, useEffect, useMemo, useState} from "react";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";
import {CollectionDetails} from "@/components/CollectionBuilder";
import {Artefact} from "@/app/api/ArtefactsRestClient";
import {CollectionResponse, collectionRestClient} from "@/app/api/CollectionsRestClient";
import {ArtefactsSelectorList} from "@/components/ArtefactsSelectorList";
import DatabaseSelector from "@/components/DatabaseSelector";
import {getSourcesFromArtefacts} from "@/lib/utils";

const ArtefactsSelectorListMemo = memo(({ selectedSources, selectedArtefacts, setSelectedArtefacts }: any) => {
    return (
        <ArtefactsSelectorList
            selectedSources={selectedSources}
            selectedArtefacts={selectedArtefacts}
            setSelectedArtefacts={setSelectedArtefacts}
        />
    );
});
ArtefactsSelectorListMemo.displayName = "ArtefactsSelectorListMemo";

export default function CollectionDialog({
                                             isOpen,
                                             setIsOpen,
                                             value,
                                             onSubmit,
                                         }: any) {
    const [collection, setCollection] = useState<CollectionResponse>(
        value ?? {
            id: "",
            label: "",
            description: "",
            collaborators: [],
            terminologies: [],
            isPublic: false,
        }
    );


    const [selectedArtefacts, setSelectedArtefacts] = useState<Artefact[]>([]);
    const [selectedSources, setSelectedSources] = useState<string[]>([]);

    const initialSelectedArtefacts = useMemo(() => {
        if (!value) return [];
        return value.terminologies.map((term: any) => ({
            label: term.label,
            short_form: term.label,
            source: term.source,
            uri: term.uri,
        }));
    }, [value]);


    useEffect(() => {
        if (value) {
            setCollection(value);
            setSelectedArtefacts(initialSelectedArtefacts);
            setSelectedSources(getSourcesFromArtefacts(initialSelectedArtefacts));
        }
    }, [value, initialSelectedArtefacts]);


    useEffect(() => {
        collection.terminologies = selectedArtefacts.map((artefact: any) => ({
            label: artefact.short_form,
            source: artefact.source_name || artefact.source,
            uri: artefact.iri || artefact.uri
        }));
    }, [collection, selectedArtefacts]);

    const handleSave = async () => {
        let res: any;
        if (collection.id === "") {
            delete collection.id;
            res = await collectionRestClient.createCollection(collection as any);
        } else if (collection.id) {
            res = await collectionRestClient.updateCollection(collection.id, collection);
        }

        if (res.status === 201 || res.status === 200) {
            onSubmit(res.data, value.id === "");
            setIsOpen(false);
        } else {
            console.error("Failed to save/update collection");
            // TODO add notification
        }
    };


    return <Dialog open={isOpen} onOpenChange={setIsOpen}>
        <DialogContent className="h-[70vh] w-[70vw] max-h-[90vh] max-w-[800px]">
            <DialogHeader>
                <DialogTitle>
                    {collection.id ? "Edit Collection" : "Create a New Collection"}
                </DialogTitle>
            </DialogHeader>

            <div className="space-y-6  overflow-y-auto px-1">
                <div className="space-y-3">
                    <CollectionDetails
                        selectedSources={selectedSources}
                        selectedArtefacts={selectedArtefacts}
                        data={collection}
                        setData={setCollection}/>
                    <div>
                        <h3 className="flex text-sm font-medium text-gray-700">
                            Available Databases
                        </h3>
                        <DatabaseSelector selected={selectedSources} onChange={(sources: string[]) => setSelectedSources(sources)} />
                    </div>
                    <div className="flex flex-col">
                        <ArtefactsSelectorListMemo selectedSources={selectedSources}
                                               selectedArtefacts={selectedArtefacts}
                                               setSelectedArtefacts={setSelectedArtefacts}/>
                    </div>
                </div>
            </div>

            <DialogFooter className="mt-6">
                <Button variant="outline" onClick={() => setIsOpen(false)}>
                    Cancel
                </Button>
                <Button onClick={handleSave}>
                    {collection.id ? "Update" : "Create"} Collection
                </Button>
            </DialogFooter>
        </DialogContent>
    </Dialog>
}