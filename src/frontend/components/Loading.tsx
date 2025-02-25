import {EuiLoadingChart, EuiText} from "@elastic/eui";
import React from "react";

export function Loader() {

    // Custom spinner container style
    const spinnerContainerStyle: any = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        flexDirection: 'column',
        height: '200px',
    };

    return (
        <div style={spinnerContainerStyle}>
            <EuiLoadingChart size="xl"/>
            <EuiText>Loading resources</EuiText>
        </div>
    )
}
