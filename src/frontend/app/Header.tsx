import React from 'react';
import {Button} from '@/components/ui/button';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '@/components/ui/card';
import {ExternalLinkIcon} from "lucide-react";

const PageHeader = () => {
    const isLoggedIn = false; //TODO Replace with your auth logic

    return (
        <div>
            <header className="border-b">
                <div className="flex h-16 items-center px-4 max-w-7xl mx-auto">
                    <div className="flex-1 flex items-center gap-2">
                        <img src={'/api-gateway/logo.png'} alt="Icon" className="w-6 h-6"/>
                        <h1 className="text-xl font-bold">TSNFDI API Gateway</h1>
                    </div>

                    {isLoggedIn ? (
                        <a href="/profile" className="text-blue-600 hover:underline">
                            My Profile
                        </a>
                    ) : (
                        <Button className="bg-blue-600">Login</Button>
                    )}
                </div>
            </header>

            <div className="bg-gradient-to-r from-blue-600 to-indigo-600">
                <Card className="max-w-7xl mx-auto bg-transparent border-0 shadow-none text-white">
                    <CardHeader className="py-16 w-3/4">
                        <CardTitle className="text-4xl font-bold">Welcome to TSNFDI API Gateway</CardTitle>
                        <CardDescription className="text-xl text-white/80 mt-5">
                            The TS4NFDI Federated Service is an advanced, dynamic solution designed to perform federated
                            calls across multiple Terminology Services (TS) within NFDI. It is particularly tailored for
                            environments where integration and aggregation of diverse data sources are essential. The
                            service offers search capabilities, enabling users to refine search results based on
                            specific criteria, and supports responses in both JSON and JSON-LD formats.
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="w-2/4">
                        <a
                            href={process.env.API_GATEWAY_URL}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="block"
                        >
                            <Card className="transition-colors hover:bg-muted/90">
                                <CardContent className="flex items-center justify-between p-6">
                                    <div className="space-y-1">
                                        <p className="text-sm text-muted-foreground">
                                            Access the API endpoint for documentation and usage
                                        </p>
                                    </div>
                                    <ExternalLinkIcon className="h-5 w-5"/>
                                </CardContent>
                            </Card>
                        </a>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};

export default PageHeader;
