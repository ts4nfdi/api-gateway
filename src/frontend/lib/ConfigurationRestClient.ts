import {RestApplicationClient, RestResponse} from "@/lib/RestClient";
import httpClient from "@/lib/httpClient";

export interface DatabaseConfig {
    type: string,
    name: string,
    url: string,
    artefactsUrl: string,
    searchUrl: string,
}

export interface ServiceConfig {
    name: string,
    endpoints: Array<any>,
}

export class ConfigurationRestClient extends RestApplicationClient {
    getAllDatabases(): RestResponse<DatabaseConfig[]> {
        return this.httpClient.request({method: 'GET', url: '/config/databases'})
    }

    getAllServices(): RestResponse<ServiceConfig[]> {
        return this.httpClient.request({method: 'GET', url: '/config/services'})
    }

    getMetadata(type: string): RestResponse<any> {
        return this.httpClient.request({method: 'GET', url: `/config/metadata/${type}`})
    }
}

export const configurationRestClient = new ConfigurationRestClient(httpClient)
