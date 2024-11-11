import {EuiFieldText, EuiFlexGroup, EuiFlexItem, EuiLink, EuiPanel, EuiSpacer, EuiText, EuiTitle} from "@elastic/eui";
import React from "react";


function HomeBanner(props: { apiUrlValue: string, onChange: any }) {
    return (<EuiFlexGroup justifyContent="spaceAround" alignItems={"center"} style={{width: '100%'}}>
        <EuiFlexItem grow={false} style={{width: '70%'}} paddingsize="xl">
            <EuiPanel>
                <EuiTitle size="m">
                    <h2>TS4NFDI Federated Service</h2>
                </EuiTitle>

                <EuiSpacer size="s"/>
                <EuiLink href="#" target="_blank">
                    {props.apiUrlValue}
                </EuiLink>

                <EuiSpacer size="s"/>

                <EuiText>
                    <p>
                        The TS4NFDI Federated Service is an advanced, dynamic solution designed to perform federated
                        calls across multiple Terminology Services (TS) within NFDI. It is particularly tailored for
                        environments where integration and aggregation of diverse data sources are essential. The
                        service offers search capabilities, enabling users to refine search results based on specific
                        criteria, and supports responses in both JSON and JSON-LD formats.
                    </p>
                    <p>
                        A standout feature of this service is its dynamic nature, governed by a JSON configuration file.
                        This design choice allows for easy extension and customization of the service to include new TS
                        or modify existing configurations.
                    </p>
                </EuiText>
            </EuiPanel>
        </EuiFlexItem>
    </EuiFlexGroup>)
}

export default HomeBanner
