import {Card, CardHeader, CardTitle} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";
import React from "react";

export const BrowseCard = ({title, tags, onTagClick, sourceUrl}: any) => {
    return (
        <Card className="hover:shadow-lg transition-shadow"
              onClick={() => window.open(sourceUrl, '_blank', 'noopener,noreferrer')}>
            <CardHeader>
                <div className="space-y-3">
                    <CardTitle className="text-xl font-bold tracking-tight">
                        {title}
                    </CardTitle>
                    <div className="flex flex-wrap gap-2">
                        {tags.map((tag: any, index: number) => (
                            <Badge
                                key={index}
                                className="bg-blue-600 hover:bg-blue-600/80 transition-colors cursor-pointer"
                                onClick={() => onTagClick?.(tag)}
                            >
                                {tag}
                            </Badge>
                        ))}
                    </div>
                </div>
            </CardHeader>
        </Card>
    );
};
