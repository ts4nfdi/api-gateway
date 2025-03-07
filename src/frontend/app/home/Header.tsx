'use client'
import React from 'react';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '@/components/ui/card';
import {ExternalLinkIcon} from "lucide-react";
import {TopNav} from "@/app/TopNav";
import DataMergeGraph from "@/app/home/Animation";

export const Banner = () => {

    return (
        <div className="bg-gradient-to-r from-blue-600 to-indigo-600">
            <Card className="container mx-auto bg-transparent border-0 shadow-none text-white">
                <CardHeader className="py-16 space-y-2">
                    <CardTitle className="text-5xl font-bold max-w-1xl">Welcome to TSNFDI API Gateway</CardTitle>
                    <CardDescription className="flex mt-5 flex-wrap">
                        <div className={'text-xl text-white/80 w-1/2 space-y-4 flex flex-col'}>
                            <p className={'text-justify'}>
                                The TS4NFDI Federated Service is an advanced, dynamic solution designed to perform
                                federated
                                calls across multiple Terminology Services (TS) within NFDI. It is particularly tailored
                                for
                                environments where integration and aggregation of diverse data sources are essential.
                                The
                                service offers search capabilities, enabling users to refine search results based on
                                specific criteria, and supports responses in both JSON and JSON-LD formats.
                            </p>
                            <a href={process.env.API_GATEWAY_URL} target="_blank" className="flex">
                                <Card className="transition-colors hover:bg-muted/90">
                                    <CardContent className="flex space-x-2 items-center justify-between p-6">
                                        <p className="text-sm text-muted-foreground">
                                            Access the API endpoint for the documentation and usage
                                        </p>
                                        <ExternalLinkIcon className="h-5 w-5"/>
                                    </CardContent>
                                </Card>
                            </a>
                        </div>
                        <div className={'p-3'}>
                            <DataMergeGraph width={500} height={400}/>
                        </div>

                    </CardDescription>
                </CardHeader>
            </Card>
        </div>
    )
}


export default function Header() {
    return (
        <div>
            <TopNav/>
            <Banner/>
        </div>
    )
}
