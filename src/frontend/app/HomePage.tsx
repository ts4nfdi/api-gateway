import React, {useState} from 'react';
import {
    EuiSpacer,
    EuiPageHeader,
} from '@elastic/eui';
import HomeBanner from "@/app/partials/homepage/HomeBanner";
import HomeBrowseSection from "@/app/partials/homepage/HomeBrowseSection";
import HomeSearchSection from "@/app/partials/homepage/HomeSearchSection";

function Header() {
    return (
        <EuiPageHeader
            paddingSize='l'
            bottomBorder
            pageTitle="API Gateway"
            iconType="/api-gateway/tsnfdi.png"
            description="A user interface demonstrating the NFDI API Gateway page">
        </EuiPageHeader>
    )
}

export function HomePage(props: { apiUrl: string }) {

    const [apiUrlValue, setApiUrl] = useState(props.apiUrl);

    // @ts-ignore
    return (
        <>
            <Header></Header>

            <EuiSpacer size="xxl"/>
            <HomeBanner apiUrlValue={apiUrlValue} onChange={(e: any) => setApiUrl(e.target.value)}></HomeBanner>

            <EuiSpacer size="xxl"/>
            <HomeSearchSection apiUrlValue={apiUrlValue}></HomeSearchSection>


            <EuiSpacer size="xxl"/>
            <HomeBrowseSection apiUrlValue={apiUrlValue}></HomeBrowseSection>

            <EuiSpacer size="xxl"/>

        </>
    )
}
