import React, {useState} from 'react';
import {Badge} from "@/components/ui/badge";
import {ChevronRight} from "lucide-react";
import DialogWrapper from "@/components/Dialog";
import TermViewer from "@/components/TermViewer";

export function AutoCompleteResult({suggestion}: any) {
    const [dialogOpen, setDialogOpen] = useState(false);

    const getBadgeVariant = (color: string) => {
        switch (color) {
            case 'primary':
                return 'bg-blue-500 hover:bg-blue-600';
            case 'success':
                return 'bg-green-500 hover:bg-green-600';
            case 'danger':
                return 'bg-red-500 hover:bg-red-600';
            case 'warning':
                return 'bg-yellow-500 hover:bg-yellow-600';
            default:
                return 'bg-gray-500 hover:bg-gray-600';
        }
    };

    const getOntologyAcronym = (ontology: string, source: string) => {
        let acronym = ontology?.split('/').pop();
        if (acronym) {
            acronym = acronym.split('#')[0];
        }
        if (acronym?.trim() === '') {
            acronym = ontology;
        }
        if (acronym?.trim() === '') {
            acronym = source;
        }
        
        return acronym;
    }

    return (
        <>
            <div className="flex flex-wrap justify-between items-center w-full" onClick={() => setDialogOpen(true)}>
                <div className="flex-grow max-w-[50%]">
                    <div>{suggestion.label}</div>

                </div>
                <div className="flex flex-wrap gap-2">
                    <Badge className={getBadgeVariant('primary')}
                           title={`${suggestion.backend_type}(${suggestion.source})`}>
                        {suggestion.source_name.toUpperCase()}
                    </Badge>

                    <ChevronRight className="mx-1 h-4 w-4 text-gray-400"/>

                    <Badge className={getBadgeVariant('success')}>
                        {getOntologyAcronym(suggestion.ontology)}
                    </Badge>

                    <ChevronRight className="mx-1 h-4 w-4 text-gray-400"/>

                    <Badge className={getBadgeVariant('danger')} title={suggestion.iri}>
                        {suggestion.short_form}
                    </Badge>
                </div>
            </div>
             <DialogWrapper title={suggestion.label} isOpen={dialogOpen} setIsOpen={setDialogOpen}>
                <TermViewer data={suggestion}/>
            </DialogWrapper>
        </>
    );
}

export default AutoCompleteResult;
