import React, {useEffect, useState} from 'react';
import {ArtefactTerm} from "@/app/api/SearchRestClient";
import {TreeView} from "@/components/Tree";
import ArtefactMetadata from "@/app/home/browse/components/ArtefactMetadata";
import {Card} from "@/components/ui/card";
import {ScrollArea} from "@/components/ui/scroll-area";


export default function ArtefactConcepts({concepts = [], selected, showGoToButtons}: {
    concepts: ArtefactTerm[],
    selected?: ArtefactTerm,
    showGoToButtons?: boolean,
}) {
    const [selectedConcept, setSelectedConcept] = useState<any>(selected);


    useEffect(() => {
        if (concepts.length > 0 && !selectedConcept) {
            setSelectedConcept(concepts[0]);
        }

    }, [concepts, selectedConcept]);

    return (
        <div className="flex gap-4">
            <Card className="w-1/3 p-1">
                <ScrollArea className="h-[60vh]">
                    <TreeView
                        concepts={concepts}
                        onSelect={setSelectedConcept}
                        selectedConcept={selectedConcept}
                    />
                </ScrollArea>
            </Card>
            <div className="w-2/3">
                {selectedConcept && <ArtefactMetadata data={selectedConcept} showGoToButtons={showGoToButtons}/>}
            </div>
        </div>
    );

}
