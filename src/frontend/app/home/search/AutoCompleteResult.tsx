import {EuiBadge, EuiFlexGroup, EuiFlexItem} from "@elastic/eui";

export function AutoCompleteResult(props: { suggestion: any }) {

    return <>
        <EuiFlexGroup justifyContent="spaceBetween" wrap={true} style={{ maxHeight: "100vh", overflowY: "auto", width: "100%" }}>
            <EuiFlexItem grow={true} style={{width: '50vw'}}>
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