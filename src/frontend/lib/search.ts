import {SetStateAction, useCallback, useRef, useState} from "react";
import httpClient from "@/lib/httpClient";


function debounce(this: any, func: { (query: any): void; apply?: any; }, wait: number) {
    let timeout: string | number | NodeJS.Timeout | undefined;
    return (...args: any) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

export function useSearch(props: { apiUrl: string }) {
    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setError] = useState(null);
    const [suggestions, setSuggestions] = useState([]);
    const [inputValue, setInputValue] = useState("");
    const [totalResults, setTotalResults] = useState(0)
    const [responseTime, setResponseTime] = useState("");
    const latestRequestRef = useRef(0); // Ref to track the latest request
    const [apiUrl, setApiUrl] = useState(props.apiUrl);

    const fetchSuggestions = async (query: string,  requestId: number, apiUrl: string, pageSize = 20) => {
        if (query.length < 2) return setSuggestions([]);

        // Set loading state to true
        setIsLoading(true);
        setError(null); // Clear any previous errors

        const startTime = performance.now();

        let url = new URL(apiUrl);
        url.searchParams.set("query", query);
        url.searchParams.entries().forEach(([key, value]) => {
            value = value.toString().trim();
            if (value === null || value === undefined || value == "") {
                url.searchParams.delete(key);
            }
        })

        if(url.searchParams.has("collectionId") && url.searchParams.get("collectionId") === "") {
            url.searchParams.delete("collectionId");
        }

        try {
            const response = await httpClient.get(url.toString());
            const data = response.data;
            const endTime = performance.now(); // End timing

            if (requestId === latestRequestRef.current) {
                setResponseTime(((endTime - startTime) / 1000).toFixed(2)); // Calculate response time
                setTotalResults(data ? data.length : 0)
                setSuggestions(data ? data.slice(0, pageSize) : []);
            }
        } catch (error: any) {
            setError(error.message); // Set error message
        } finally {
            // Set loading state to false
            setIsLoading(false);
        }
    };

    // Debounced version of fetchSuggestions
    const debouncedFetchSuggestions = useCallback(debounce((query) => {
        const requestId = ++latestRequestRef.current; // Increment request ID
        fetchSuggestions(query, requestId, apiUrl)
    }, 100), [apiUrl]);

    const handleInputChange = (event: { target: { value: any; }; }) => {
        const value = event.target.value;
        setInputValue(value);
        // debouncedFetchSuggestions(value);
    };


    const handleApiUrlChange = (event: { target: { value: SetStateAction<string>; }; }) => {
        setApiUrl(event.target.value);
        setSuggestions([]);
    };

    return {
        apiUrl,
        suggestions,
        totalResults,
        inputValue,
        responseTime,
        isLoading,
        errorMessage,
        handleInputChange,
        handleApiUrlChange,
        setApiUrl,
        debouncedFetchSuggestions,
    };
}
