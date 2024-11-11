import {
    EuiBadge,
    EuiFieldText,
    EuiFlexGroup,
    EuiFlexItem,
    EuiLink,
    EuiPanel,
    EuiSpacer,
    EuiTitle,
    EuiToolTip
} from "@elastic/eui";
import React from "react";
import ArtefactsTable from "@/app/components/BrowseResources";


function HomeBrowseSection(props: { apiUrlValue: string }) {
    const artefactsApiUrl = `${props.apiUrlValue}/artefacts?showResponseConfiguration=true`;
    return (<EuiFlexGroup justifyContent="spaceAround" alignItems={"center"} style={{width: '100%'}}>
        <EuiFlexItem grow={false} style={{width: '70%'}} paddingsize="xl">
            <EuiPanel>
                <EuiTitle>
                    <h3>
                        Browse resources
                        <EuiLink title={"Go to API Gateway"} href={`${artefactsApiUrl}`} target="_blank"></EuiLink>
                    </h3>
                </EuiTitle>

                <EuiSpacer/>

                <ArtefactsTable
                    apiUrl={artefactsApiUrl}></ArtefactsTable>
            </EuiPanel>
        </EuiFlexItem>
    </EuiFlexGroup>)
}

export default HomeBrowseSection
