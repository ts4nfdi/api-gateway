"use client";

import {useEffect, useState} from "react";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {CollectionResponse, collectionRestClient} from "@/app/auth/lib/CollectionsRestClient";
import UUIDDisplay from "@/components/CopyId";
import CollectionDialog from "@/app/auth/profile/components/collection-dialog";

export default function CollectionsTable() {
    const [collections, setCollections] = useState<CollectionResponse[]>([]);
    const [dialogOpen, setDialogOpen] = useState(false);
    const [formData, setFormData] = useState<CollectionResponse>({
        id: "",
        label: "",
        description: "",
        terminologies: [],
    });

    useEffect(() => {
        const fetchCollections = async () => {
            const res: any = await collectionRestClient.getAllCollections();
            setCollections(res.data);
        };
        fetchCollections();
    }, []);

    const handleAddOrEdit = () => {
        /*setCollections((prev) =>
            formData.id
                ? prev.map((col) =>
                    col.id === formData.id
                        ? {...col, ...formData, tags: formData.tags.split(",")}
                        : col
                )
                : [...prev, {...formData, tags: formData.tags.split(",")}]
        );*/
        resetForm();
    };

    const handleDelete = (id: string) => {
        setCollections((prev) => prev.filter((col) => col.id !== id));
    };

    const openDialog = (collection: any) => {
        setFormData(collection);
        setDialogOpen(true);

    };

    const resetForm = () => {
        setFormData({id: "", label: "", description: "", terminologies: []});
        setDialogOpen(false);
    };

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
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>ID</TableHead>
                                <TableHead>Label</TableHead>
                                <TableHead>Description</TableHead>
                                <TableHead>Tags</TableHead>
                                <TableHead>Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {collections.map((collection) => (
                                <TableRow key={collection.id}>
                                    <TableCell><UUIDDisplay value={collection.id}/></TableCell>
                                    <TableCell>{collection.label}</TableCell>
                                    <TableCell>{collection.description}</TableCell>
                                    <TableCell>
                                        {collection.terminologies.map((tag, index) => (
                                            <Badge key={index} className="mr-2">
                                                {tag}
                                            </Badge>
                                        ))}
                                    </TableCell>
                                    <TableCell>
                                        <Button
                                            variant="secondary"
                                            size="sm"
                                            onClick={() => openDialog(collection)}>
                                            Edit
                                        </Button>
                                        <Button
                                            variant="destructive"
                                            size="sm"
                                            className="ml-2"
                                            onClick={() => handleDelete(collection.id)}
                                        >
                                            Delete
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>
            <CollectionDialog isOpen={dialogOpen} setIsOpen={setDialogOpen} value={formData}></CollectionDialog>
        </>)
        ;
}
