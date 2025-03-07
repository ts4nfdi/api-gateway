import React from 'react';
import {Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle} from '@/components/ui/card';
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from '@/components/ui/table';
import {Badge} from '@/components/ui/badge';
import {StatusCheckResponse} from "@/app/status/lib/StatusRestClient";
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from "@/components/ui/accordion";

const getDomain = (url: string | URL): string => {
    try {
        return new URL(url).hostname.replace('www.', '').toString();
    } catch {
        return url.toString();
    }
};

const getResponseTimeColor = (time: number) => {
    if (time < 400) return "bg-green-600";
    if (time < 600) return "bg-yellow-600";
    return "bg-red-600";
};

export function APIResponseStatsDetails({checkResponse}: { checkResponse: StatusCheckResponse }) {
    return <Table className={'transition-all duration-300 ease-in-out overflow-hidden'}>
        <TableHeader>
            <TableRow>
                <TableHead>Database</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Response Time</TableHead>
            </TableRow>
        </TableHeader>
        <TableBody>
            {checkResponse.databases.map((db: any, index: any) => (
                <TableRow key={index}>
                    <TableCell className="font-medium" title={db.url}>{getDomain(db.url)}</TableCell>
                    <TableCell>
                        <Badge className={db.statusCode === 200 ? "bg-green-600" : "bg-red-600"}>
                            {db.statusCode === 200 ? "OK" : "Error"}
                        </Badge>
                    </TableCell>
                    <TableCell>
                        <Badge className={getResponseTimeColor(db.responseTime)}>
                            {db.responseTime}ms
                        </Badge>
                    </TableCell>
                </TableRow>
            ))}
        </TableBody>
    </Table>
}

export function APIResponseStats(checkResponse: StatusCheckResponse) {
    return <div className="container space-y-4">
        <Card className="w-full">
            <CardHeader>
                <CardTitle>Status of <span className={'text-blue-600'}>{checkResponse.endpoint}</span></CardTitle>
                <CardDescription>
                    Displaying response metrics across {checkResponse.databases.length} database endpoints
                </CardDescription>
            </CardHeader>
            <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                    <Card>
                        <CardHeader className="py-2">
                            <CardTitle className="text-sm font-medium">Total Results</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">{checkResponse.totalResults}</div>
                        </CardContent>
                    </Card>
                    <Card>
                        <CardHeader className="py-2">
                            <CardTitle className="text-sm font-medium">Total Response Time</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">{checkResponse.totalResponseTime}ms</div>
                        </CardContent>
                    </Card>
                    <Card>
                        <CardHeader className="py-2">
                            <CardTitle className="text-sm font-medium">Average data filled</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">
                                {checkResponse.avgPercentageFilled.toFixed(2)}%
                            </div>
                        </CardContent>
                    </Card>
                </div>

                <Accordion type="single" collapsible>
                    <AccordionItem value="item-1">
                        <AccordionTrigger>See details</AccordionTrigger>
                        <AccordionContent>
                            <APIResponseStatsDetails checkResponse={checkResponse}/>
                        </AccordionContent>
                    </AccordionItem>
                </Accordion>

            </CardContent>
            <CardFooter className="text-sm text-gray-500">
                Last updated: {new Date().toLocaleString()}
            </CardFooter>
        </Card>
    </div>
}
