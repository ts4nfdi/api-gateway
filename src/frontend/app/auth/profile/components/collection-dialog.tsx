import {useEffect, useState} from "react";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Textarea} from "@/components/ui/textarea";

export default function CollectionDialog({isOpen, setIsOpen, value}: { value: any, isOpen: boolean, setIsOpen: any }) {
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
        /*const {name, value} = e.target;
        setCollection({
            ...collection,
            [name]: value,
        });*/
    };

    const handleSave = () => {
        console.log("Saved collection:", collection);
        setIsOpen(false); // Close the dialog after saving
        // Add logic to handle saving the collection
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
