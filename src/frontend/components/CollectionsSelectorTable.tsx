import {CollectionResponse} from "@/app/api/CollectionsRestClient";
import React, {useEffect} from "react";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {LockIcon} from "lucide-react";
import UUIDDisplay from "@/components/CopyId";
import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";

export default function CollectionsSelectorTable({selected, collections, handleDelete, handleSelection, handleEdit}: {
    selected?: CollectionResponse;
    collections: CollectionResponse[];
    handleDelete?: (id: string) => void;
    handleEdit?: (collection: CollectionResponse) => void;
    handleSelection?: (collection: CollectionResponse) => void;
}) {
    const [searchQuery, setSearchQuery] = React.useState("");
    const [filteredCollections, setFilteredCollections] = React.useState<CollectionResponse[]>(collections);

    useEffect(() => {
        if (!searchQuery) {
            return;
        }
        let filtered = collections.filter((collection) => {
            return collection.label.toLowerCase().includes(searchQuery.toLowerCase()) ||
                (collection.description && collection.description.toLowerCase().includes(searchQuery.toLowerCase()));
        });
        setFilteredCollections(filtered);
    }, [searchQuery, collections]);

    return <div className={"space-y-2 flex flex-col  max-h-[70vh]"}>
        <Input
            placeholder="Search by label or description"
            value={searchQuery}
            onChange={(e: any) => setSearchQuery(e.target.value)}
        />
        <Table className="text-xs">
            <TableHeader>
                <TableRow>
                    <TableHead className="w-[200px]">Collection</TableHead>
                    <TableHead className="w-[300px]">Description</TableHead>
                    <TableHead className="w-[250px]">Tags</TableHead>
                    <TableHead className="w-[200px]">Collaborators</TableHead>
                    <TableHead className="w-[150px] text-right">Actions</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {filteredCollections.map((collection) => (
                    <TableRow key={collection.id} className="hover:bg-muted/50">
                        <TableCell className="py-3">
                            <div className="space-y-1">
                                <div className="flex items-center gap-2">
                                    <span className="font-medium text-sm">{collection.label}</span>
                                    {!collection.isPublic && (
                                        <LockIcon className="h-3 w-3 text-muted-foreground"/>
                                    )}
                                </div>
                                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                                    <UUIDDisplay value={collection.id ?? ''}/>
                                </div>
                            </div>
                        </TableCell>

                        <TableCell className="py-3">
                            <p className="text-sm text-muted-foreground line-clamp-2">
                                {collection.description || "No description"}
                            </p>
                        </TableCell>

                        <TableCell className="py-3">
                            <div className="flex flex-wrap gap-1 max-w-[250px]">
                                {collection.terminologies.length > 0 ? (
                                    collection.terminologies.slice(0, 3).map((tag, index) => (
                                        <Badge key={index} variant="secondary" className="text-xs">
                                            {tag.label}
                                            <span
                                                className="ml-1 text-muted-foreground">({tag.source})</span>
                                        </Badge>
                                    ))
                                ) : (
                                    <span className="text-xs text-muted-foreground">No tags</span>
                                )}
                                {collection.terminologies.length > 3 && (
                                    <Badge variant="outline" className="text-xs">
                                        +{collection.terminologies.length - 3} more
                                    </Badge>
                                )}
                            </div>
                        </TableCell>

                        <TableCell className="py-3">
                            <div className="flex flex-wrap gap-1 max-w-[200px]">
                                {collection.collaborators.length > 0 ? (
                                    collection.collaborators.slice(0, 2).map((contributor, index) => (
                                        <Badge key={index} variant="outline" className="text-xs">
                                            {contributor.username}
                                        </Badge>
                                    ))
                                ) : (
                                    <span className="text-xs text-muted-foreground">No collaborators</span>
                                )}
                                {collection.collaborators.length > 2 && (
                                    <Badge variant="outline" className="text-xs">
                                        +{collection.collaborators.length - 2} more
                                    </Badge>
                                )}
                            </div>
                        </TableCell>

                        <TableCell className="py-3 text-right">
                            <div className="flex justify-end gap-2">
                                {handleEdit && (
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        className="h-8 px-3"
                                        onClick={() => handleEdit(collection)}
                                    >
                                        Edit
                                    </Button>
                                )}
                                {handleSelection && (
                                    <Button
                                        variant={selected?.id === collection.id ? "default" : "ghost"}
                                        size="sm"
                                        className="h-8 px-3"
                                        onClick={() => handleSelection(collection)}
                                    >
                                        {selected?.id === collection.id ? "Selected" : "Select"}
                                    </Button>
                                )}
                                {handleDelete && collection.id && collection.id !== "" && (
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        className="h-8 px-3 text-destructive hover:text-destructive"
                                        onClick={() => handleDelete(collection.id as string)}
                                    >
                                        Delete
                                    </Button>
                                )}
                            </div>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    </div>
}
