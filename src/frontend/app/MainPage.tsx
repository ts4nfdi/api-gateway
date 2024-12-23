import React, { useState } from 'react';
import {
    EuiFlexGroup,
    EuiFlexItem,
    EuiSpacer,
    EuiTitle,
    EuiPageHeader,
    EuiPageHeaderSection, EuiBadge, EuiFieldText
} from '@elastic/eui';
import Autocomplete from './components/AutoComplete'
import {useEuiPaddingCSS} from "@elastic/eui";
import {EuiPanel} from "@elastic/eui";
import {EuiHorizontalRule} from "@elastic/eui";
import ArtefactsTable from "@/app/components/BrowseResources";


function Header() {
    return (
        <EuiPageHeader
            paddingSize='l'
            bottomBorder
            pageTitle="API Gateway"
            iconType="logoKibana"
            description="A user interface demonstrating the NFDI API Gateway page">
        </EuiPageHeader>
    )
}



export function MainPage(props: {apiUrl: string}) {

    const [apiUrlValue, setApiUrl] = useState(props.apiUrl);

    return (
        <>
            <Header></Header>

            <EuiSpacer size="xxl"/>
            <EuiFlexGroup justifyContent="spaceAround" alignItems={"center"} style={{width: '100%'}}>
                <EuiFlexItem grow={false} style={{width: '70%'}} paddingSize="xl">
                    <EuiPanel>
                        <EuiFieldText value={apiUrlValue}

                                      onChange={(e) => setApiUrl(e.target.value)}
                                      placeholder="Enter API Gateway URL" fullWidth={true}/>
                    </EuiPanel>
                </EuiFlexItem>
            </EuiFlexGroup>
            <EuiSpacer size="xxl"/>
            <EuiFlexGroup justifyContent="spaceAround" alignItems={"center"} style={{width: '100%'}}>
                <EuiFlexItem grow={false} style={{width: '70%'}} paddingSize="xl">
                    <EuiPanel>
                        <EuiTitle>
                            <h3>
                                <span>Find a concept</span>
                                {/*<EuiBadge color="primary" title={<a href={"http://localhost:8080/api-gateway/search?query="}></a>}></EuiBadge>*/}
                            </h3>
                        </EuiTitle>
                        <EuiSpacer/>
                        <Autocomplete apiUrl={`${apiUrlValue}/search?query=`}></Autocomplete>
                    </EuiPanel>
                </EuiFlexItem>
            </EuiFlexGroup>
            <EuiSpacer size="xxl"/>
            <EuiFlexGroup justifyContent="spaceAround" alignItems={"center"} style={{width: '100%'}}>
                <EuiFlexItem grow={false} style={{width: '70%'}} paddingSize="xl">
                    <EuiPanel>
                        <EuiTitle>
                            <h3>
                                <span>Browse resources</span>
                            </h3>
                        </EuiTitle>
                        <EuiSpacer/>
                        <ArtefactsTable apiUrl={`${apiUrlValue}/artefacts?showResponseConfiguration=true`} ></ArtefactsTable>
                    </EuiPanel>
                </EuiFlexItem>
            </EuiFlexGroup>


        </>
    )
}
