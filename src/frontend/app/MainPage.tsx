import React from 'react';
import {EuiFlexGroup, EuiFlexItem, EuiPage, EuiPageBody, EuiPanel, EuiSpacer, EuiTitle} from '@elastic/eui';
import Autocomplete from './components/AutoComplete'

export function MainPage() {
    return (
        <EuiPage paddingSize={"l"}>
            <EuiFlexGroup gutterSize="m">
                <EuiFlexItem>
                    <EuiPanel>
                        <EuiTitle><h3>Autocomplete Widget</h3></EuiTitle>
                        <EuiSpacer/>
                        <Autocomplete apiUrl="http://localhost:8080/api-gateway/search?query="></Autocomplete>
                    </EuiPanel>
                </EuiFlexItem>

            </EuiFlexGroup>
        </EuiPage>
    );
}

