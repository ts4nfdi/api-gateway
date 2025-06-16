import httpClient from "@/lib/httpClient";
import {RestApplicationClient, RestResponse} from "@/lib/RestClient";
import {useEffect, useState} from "react";

export interface CollectionTerminology {
    label: string,
    source: string,
    uri: string,
}

interface CollectionCollaborator {
    username: string,
    role: string,
}

interface CreateCollectionRequest {
    label: string,
    description: string,
    terminologies: CollectionTerminology[],
    isPublic: boolean,
    collaborators: CollectionCollaborator[],
}

export interface CollectionResponse {
    id?: string,
    label: string,
    description: string,
    terminologies: CollectionTerminology[],
    collaborators: CollectionCollaborator[],
    isPublic: boolean,
}

export  interface UpdateCollectionRequest {
    label: string,
    description: string,
    terminologies: CollectionTerminology[],
    collaborators: CollectionCollaborator[],
    isPublic: boolean,
}

export class CollectionRestClient extends RestApplicationClient {

    getAllCollections(): RestResponse<CollectionResponse[]> {
        return this.httpClient.request({method: 'GET', url: '/collections/'})
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


export function useCollections() {
    const [collections, setCollections] = useState<CollectionResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchCollections = async () => {
        setLoading(true);
        try {
            const response: any = await collectionRestClient.getAllCollections();
            setCollections(response.data);
        } catch (err: any) {
            setError(err.message || "Failed to fetch collections");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCollections();
    }, []);

    return {collections, setCollections, loading, error, fetchCollections};
}