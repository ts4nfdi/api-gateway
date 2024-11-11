// Custom debounce function
import {
    EuiFieldSearch,
    EuiFlexGroup,
    EuiFlexItem,
    EuiListGroup,
    EuiListGroupItem,
    EuiSpacer, EuiFormRow
} from "@elastic/eui";
import {useSearch} from "@/app/utils/search";
import {EuiFieldText} from "@elastic/eui";
import {EuiLoadingChart} from "@elastic/eui";
import {EuiStat} from "@elastic/eui";
import {AutoCompleteResult} from "@/app/components/search/AutoCompleteResult";
import ArtefactModal from "@/app/components/Modal";
import React, {useRef, useState} from "react";


export default function Autocomplete(props: { apiUrl: string }) {
    const {
        suggestions,
        totalResults,
        inputValue,
        responseTime,
        isLoading,
        errorMessage,
        handleInputChange,
        handleApiUrlChange
    } = useSearch(props);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedArtefact, setSelectedArtefact] = useState(null);

    const openModal = (item: any) => {
        setSelectedArtefact(item);
        setIsModalOpen(true);
    };
    const closeModal = () => {
        setIsModalOpen(false);
        setSelectedArtefact(null);
    };


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
                            <EuiStat title={totalResults} description="Resutls count" titleSize={'xxs'}
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
                        <EuiFlexItem style={{width: '100%'}}>
                            <EuiListGroup flush={true} showToolTips bordered={true} maxWidth={"none"}>
                                {suggestions.map((suggestion: any, index) => (
                                    <EuiListGroupItem key={index} onClick={(e) => openModal(suggestion)}
                                                      label={<AutoCompleteResult suggestion={suggestion}/>}/>
                                ))}

                            </EuiListGroup>
                        </EuiFlexItem>
                    </>
                )}
                {isModalOpen && (
                    <ArtefactModal artefact={selectedArtefact} onClose={closeModal}/>
                )}
            </EuiFlexGroup>
        </>
    );
}
