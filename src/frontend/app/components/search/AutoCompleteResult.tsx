import {EuiBadge, EuiFlexGroup, EuiFlexItem} from "@elastic/eui";

export function AutoCompleteResult(props: { suggestion: any }) {

    return <>
        <EuiFlexGroup justifyContent="spaceBetween" wrap={true} style={{ minWidth: '500px', maxHeight: "100vh", overflowY: "auto", width: "100%" }}>
            <EuiFlexItem grow={true} style={{width: '50vw'}}>
                <div> {props.suggestion.label} </div>
            </EuiFlexItem>
            <EuiFlexItem>
                <EuiFlexGroup justifyContent={"flexEnd"} gutterSize={'s'} >
                    <EuiFlexItem grow={0}><EuiBadge color="primary">{props.suggestion.backend_type.toString().toUpperCase()}</EuiBadge></EuiFlexItem>
                    <EuiFlexItem grow={0}><EuiBadge color="primary">{props.suggestion.source_name.toString().toUpperCase()}</EuiBadge></EuiFlexItem>
                    <EuiFlexItem grow={0}><EuiBadge color="default">{props.suggestion.ontology.toString().toUpperCase()}</EuiBadge></EuiFlexItem>
                    { props.suggestion.short_form && <EuiFlexItem grow={0}><EuiBadge color="default">{props.suggestion.short_form.toString().toUpperCase()}</EuiBadge></EuiFlexItem> }
                </EuiFlexGroup>
            </EuiFlexItem>
        </EuiFlexGroup>
    </>
}
