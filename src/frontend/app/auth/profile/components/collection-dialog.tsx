import {useEffect, useState} from "react";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Textarea} from "@/components/ui/textarea";
import {collectionRestClient} from "@/app/auth/lib/CollectionsRestClient";

export default function CollectionDialog({isOpen, setIsOpen, value, onSubmit}: { value: any, isOpen: boolean, setIsOpen: any, onSubmit: any }) {
    const [collection, setCollection] = useState(value || {
        id: "",
        label: "",
        description: "",
        terminologies: [],
    });

    useEffect(() => {
        if (value)
            setCollection(value);
    }, [value]);

    const handleChange = (e: any) => {
        const {name, value} = e.target;
        setCollection({
            ...collection,
            [name]: value,
        });
    };

    const handleSave = async () => {
        console.log("Saved collection:", collection);
        if(!(collection.terminologies instanceof Array))
            collection.terminologies = collection.terminologies.split(",")
        let res: any
        if (collection.id === ""){
            delete collection.id;
            res = await collectionRestClient.createCollection(collection);
        } else {
            res = await collectionRestClient.updateCollection(collection.id, collection);
        }


        if(res.status === 201 || res.status === 200){
            console.log("Collection saved/updated successfully");
            onSubmit(res.data, value.id === "");
            setIsOpen(false); // Close the dialog after saving
        } else {
            console.error("Failed to save/collection collection");
            //TODO add notification
        }
    };

    return (
        <Dialog open={isOpen} onOpenChange={setIsOpen}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Create a New Collection</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <Input
                        name="label"
                        placeholder="Enter collection label"
                        value={collection.label}
                        onChange={handleChange}
                    />
                    <Textarea
                        name="description"
                        placeholder="Enter collection description"
                        value={collection.description}
                        onChange={handleChange}
                    />
                    <Input
                        name="terminologies"
                        placeholder="Enter tags, separated by commas"
                        value={collection.terminologies}
                        onChange={handleChange}
                    />
                </div>
                <DialogFooter>
                    <Button variant="secondary" onClick={() => setIsOpen(false)}>Cancel</Button>
                    <Button onClick={handleSave}>Save</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
