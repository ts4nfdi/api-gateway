'use client';
import {Card, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import React, {useState} from "react";
import SmallLink from "@/components/SmallLink";
import {Badge} from "@/components/ui/badge";
import DialogWrapper from "@/components/Dialog";
import TermViewer from "@/components/TermViewer";

export const BrowseCard = ({artefact}: any) => {
    const [dialogOpen, setDialogOpen] = useState(false);

    if (!artefact) return null;

    const title = `${artefact.label} (${artefact.short_form})`

    return (
        <>
            <Card className="hover:shadow-lg transition-shadow flex flex-col"
                  onClick={() => setDialogOpen(true)}>
                <CardHeader className={'flex-grow'}>
                    <div className="space-y-1">
                        <CardTitle className="text-xl font-bold tracking-tight">
                            {title}
                        </CardTitle>
                        <div className="flex flex-wrap gap-1">
                            <SmallLink href={artefact.iri || 'No link'}> {artefact.iri}</SmallLink>
                        </div>
                        <div className="text-sm text-gray-600">
                            <p className="line-clamp-3 overflow-hidden text-ellipsis">{artefact.descriptions}</p>
                        </div>
                    </div>
                </CardHeader>
                <CardFooter>
                    <Badge className="text-xs" title={artefact.source}>{artefact.source_name}</Badge>
                </CardFooter>
            </Card>
            <DialogWrapper title={title} isOpen={dialogOpen} setIsOpen={setDialogOpen}>
                <TermViewer data={artefact}/>
            </DialogWrapper>
        </>
    );
};
