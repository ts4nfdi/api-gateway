import {SetStateAction, useCallback, useRef, useState} from "react";


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
    const fetchSuggestions = async (query: string | any[], requestId: number, apiUrl: string, pageSize = 20) => {
        if (query.length < 2) return setSuggestions([]);

        // Set loading state to true
        setIsLoading(true);
        setError(null); // Clear any previous errors

        const startTime = performance.now(); // Start timing

        try {
            const response = await fetch(`${apiUrl}${query}`);
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();
            const endTime = performance.now(); // End timing

            if (requestId === latestRequestRef.current) {
                setResponseTime(((endTime - startTime) / 1000).toFixed(2)); // Calculate response time
                setTotalResults(data ? data.length : 0)
                setSuggestions(data ? data.slice(0, pageSize) : []);
            }
        } catch (error: any) {
            console.error("Error fetching suggestions:", error);
            setError(error.message); // Set error message
        } finally {
            // Set loading state to false
            setIsLoading(false);
        }
    };

    // Debounced version of fetchSuggestions
    const debouncedFetchSuggestions = useCallback(debounce((query) => {
        const requestId = ++latestRequestRef.current; // Increment request ID
        fetchSuggestions(query, requestId, props.apiUrl).then(r => r);
    }, 100), [props.apiUrl]);

    const handleInputChange = (event: { target: { value: any; }; }) => {
        const value = event.target.value;
        setInputValue(value);
        debouncedFetchSuggestions(value);
    };


    const handleApiUrlChange = (event: { target: { value: SetStateAction<string>; }; }) => {
        setSuggestions([]);
    };

    return {
        suggestions,
        totalResults,
        inputValue,
        responseTime,
        isLoading,
        errorMessage,
        handleInputChange,
        handleApiUrlChange
    };
}
