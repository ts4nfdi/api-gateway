"use client";

import {useState} from "react";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import CollectionDialog from "@/app/auth/profile/components/collection-dialog";
import {CollectionResponse, collectionRestClient, useCollections} from "@/app/api/CollectionsRestClient";
import CollectionsSelectorTable from "@/components/CollectionsSelectorTable";
import {Loading} from "@/components/Loading";

export default function CollectionsTable() {
    const [dialogOpen, setDialogOpen] = useState(false);
    const [formData, setFormData] = useState<CollectionResponse>({
        id: "",
        label: "",
        description: "",
        collaborators: [],
        isPublic: false,
        terminologies: [],
    });
    const {collections, error, loading, setCollections} = useCollections()


    const handleAddOrEdit = (collection: CollectionResponse, addition: boolean) => {
        setCollections((prev) =>
            !addition
                ? prev.map((col) =>
                    col.id === collection.id
                        ? {...col, ...collection, terminologies: collection.terminologies}
                        : col
                )
                : [{...collection}, ...prev]
        )
    };

    const handleDelete = async (id: string) => {
        const res = await collectionRestClient.deleteCollection(id);
        if (res.status === 204) {
            setCollections((prev) => prev.filter((col) => col.id !== id));
        } else {
            console.error("Failed to delete collection");
        }
    };

    const openDialog = (collection: CollectionResponse | null) => {
        if (collection === null)
            collection = {
                id: "",
                label: "",
                description: "",
                collaborators: [],
                isPublic: false,
                terminologies: [],
            };

        setFormData(collection);
        setDialogOpen(true);
    };


    if (loading) {
        return <Loading/>;
    }

    if (error) {
        return (
            <div className="text-red-500">
                <p>Error loading collections: {error}</p>
            </div>
        );
    }

    return (
        <>
            <Card>
                <CardHeader>
                    <CardTitle className="text-l flex justify-between items-center font-semibold leading-tight">
                        <span>Collections</span>
                        <Button className="mx-2" onClick={() => openDialog(null)}>
                            Add a Collection
                        </Button>
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <CollectionsSelectorTable collections={collections}
                                              handleDelete={handleDelete}
                                              handleEdit={openDialog}/>
                </CardContent>
            </Card>
            <CollectionDialog isOpen={dialogOpen} setIsOpen={setDialogOpen} value={formData}
                              onSubmit={handleAddOrEdit}></CollectionDialog>
        </>)
}
