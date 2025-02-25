import httpClient from "@/lib/httpClient";
import {RestApplicationClient, RestResponse, uriEncoding} from "@/lib/RestClient";


interface CreateCollectionRequest {
    label: string,
    description: string,
    terminologies: string[],
}

export interface CollectionResponse {
    id: string,
    label: string,
    description: string,
    terminologies: string[],
}

interface UpdateCollectionRequest {
    label: string,
    description: string,
    terminologies: string[],
}

export class CollectionRestClient extends RestApplicationClient {

    getAllCollections(): RestResponse<CollectionResponse[]> {
        return this.httpClient.request({method: 'GET', url: '/users/collections/'})
    }

    deleteCollection(id: string): RestResponse<any> {
        return this.httpClient.request({method: "DELETE", url: 'users/collections/' + id});
    }

    createCollection(request: CreateCollectionRequest): RestResponse<CollectionResponse> {
        return this.httpClient.request({method: "POST", url: 'users/collections/', data: request});
    }

    updateCollection(id: string, request: UpdateCollectionRequest): RestResponse<CollectionResponse> {
        return this.httpClient.request({method: "PUT", url: 'users/collections/' + id, data: request});
    }

}

export const collectionRestClient = new CollectionRestClient(httpClient)
