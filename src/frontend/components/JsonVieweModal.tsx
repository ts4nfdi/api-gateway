import {Button} from "@/components/ui/button";
import React, {useEffect, useState} from "react";
import TextInput from "@/components/TextInput";
import {Alert, AlertDescription} from "@/components/ui/alert";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import 'react-json-view-lite/dist/index.css';
import {Code} from "lucide-react";
import {Badge} from "@/components/ui/badge";
import httpClient from "@/lib/httpClient";
import JSONViewer from "@/components/JSONViewer";

export function JsonPrettyDisplay({url}: { url: string }) {

    const [inputValue, setInputValue] = useState(url || '');
    const [parsedJson, setParsedJson] = useState({});
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleInputChange = (event: any) => {
        setInputValue(event.target.value);
    };


    useEffect(() => {
        if (inputValue) {
            fetchURLJson();
        }
    }, []);

    const fetchURLJson = async () => {
        try {
            setLoading(true);
            const response = await httpClient.get(inputValue);
            const json = response.data;
            setParsedJson(json);
        } catch (e) {
            setError("Failed to fetch JSON from URL");
        }
        setLoading(false);
    };

    if (loading) {
        return <div>Loading...</div>
    }

    if (error) {
        return (
            <Alert variant="destructive">
                <AlertDescription>{error}</AlertDescription>
            </Alert>
        )
    }

    return (
        <div className="space-y-4">
            <div className={'flex space-x-4 '}>
                <TextInput
                    id="search"
                    placeholder="JSON URL"
                    isClearable={true}
                    value={inputValue}
                    onChange={handleInputChange}
                />
                <Button onClick={fetchURLJson}>
                    Fetch URL
                </Button>
            </div>
            <div className={"h-[60vh] overflow-auto"}>
                <JSONViewer data={parsedJson}/>
            </div>
        </div>
    )
        ;
}

export function JsonViewerDialog({isOpen, setIsOpen, url}: any) {
    return (
        <>
            <Dialog open={isOpen} onOpenChange={setIsOpen}>
                <DialogContent className="h-[80vh] max-w-[80vw]">
                    <DialogHeader>
                        <DialogTitle>
                            JSON Viewer
                        </DialogTitle>
                    </DialogHeader>
                    <div>
                        <JsonPrettyDisplay url={url}/>
                    </div>
                    <DialogFooter>
                        <Button variant="secondary" onClick={() => setIsOpen(false)}>Close</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

        </>
    )
}

function JsonIcon() {
    return <span className={'flex space-x-2 text-xs'} title={'View JSON'}>
        <span>JSON</span>
        <Code/>
    </span>
}

export default function JsonViewerModalButton({url, variant}: { url: string, variant: 'btn' | 'link' | 'badge' }) {
    const [dialogOpen, setDialogOpen] = useState(false);
    return (
        <div className={'flex space-x-4'}>
            {variant == 'link' && <Badge onClick={() => setDialogOpen(true)}><JsonIcon/></Badge>}
            {variant == 'btn' && <Button onClick={() => setDialogOpen(true)}><JsonIcon/></Button>}
            <JsonViewerDialog isOpen={dialogOpen} setIsOpen={setDialogOpen} url={url}/>
        </div>


    )
}