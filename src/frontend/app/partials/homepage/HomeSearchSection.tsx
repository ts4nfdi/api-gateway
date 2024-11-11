import {EuiFieldText, EuiFlexGroup, EuiFlexItem, EuiLink, EuiPanel, EuiSpacer, EuiTitle} from "@elastic/eui";
import React from "react";
import Autocomplete from "@/app/components/search/AutoComplete";

function HomeSearchSection(props: { apiUrlValue: string }) {
    const searchApiUrl = `${props.apiUrlValue}/search?query=`;
    return (<EuiFlexGroup justifyContent="spaceAround" alignItems={"center"} style={{width: '100%'}}>
        <EuiFlexItem grow={false} style={{width: '70%'}} paddingsize="xl">
            <EuiPanel>
                <EuiTitle>
                    <h3>
                        <span>Find a concept</span>
                        <EuiLink title={"Go to API Gateway"} href={`${searchApiUrl}`} target="_blank"></EuiLink>
                    </h3>
                </EuiTitle>
                <EuiSpacer/>
                <Autocomplete apiUrl={searchApiUrl}></Autocomplete>
            </EuiPanel>
        </EuiFlexItem>
    </EuiFlexGroup>)
}

export default HomeSearchSection
