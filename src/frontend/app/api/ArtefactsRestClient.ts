import {RestApplicationClient, RestResponse} from "@/lib/RestClient";
import httpClient from "@/lib/httpClient";
import {useEffect, useState} from "react";
import {CollectionResponse} from "@/app/api/CollectionsRestClient";
import {ArtefactTerm} from "@/app/api/SearchRestClient";

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

    getArtefactConceptRoots(acronym: string, database: string): RestResponse<ArtefactTerm[]> {
        const params: any = {};
        if (database && database.length > 0) {
            params['database'] = database
        }

        return this.httpClient.request({method: 'GET', url: `/artefacts/${acronym}/resources/concepts/roots`, params});
    }

    getArtefactConceptsChildren(acronym: string, uri: string, database: string): RestResponse<ArtefactTerm[]> {
        const params: any = {};
        if (database && database.length > 0) {
            params['database'] = database
        }
        if (uri && uri.length > 0) {
            params['uri'] = uri
        } else {
            throw new Error("URI is required to fetch concept children");
        }
        return this.httpClient.request({
            method: 'GET',
            url: `/artefacts/${acronym}/resources/concepts/children`,
            params
        });
    }

    getArtefactConceptTree(acronym: string, uri: string, database: string): RestResponse<ArtefactTerm[]> {
        const params: any = {};
        if (database && database.length > 0) {
            params['database'] = database
        }
        if (uri && uri.length > 0) {
            params['uri'] = uri
        } else {
            throw new Error("URI is required to fetch concept tree");
        }
        return this.httpClient.request({method: 'GET', url: `/artefacts/${acronym}/resources/concepts/tree`, params});
    }
}


export const useArtefacts = (sources: string[] = [], collection: CollectionResponse) => {
    const apiUrl = `${process.env.API_GATEWAY_URL}/artefacts`;
    const [items, setItems] = useState<Artefact[]>([]);
    const [responseConfig, setResponseConfig] = useState<ResponseConfig>({totalResponseTime: -1});
    const [loading, setLoading] = useState(true);

    const fetchArtefacts = async () => {
        setLoading(true);
        artefactsRestClient.getAllArtefacts(sources.join(','), collection?.id)
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

export const useArtefactConceptRoots = (acronym: string, databaseType: string) => {
    const [roots, setRoots] = useState<ArtefactTerm[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);


    const fetchRoots = async () => {
        setLoading(true);
        artefactsRestClient.getArtefactConceptRoots(acronym, databaseType).then((response: any) => {
            if (response.status !== 200) {
                throw new Error(`Error fetching concept roots: ${response.statusText}`);
            }
            setRoots(response.data || []);
        }).catch((error) => {
            console.error("Error fetching concept roots:", error);
            setError(`Failed to fetch concept roots: ${error.message}`);
        }).finally(() => {
            setLoading(false);
        })
    }

    useEffect(() => {
        fetchRoots();
    }, [acronym]);


    if (!acronym || !databaseType) {
        return {roots: [], loading, fetchRoots: () => Promise.resolve()};
    }

    return {roots, loading, error, fetchRoots};
}

export const useArtefactConceptChildren = (concept: ArtefactTerm) => {

    const acronym = concept.ontology
    const uri = concept.iri
    const databaseType = concept.source_name
    const [children, setChildren] = useState<ArtefactTerm[]>(concept.children || []);
    const [loading, setLoading] = useState(!(concept.children && concept.children.length > 0));
    const [error, setError] = useState<string | null>(null);

    const fetchChildren = async () => {
        setLoading(true);
        artefactsRestClient.getArtefactConceptsChildren(acronym, uri, databaseType).then((response: any) => {
            if (response.status !== 200) {
                throw new Error(`Error fetching concept children: ${response.statusText}`);
            }
            setChildren(response.data.member || []);
        }).catch((error) => {
            console.error("Error fetching concept children:", error);
            setError(`Failed to fetch concept children: ${error.message}`);
        }).finally(() => {
            setLoading(false);
        })
    }

    useEffect(() => {
        if (acronym && uri && databaseType && concept.children && concept.children.length === 0 && concept.hasChildren === true) {
            fetchChildren();
        }
    }, [acronym, uri, databaseType]);

    return {children, loading, error, fetchChildren};
}

export const useArtefactConceptTree = (acronym: string, uri: string, databaseType: string) => {
    const [tree, setTree] = useState<ArtefactTerm[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchTree = async () => {
        setLoading(true);
        artefactsRestClient.getArtefactConceptTree(acronym, uri, databaseType).then((response: any) => {
            if (response.status !== 200) {
                throw new Error(`Error fetching concept tree: ${response.statusText}`);
            }
            setTree(response.data || []);
        }).catch((error) => {
            console.error("Error fetching concept tree:", error);
            setError(`Failed to fetch concept tree: ${error.message}`);
        }).finally(() => {
            setLoading(false);
        })
    }

    useEffect(() => {
        if (acronym && uri && databaseType) {
            fetchTree();
        }
    }, [acronym, uri, databaseType]);

    return {tree, loading, error, fetchTree};
}

export const artefactsRestClient = new ArtefactsRestClient(httpClient)