import {create} from 'zustand'
import {CollectionResponse} from "@/app/api/CollectionsRestClient";
import {Artefact} from "@/app/api/ArtefactsRestClient";

type SelectedCollectionStore = {
    data: {
        collection: CollectionResponse;
        sources: string[];
        artefacts: Artefact[];
    };
    setCollection: (collection: CollectionResponse) => void;
    setSources: (sources: string[]) => void;
    setArtefacts: (artefacts: Artefact[]) => void;
}
const useSelectedCollectionStore = create<SelectedCollectionStore>((set, get) => ({
    // State
    data: {
        collection: {} as CollectionResponse,
        sources: [] as string[],
        artefacts: [] as Artefact[],
    },

    // Actions
    setCollection: (collection: CollectionResponse) => {
        set((state: any) => ({
            data: {
                ...state.data,
                collection: collection,
            }
        }))
    },

    setSources: (sources: string[]) => {
        set((state: any) => ({
            data: {
                ...state.data,
                sources: sources,
            }
        }))
    },

    setArtefacts: (artefacts: Artefact[]) => {
        set((state: any) => ({
            data: {
                ...state.data,
                artefacts: artefacts,
                collection: {
                    ...state.data.collection,
                    terminologies: artefacts.map(artefact => ({
                        label: artefact.label,
                        short_form: artefact.short_form,
                        source: artefact.source,
                        uri: artefact.iri,
                    }))
                }
            }
        }))
    },
}))

export default useSelectedCollectionStore