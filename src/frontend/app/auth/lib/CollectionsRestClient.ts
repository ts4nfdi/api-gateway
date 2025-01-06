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

    deleteCollection() {

    }

    createCollection(request: CreateCollectionRequest): RestResponse<CollectionResponse> {
        return this.httpClient.request({method: "POST", url: uriEncoding('api/users'), data: request});
    }

    updateCollection(id: string, request: UpdateCollectionRequest): RestResponse<CollectionResponse> {
        return this.httpClient.request({method: "PUT", url: uriEncoding('api/users'), data: request});
    }

}

export const collectionRestClient = new CollectionRestClient(httpClient)
