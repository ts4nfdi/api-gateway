// Custom debounce function
import { EuiBadge, EuiButton, EuiFieldSearch, EuiFlexGroup, EuiFlexItem, EuiListGroup, EuiListGroupItem } from "@elastic/eui";
import { SetStateAction, useCallback, useRef, useState } from "react";

function debounce(this: any, func: { (query: any): void; apply?: any; }, wait: number | undefined) {
    let timeout: string | number | NodeJS.Timeout | undefined;
    return (...args: any) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}


export default function Autocomplete(props: { apiUrl: string }) {
    const [suggestions, setSuggestions] = useState([]);
    const [inputValue, setInputValue] = useState("");
    const [apiUrlValue, setApiUrl] = useState(props.apiUrl);
    const [responseTime, setResponseTime] = useState("");
    const latestRequestRef = useRef(0); // Ref to track the latest request

    const fetchSuggestions = async (query: string | any[], requestId: number) => {
        if (query.length < 2) return;
        const startTime = performance.now(); // Start timing
        try {
            const response = await fetch(`${apiUrlValue}${query}`);
            const data = await response.json();
            const endTime = performance.now(); // End timing
            if (requestId === latestRequestRef.current) {
                setResponseTime((endTime - startTime).toFixed(2)); // Calculate response time
                setSuggestions(data ? data : []);
            }
        } catch (error) {
            console.error("Error fetching suggestions:", error);
        }
    };

    // Debounced version of fetchSuggestions
    const debouncedFetchSuggestions = useCallback(debounce((query) => {
        const requestId = ++latestRequestRef.current; // Increment request ID
        fetchSuggestions(query, requestId);
    }, 100), [apiUrlValue]);

    const handleInputChange = (event: { target: { value: any; }; }) => {
        const value = event.target.value;
        setInputValue(value);
        debouncedFetchSuggestions(value);
    };

    const handleItemClick = (item: any) => {
        const url = item.html_url; // Assuming each suggestion has an 'html_url' property
        if (url) {
            window.open(url, '_blank'); // Open URL in a new tab
        }
    };

    const handleApiUrlChange = (event: { target: { value: SetStateAction<string>; }; }) => {
        setApiUrl(event.target.value);
        // Reset suggestions when API URL changes
        setSuggestions([]);
    };


    function AutoCompleteResutlt(props: { suggestion: any }) {

        return <>
            <EuiFlexGroup justifyContent="spaceBetween" wrap={true}>
                <EuiFlexItem grow={true}>
                    <div> {props.suggestion.label} </div>
                </EuiFlexItem>
                <EuiFlexItem>
                    <EuiFlexGroup>
                        <EuiFlexItem><EuiBadge color="primary">{props.suggestion.backend_type}</EuiBadge></EuiFlexItem>
                        <EuiFlexItem><EuiBadge color="success">{props.suggestion.ontology}</EuiBadge></EuiFlexItem>
                        <EuiFlexItem><EuiBadge color="danger">{props.suggestion.short_form}</EuiBadge></EuiFlexItem>
                    </EuiFlexGroup>
                </EuiFlexItem>
            </EuiFlexGroup>
        </>
    }
    return (
        <div className="relative w-full max-w-lg mx-auto">
            <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700">API URL</label>
                <input
                    className="mt-1 p-2 w-full border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    type="text"
                    value={apiUrlValue}
                    onChange={handleApiUrlChange}
                    placeholder="Enter API URL"
                />
            </div>
            <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700">Search</label>
                <EuiFieldSearch
                    placeholder="Search this"
                    value={inputValue}
                    isClearable={true}
                    onChange={handleInputChange}
                />

            </div>
            {responseTime !== null && (
                <div className="mb-4 text-sm text-gray-600">
                    Response Time: <span className="font-medium text-blue-600">{responseTime} ms</span>
                </div>
            )}
            {suggestions.length > 0 && (
                <EuiListGroup flush={true} bordered={true}>
                    {suggestions.map((suggestion: any, index) => (
                        <EuiListGroupItem onClick={() => handleItemClick(suggestion)} label={<AutoCompleteResutlt suggestion={suggestion} />} />
                    ))}

                </EuiListGroup>
            )}
        </div>
    );
}
