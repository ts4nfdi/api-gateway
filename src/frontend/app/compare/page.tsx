'use client';

import React, {useEffect, useState} from "react";
import httpClient from "@/lib/httpClient";
import {Alert, AlertDescription} from "@/components/ui/alert";
import TextInput from "@/components/TextInput";
import {Button} from "@/components/ui/button";

import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Loading} from "@/components/Loading";
import {Badge} from "@/components/ui/badge";
import Link from "next/link";
import {APIResponseStats} from "@/components/ResponseStats";
import {TopNav} from "@/app/TopNav";
import {ExampleResponse, StatusCheckResponse, statusRestClient} from "@/app/api/StatusRestClient";
import ArtefactMetadata from "@/app/home/browse/components/ArtefactMetadata";

const calculateCommonKeysPercentage = (data: any) => {
    if (!data || !data.originalResponse) {
        return 0;
    }

    const originalResponseKeys = Object.keys(data.originalResponse);

    const mainDataKeys = Object.keys(data).filter(key => key !== "originalResponse");

    const commonKeys = originalResponseKeys.filter(key => mainDataKeys.includes(key));

    const emptyKeys = mainDataKeys.filter(key =>
        data[key] === null || data[key] === undefined ||
        data[key] === "" ||
        (Array.isArray(data[key]) && data[key].length === 0)
    );


    const percentageFilled = (1 - (emptyKeys.length / mainDataKeys.length)) * 100;
    const percentageCommon = (commonKeys.length / originalResponseKeys.length) * 100;

    return {
        percentageCommon: percentageCommon.toFixed(2),
        percentageFilled: percentageFilled.toFixed(2),
        commonKeys,
        emptyKeys,
        totalOriginalKeys: originalResponseKeys.length,
        totalMainKeys: mainDataKeys.length,
    };
};


function APIExamples({exampleSelected}: any) {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [examples, setExamples] = useState<ExampleResponse>({} as ExampleResponse);
    const fetchExampleStats = async () => {
        setIsLoading(true);
        const res: any = await statusRestClient.getAllExamples();
        if (res.status === 200) {
            setExamples(res.data);
        } else {
            setError(res);
        }
        setIsLoading(false);
    };

    const exampleClicked = (url: string) => {
        if (exampleSelected)
            exampleSelected(url);
    }

    useEffect(() => {
        fetchExampleStats()
    }, []);

    if (isLoading) return <Loading/>;
    if (error) return <div>Error: {error}</div>;
    if (Object.keys(examples).length == 0) return null;

    return (
        <div className="flex">
            <div className={'flex-1'}>
                Examples metadata endpoints:
                <div className="">
                    {Object.entries(examples.metadata).map(([label, url], index) => (
                        <div key={index} className="text-blue-500 cursor-pointer" onClick={() => exampleClicked(url)}>
                            {label}
                        </div>))}
                </div>
            </div>
            <div className={'flex-1'}>
                Examples data endpoints:
                <div className="">
                    {Object.entries(examples.data).map(([label, url], index) => (
                        <div key={index} className="text-blue-500 cursor-pointer" onClick={() => exampleClicked(url)}>
                            {label}
                        </div>))}
                </div>
            </div>
            <div className={'flex-1'}>
                Examples search endpoints:
                <div className="">
                    {Object.entries(examples.search).map(([label, url], index) => (
                        <div key={index} className="text-blue-500 cursor-pointer" onClick={() => exampleClicked(url)}>
                            {label}
                        </div>))}
                </div>
            </div>
        </div>)
}

const DataTable = ({data}: any) => {
    // Extract top-level data excluding originalResponse
    const mainData = Object.entries(data).filter(([key]) => key !== "originalResponse");
    // Extract originalResponse data
    const originalResponseData = data.originalResponse ? Object.entries(data.originalResponse) : [];
    const commonKeysInfo: any = calculateCommonKeysPercentage(data);

    return (
        <Card className="w-full">
            <CardHeader>
                <CardTitle className={'flex items-center'}>
                    <Button variant={'link'}> <Link href={data.iri || 'warning'}> {data.iri}</Link></Button>/
                    <Badge className={'mx-2'}>{data.backend_type}</Badge>/
                    <Badge className={'mx-2'}>{data.source_name}</Badge>
                </CardTitle>
            </CardHeader>
            <CardContent>
                <div className="mb-4 p-3 bg-blue-50 rounded border border-blue-200">
                    <p className="font-medium">Keys filled: {commonKeysInfo.percentageFilled}%</p>
                    <p className="text-sm text-gray-600"
                       title={`Common keys: ${commonKeysInfo.commonKeys.join(', ')} and empty keys: ${commonKeysInfo.emptyKeys.join(', ')}`}>
                        {commonKeysInfo.commonKeys.length} common keys out
                        of {commonKeysInfo.totalOriginalKeys} original response keys
                    </p>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {/* Main Data Table */}
                    <div className="border rounded-lg overflow-hidden">
                        <div className="bg-gray-100 p-3 font-semibold border-b">Aggregated Data</div>
                        <div className="overflow-auto max-h-96">
                            <ArtefactMetadata showGoToButtons={false} data={Object.fromEntries(mainData || [])}/>
                        </div>
                    </div>

                    {/* Original Response Table */}
                    <div className="border rounded-lg overflow-hidden">
                        <div className="bg-gray-100 p-3 font-semibold border-b">Original Data</div>
                        <div className="overflow-auto max-h-96">
                            <ArtefactMetadata showGoToButtons={false} data={Object.fromEntries(originalResponseData || [])}/>
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
};

function ShowResults({parsedJson, url}: any) {
    if (!parsedJson || !parsedJson.collection) {
        return null;
    }

    const responseStats: StatusCheckResponse = parsedJson.responseConfig;

    return (<>
        <APIResponseStats endpoint={url}
                          databases={responseStats.databases}
                          totalResults={responseStats.totalResults}
                          totalResponseTime={responseStats.totalResponseTime}
                          avgPercentageCommon={responseStats.avgPercentageCommon}
                          avgPercentageFilled={responseStats.avgPercentageFilled}/>
        <div className={'my-2'}>
            {parsedJson.collection && <span>Results:</span>}
            <div className={'space-y-2'}>
                {parsedJson.collection && parsedJson.collection.map((data: any, index: number) => (
                    <DataTable key={index} data={data}/>
                ))}
            </div>
        </div>
    </>)
}

export default function CompareSources() {
    const [inputValue, setInputValue] = useState('');
    const [parsedJson, setParsedJson] = useState({});
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleInputChange = (event: any) => {
        setInputValue(event.target.value);
    };

    const fetchURLJson = async (url: string) => {
        try {
            setLoading(true);
            const response = await httpClient.get(url);
            const json = response.data;
            setParsedJson(json);
        } catch (e) {
            setError("Failed to fetch JSON from URL");
        }
        setLoading(false);
    };

    const fetchExample = (url: string) => {
        setInputValue(url);
        fetchURLJson(url);
    }

    if (error) {
        return (
            <Alert variant="destructive">
                <AlertDescription>{error}</AlertDescription>
            </Alert>
        )
    }

    return (<>
            <TopNav/>
            <div className="space-y-4 container mx-auto my-5">
                <div className={'flex space-x-4 bg-white p-2 rounded-lg'}>
                    <TextInput
                        id="search"
                        placeholder="JSON URL"
                        isClearable={true}
                        value={inputValue}
                        onChange={handleInputChange}
                    />
                    <Button onClick={() => fetchURLJson(inputValue)}>
                        Fetch URL
                    </Button>
                </div>

                <APIExamples exampleSelected={fetchExample}/>

                <div className={"w-full"}>
                    {loading && <Loading/>}
                    {error && <Alert variant="destructive">
                        <AlertDescription>{error}</AlertDescription>
                    </Alert>
                    }
                    <div className="space-y-4">
                        <ShowResults parsedJson={parsedJson} url={inputValue}/>
                    </div>
                </div>
            </div>
        </>
    )
}
