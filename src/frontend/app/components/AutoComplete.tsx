// Custom debounce function
import {
    EuiFieldSearch,
    EuiFieldText,
    EuiFlexGroup,
    EuiFlexItem,
    EuiFormRow,
    EuiListGroup,
    EuiListGroupItem,
    EuiLoadingChart,
    EuiSpacer,
    EuiStat
} from "@elastic/eui";
import {useSearch} from "@/app/utils/search";
import {AutoCompleteResult} from "@/app/components/AutoCompleteResult";
import React from "react";
import ModalContainer, {useModal} from "@/app/utils/modal";


export default function Autocomplete(props: { apiUrl: string }) {
    const {
        suggestions,
        inputValue,
        responseTime,
        isLoading,
        errorMessage,
        handleInputChange,
        handleApiUrlChange
    } = useSearch(props);

    const {isModalOpen, selectedObject, openModal, closeModal} = useModal();


    return (
        <>
            <EuiFormRow label="Gateway search endpoint" fullWidth={true}>
                <EuiFieldText value={props.apiUrl}
                              onChange={handleApiUrlChange}
                              placeholder="Enter API URL" fullWidth={true}/>
            </EuiFormRow>
            <EuiSpacer size={'l'}></EuiSpacer>
            <EuiFormRow label="Search" fullWidth={true}>
                <EuiFieldSearch
                    placeholder="Search this"
                    value={inputValue}
                    isClearable={true}
                    onChange={handleInputChange}
                    fullWidth={true}
                />
            </EuiFormRow>

            {
                suggestions.length > 0 && !isLoading && !errorMessage && (
                    <EuiFlexGroup>
                        <EuiFlexItem>
                            <EuiStat title={responseTime + 's'} description="Response Time:" titleSize={'xxs'}/>
                        </EuiFlexItem>
                        <EuiFlexItem>
                            <EuiStat title={suggestions.length} description="Resutls count" titleSize={'xxs'}
                                     titleColor="success"/>
                        </EuiFlexItem>
                    </EuiFlexGroup>
                )
            }

            <EuiSpacer size={'l'}/>

            <EuiFlexGroup justifyContent={'center'} style={{width: '100%'}}>

                {isLoading && (
                    <EuiFlexItem grow={false}>
                        <EuiLoadingChart size="xl"/>
                    </EuiFlexItem>
                )}


                {errorMessage && (
                    <EuiFlexItem grow={false}>
                        <p>Error: {errorMessage}</p>
                    </EuiFlexItem>
                )}


                {suggestions.length > 0 && !isLoading && !errorMessage && (
                    <>
                        <EuiFlexItem grow={true}>
                            <EuiListGroup style={{width: '100%'}} showToolTips maxWidth={'none'} size={'s'}
                                          flush={false}
                                          bordered={true}>
                                {suggestions.map((suggestion: any, index) => (
                                    <EuiListGroupItem onClick={() => openModal(suggestion)} key={index}
                                                      style={{width: '100%'}}
                                                      label={<AutoCompleteResult suggestion={suggestion}/>}/>
                                ))}

                            </EuiListGroup>
                        </EuiFlexItem>
                    </>
                )}

            </EuiFlexGroup>

            <ModalContainer onClose={closeModal} artefact={selectedObject} isOpen={isModalOpen}></ModalContainer>

        </>
    );
}
