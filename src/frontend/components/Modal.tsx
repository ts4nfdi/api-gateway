import React from 'react';
import {EuiButton, EuiModal, EuiModalBody, EuiModalFooter, EuiModalHeader, EuiModalHeaderTitle,} from '@elastic/eui';

// @ts-ignore
export default function ArtefactModal({artefact, onClose}) {
    return (
        <>
            <EuiModal onClose={onClose}>
                <EuiModalHeader>
                    <EuiModalHeaderTitle>
                        {artefact.label}
                    </EuiModalHeaderTitle>
                </EuiModalHeader>
                <EuiModalBody>
                    <p><strong>Backend Type:</strong> {artefact.backend_type}</p>
                    <p><strong>Source:</strong> <a href={artefact.source} target="_blank"
                                                   rel="noopener noreferrer">{artefact.source}</a></p>
                    <p><strong>Source Name:</strong> {artefact.source_name}</p>
                    <p><strong>Source URL:</strong> <a href={artefact.source_url} target="_blank"
                                                       rel="noopener noreferrer">{artefact.source_url}</a></p>
                </EuiModalBody>
                <EuiModalFooter>
                    <EuiButton onClick={() => window.open(artefact.source_url, '_blank', 'noopener,noreferrer')}>
                        Go to {artefact.source_name}
                    </EuiButton>

                    <EuiButton onClick={onClose} fill>
                        Close
                    </EuiButton>
                </EuiModalFooter>
            </EuiModal>
        </>
    );
};
