import {RestApplicationClient, RestResponse} from "@/lib/RestClient";
import httpClient from "@/lib/httpClient";

export interface ExampleResponse {
    data: {
        [key: string]: string;
    };
    metadata: {
        [key: string]: string;
    };
    search: {
        [key: string]: string;
    };
}


interface Database {
    url: string;
    statusCode: number;
    responseTime: number;
}


export interface StatusCheckResponse {
    endpoint: string;
    totalResponseTime: number;
    totalResults: number;
    databases: Database[];
    avgPercentageCommon: number;
    avgPercentageFilled: number;
}

export class StatusRestClient extends RestApplicationClient {

    getAllExamples(): RestResponse<ExampleResponse> {
        return this.httpClient.request({method: 'GET', url: '/status/examples'})
    }

    checkStatus(endpoint: string): RestResponse<StatusCheckResponse> {
        return this.httpClient.request({method: 'GET', url: '/status/check', params: {endpoint: endpoint}})
    }
}

export const statusRestClient = new StatusRestClient(httpClient)
