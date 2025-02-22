import React, {useCallback, useState} from "react";
import ArtefactModal from "@/components/Modal";

export function useModal() {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedObject, setSelectedObject] = useState<any>(null);

    const openModal = useCallback((object: any) => {
        setSelectedObject(object);
        setIsModalOpen(true);
    }, []);

    const closeModal = useCallback(() => {
        setIsModalOpen(false);
        setSelectedObject(null);
    }, []);

    return {
        isModalOpen,
        selectedObject,
        openModal,
        closeModal,
    };
}

interface ModalContainerProps {
    isOpen: boolean;
    artefact: any;
    onClose: () => void;
}

export default function ModalContainer(props: ModalContainerProps) {
    if (!props.isOpen) return null;

    return (
        <ArtefactModal onClose={props.onClose} artefact={props.artefact}></ArtefactModal>
    );
}
