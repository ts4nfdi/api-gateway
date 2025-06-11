import {RestApplicationClient, RestResponse} from "@/lib/RestClient";
import {useCallback, useRef, useState} from "react";
import httpClient from "@/lib/httpClient";
import {CollectionResponse} from "@/app/api/CollectionsRestClient";
import {ResponseConfig} from "@/app/api/ArtefactsRestClient";
import {debounce} from "next/dist/server/utils";

export class SearchRestClient extends RestApplicationClient {
    search(query: string, databases: string[], collectionId: string | undefined): RestResponse<any> {
        const params: any = {
            query: query,
            showResponseConfiguration: true
        };

        if (databases && databases.length > 0) {
            params['database'] = databases.join(',');
        }
        if (collectionId && collectionId.length > 0) {
            params["collectionId"] = collectionId;
        }

        params["showResponseConfiguration"] = true;

        return this.httpClient.request({method: 'GET', url: '/search', params})
    }

}

export const searchRestClient = new SearchRestClient(httpClient)

export function useSearch({databases, collection}: {
    databases: string[],
    collection: CollectionResponse
}) {
    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setError] = useState(null);
    const [suggestions, setSuggestions] = useState<[] | null>(null);
    const [inputValue, setInputValue] = useState("");
    const [totalResults, setTotalResults] = useState(0)
    const [responseConfig, setResponseConfig] = useState<ResponseConfig>({totalResponseTime: -1});
    const latestRequestRef = useRef(0); // Ref to track the latest request

    const fetchSuggestions = async (query: string, requestId: number, pageSize = 20) => {
        if (query.length < 2) return setSuggestions(null);

        setIsLoading(true);
        setError(null);
        setSuggestions(null);
        searchRestClient.search(query, databases, collection.id).then((response) => {
            const data = response.data.collection;
            if (requestId === latestRequestRef.current) {
                setSuggestions(data ? data.slice(0, pageSize) : []);
                setResponseConfig(response.data.responseConfig);
                setTotalResults(data ? data.length : 0)
            }
            setError(null); // Clear any previous errors
        }).catch(x => {
            setError(x.message || "Failed to fetch suggestions");
        }).finally(() => {
            setIsLoading(false);
        })
    };

    // Debounced version of fetchSuggestions
    const debouncedFetchSuggestions = useCallback(debounce((query) => {
        const requestId = ++latestRequestRef.current; // Increment request ID
        fetchSuggestions(query, requestId);
    }, 100), [databases, collection]);

    const handleInputChange = (event: { target: { value: any; }; }) => {
        const value = event.target.value;
        setInputValue(value);
    };


    return {
        suggestions,
        totalResults,
        inputValue,
        responseConfig,
        isLoading,
        errorMessage,
        handleInputChange,
        debouncedFetchSuggestions,
    };
}
