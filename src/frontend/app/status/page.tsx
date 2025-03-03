'use client';
import React, {useEffect, useState} from "react";
import {StatusCheckResponse, statusRestClient} from "@/app/status/lib/StatusRestClient";
import {APIResponseStats} from "@/components/ResponseStats";
import {Loading} from "@/components/Loading";
import {Card, CardContent} from "@/components/ui/card";
import {TopNav} from "@/app/TopNav";


function APIExampleCheckResult({url}: any) {
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<any>(null);
    const [exampleCheck, setExampleCheck] = useState<StatusCheckResponse>({} as StatusCheckResponse);


    const fetchExample = async (url: string) => {
        setIsLoading(true);
        statusRestClient.checkStatus(url).then((res: any) => {
            if (res.status !== 200) {
                setError(res);
                return;
            }
            setExampleCheck(res.data);
        })
            .catch(err => setError(err))
            .finally(() => setIsLoading(false));
    }

    useEffect(() => {
        setExampleCheck({} as StatusCheckResponse);
        setIsLoading(true);
        if (url !== null) {
            fetchExample(url)
        }
    }, [url]);

    if (isLoading) return <>
        <div>
            <h1 className={'text-lg'}>Checking status of <span className={'text-blue-600'}>{url}</span></h1>
            <Loading/>
        </div>
    </>
    if (error) return <Card className={'text-red-600'}><CardContent
        className={'p-2'}> Error: {error.message} </CardContent></Card>;
    if (Object.keys(exampleCheck).length == 0) return null;


    return <APIResponseStats endpoint={exampleCheck.endpoint}
                             databases={exampleCheck.databases}
                             totalResults={exampleCheck.totalResults}
                             totalResponseTime={exampleCheck.totalResponseTime}
                             avgPercentageCommon={exampleCheck.avgPercentageCommon}
                             avgPercentageFilled={exampleCheck.avgPercentageFilled}/>
}

export default function Status() {
    return (<>
            <TopNav/>
            <div className={'container mx-auto p-4 space-y-2'}>
                <h1 className={'text-lg'}>Status</h1>
                <APIExampleCheckResult url={'/artefacts'}/>
                <APIExampleCheckResult url={'/search?query=concept'}/>
                <APIExampleCheckResult url={'/artefacts/NCBITAXON'}/>
                <APIExampleCheckResult url={'/artefacts/NCBITAXON/terms?uri=http://purl.obolibrary.org/obo/NCBITaxon_2'}/>
            </div>
        </>
    );
}