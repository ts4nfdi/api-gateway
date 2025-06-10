import {RestApplicationClient, RestResponse} from "@/lib/RestClient";
import httpClient from "@/lib/httpClient";
import {useEffect, useState} from "react";
import {CollectionResponse} from "@/app/api/CollectionsRestClient";

export  type Artefact = {
    iri: string;
    label: string;
    descriptions: string;
    backend_type: string;
    source_name: string;
    source: string;
    short_form: string;
}


export type ResponseConfig = {
    totalResponseTime: number;
}

export class ArtefactsRestClient extends RestApplicationClient {

    getAllArtefacts(database: string | null, collectionId: string | undefined): RestResponse<Artefact> {
        const params: any = {};

        if (database && database.length > 0) {
            params['database'] = database
        }
        if (collectionId && collectionId.length > 0) {
            params["collectionId"] = collectionId
        }

        params["showResponseConfiguration"] = true;

        return this.httpClient.request({method: 'GET', url: '/artefacts', params})
    }
}


export const useArtefacts = (sources: string[] = [], collection: CollectionResponse) => {
    const apiUrl = `${process.env.API_GATEWAY_URL}/artefacts`;
    const [items, setItems] = useState<Artefact[]>([]);
    const [responseConfig, setResponseConfig] = useState<ResponseConfig>({totalResponseTime: -1});
    const [loading, setLoading] = useState(true);

    const fetchArtefacts = async () => {
        setLoading(true);
        artefactsRestClient.getAllArtefacts(sources.join(','), collection.id)
            .then((response: any) => {
                if (response.status !== 200) {
                    throw new Error(`Error fetching artefacts: ${response.statusText}`);
                }
                let data: Artefact[] = response.data.collection;

                if (collection?.terminologies && !collection.id) {
                    data = data.filter(x => collection.terminologies.some(t => t.label === x.short_form && t.source === x.source))
                }

                setItems(data)

                setResponseConfig(response.data.responseConfig);
            }).catch((error) => {
            console.error("Error fetching artefacts:", error);
        }).finally(() => {
            setLoading(false);
        })
    }

    useEffect(() => {
        fetchArtefacts();
    }, [apiUrl, sources, collection]);

    return {items, loading, responseConfig, fetchArtefacts};
}

export const artefactsRestClient = new ArtefactsRestClient(httpClient)