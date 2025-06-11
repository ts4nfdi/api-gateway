import DialogWrapper from "@/components/Dialog";
import React, {useState} from "react";
import DatabaseSelectorSquares from "@/components/DatabaseSelectorSquares";
import {Artefact} from "@/app/api/ArtefactsRestClient";
import {Check, ChevronLeft, ChevronRight, Database, FileText, Settings} from "lucide-react";
import {CollectionResponse, collectionRestClient} from "@/app/api/CollectionsRestClient";
import UsersSelector from "@/components/UsersSelector";
import {Switch} from "@/components/ui/switch";
import {Label} from "@radix-ui/react-label";
import {ArtefactsList, ArtefactsSelectorList} from "@/components/ArtefactsSelectorList";
import {useAuth} from "@/lib/authGuard";
import {useRouter} from "next/navigation";

interface CollectionBuilderProps {
    selectedSources: string[];
    setSelectedSources: (sources: string[]) => void;
    selectedArtefacts: Artefact[];
    setSelectedArtefacts?: (artefacts: Artefact[]) => void;
    onSave: (collection: CollectionResponse, isNew: boolean) => void;
}

export function QueryInput(
    {query, setQuery, placeholder}: {
        query: string,
        setQuery: (query: string) => void,
        placeholder: string
    }
) {
    return <>
        <input
            type="text"
            placeholder={placeholder}
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
        />
        {query && (
            <button
                onClick={() => setQuery("")}
                className="absolute right-2 top-2 text-gray-400 hover:text-gray-600"
            >
                ×
            </button>
        )}
    </>
}

function Step1({selectedSources, setSelectedSources}: any) {
    return (
        <div className="space-y-4">
            <div>
                <h2 className="text-xl font-semibold text-gray-800 mb-2">Select Database Sources</h2>
                <p className="text-gray-600 mb-4">Choose the databases you want to include in your
                    collection.</p>
            </div>
            <DatabaseSelectorSquares
                selected={selectedSources}
                onChange={(sources: any) => setSelectedSources(sources)}
            />
        </div>
    )
}

function Step2({selectedSources, selectedArtefacts, setSelectedArtefacts}: any) {
    return <div>
        <div>
            <h2 className="text-xl font-semibold text-gray-800 mb-2">Select Artefacts</h2>
            <p className="text-gray-600 mb-4">Choose the specific artefacts from your selected
                databases.</p>
        </div>
        <ArtefactsSelectorList selectedSources={selectedSources}
                               selectedArtefacts={selectedArtefacts}
                               setSelectedArtefacts={setSelectedArtefacts}/>


    </div>
}

function Step3({selectedSources, selectedArtefacts, collectionData, setCollectionData}: any) {
    const router = useRouter()
    const {user, isLoading, logout, authRedirect} = useAuth()
    const isLoggedIn = user != null;
    if (isLoading) return <div>Loading...</div>;
    if (!isLoggedIn) {
        return <div
            className="flex flex-col items-center justify-center p-8">
            <div className="mb-4">
                <svg className="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                          d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                </svg>
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Authentication Required</h3>
            <p className="text-gray-600 text-center mb-6">
                You must be logged in to create and save your current collection.
            </p>
            <button
                onClick={() => router.push(authRedirect)}
                className="inline-flex items-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-md transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
                <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                          d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"/>
                </svg>
                Sign In
            </button>
        </div>
    }
    return <>
        <CollectionDetails selectedSources={selectedSources}
                           selectedArtefacts={selectedArtefacts}
                           data={collectionData}
                           setData={setCollectionData}
        />
        <CollectionSummary selectedSources={selectedSources} selectedArtefacts={selectedArtefacts}/>
    </>
}

export function CollectionSummary({selectedSources, selectedArtefacts}: any) {
    return <div className="bg-gray-50 p-4 rounded-lg">
        <h3 className="text-sm font-medium text-gray-700 mb-3">Collection Summary</h3>
        <div className="space-y-2 text-sm text-gray-600">
            <p><strong>Databases:</strong> {selectedSources?.join(', ')}</p>
            <p className={"flex gap-1"}>
                <strong>Artefacts:</strong> <span> <ArtefactsList selectedArtefacts={selectedArtefacts}/> </span>
            </p>
        </div>
    </div>
}

export function CollectionDetails({selectedSources, selectedArtefacts, data, setData}: {
    selectedSources: string[],
    selectedArtefacts: Artefact[],
    data: CollectionResponse,
    setData: (data: any) => void
}) {

    const updateData = (newValue: any, key: string) => {
        let updatedData = {...data, [key]: newValue};
        setData(updatedData);
    }

    const addCollaborator = (usernames: string[]) => {
        let collaborators = usernames.map((user: any) => ({
            username: user,
            role: "USER",
        }))
        updateData(collaborators, "collaborators");
    }

    return <div className="space-y-6">
        <div>
            <h2 className="text-xl font-semibold text-gray-800 mb-2">Collection Details</h2>
            <p className="text-gray-600 mb-6">Provide details about your collection and save it.</p>
        </div>

        <div className="space-y-4">
            <input
                type="text"
                value={data.label}
                onChange={(e) => updateData(e.target.value, "label")}
                placeholder="Enter collection name...*"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />

            <textarea
                value={data.description}
                onChange={(e) => updateData(e.target.value, "description")}
                placeholder="Describe your collection... (Optional)"
                rows={4}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />

            <div className="flex items-center space-x-2">
                <Switch
                    id="public"
                    checked={data.isPublic}
                    onCheckedChange={(checked) => updateData(checked, "isPublic")}
                />
                <Label htmlFor="public">Make this collection public</Label>
            </div>

            <UsersSelector selected={data.collaborators?.map(x => x.username)}
                           onChange={(users: any) => addCollaborator(users)}/>

        </div>
    </div>
}


export function CollectionBuilder({
                                      selectedSources, setSelectedSources,
                                      selectedArtefacts, setSelectedArtefacts,
                                      onSave
                                  }: CollectionBuilderProps) {
    const [currentStep, setCurrentStep] = useState(1);
    const [collectionData, setCollectionData] = useState<CollectionResponse>({
        id: "",
        label: "",
        description: "",
        terminologies: [],
        collaborators: [],
        isPublic: false,
    })

    const canProceedToStep2 = selectedSources && selectedSources.length > 0;
    const canProceedToStep3 = selectedArtefacts && selectedArtefacts.length > 0;
    const canSave = collectionData.label.trim().length > 0;

    const handleSave = async () => {
        collectionData.terminologies = selectedArtefacts.map((artefact: any) => ({
            label: artefact.short_form,
            source: artefact.source_name || artefact.source,
            uri: artefact.iri || artefact.uri,
        }));
        const res: any = await collectionRestClient.createCollection(collectionData);
        if (res.status === 201 || res.status === 200) {
            console.log("Collection saved/updated successfully");
            onSave(res.data, collectionData.id === "");
        } else {
            console.error("Failed to save/update collection");
            // TODO add notification
        }
    };

    const steps = [
        {number: 1, title: 'Select Database', icon: Database, completed: canProceedToStep2},
        {number: 2, title: 'Select Artefacts', icon: FileText, completed: canProceedToStep3},
        {number: 3, title: 'Collection Details', icon: Settings, completed: canSave}
    ];

    return (
        <div className="flex flex-col  max-h-[70vh]">
            {/* Step Indicator */}
            <div className="mb-6 px-4">
                <div className="flex items-center justify-between">
                    {steps.map((step, index) => (
                        <div key={step.number} className="flex items-center">
                            <div
                                className={`flex items-center justify-center w-10 h-10 rounded-full border-2 transition-all ${
                                    currentStep === step.number
                                        ? 'border-blue-500 bg-blue-500 text-white'
                                        : step.completed
                                            ? 'border-green-500 bg-green-500 text-white'
                                            : 'border-gray-300 bg-white text-gray-500'
                                }`}>
                                {step.completed && currentStep !== step.number ? (
                                    <Check className="w-5 h-5"/>
                                ) : (
                                    <step.icon className="w-5 h-5"/>
                                )}
                            </div>
                            <div className="ml-3 hidden sm:block">
                                <p className={`text-sm font-medium ${
                                    currentStep === step.number ? 'text-blue-600' : 'text-gray-500'
                                }`}>
                                    Step {step.number}
                                </p>
                                <p className={`text-xs ${
                                    currentStep === step.number ? 'text-blue-600' : 'text-gray-400'
                                }`}>
                                    {step.title}
                                </p>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="flex-1 p-4 overflow-y-auto">
                {currentStep === 1 && (
                    <Step1 selectedSources={selectedSources} setSelectedSources={setSelectedSources}/>
                )}

                {currentStep === 2 && (
                    <Step2 selectedSources={selectedSources}
                           selectedArtefacts={selectedArtefacts}
                           setSelectedArtefacts={setSelectedArtefacts}/>
                )}

                {/* Step 3: Collection Details */}
                {currentStep === 3 && (
                    <Step3 selectedSources={selectedSources}
                           selectedArtefacts={selectedArtefacts}
                           collectionData={collectionData}
                           setCollectionData={setCollectionData}/>
                )}
            </div>

            <div className="flex justify-between items-center p-4 border-t">
                <button
                    onClick={() => setCurrentStep(Math.max(1, currentStep - 1))}
                    disabled={currentStep === 1}
                    className={`flex items-center px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                        currentStep === 1
                            ? 'text-gray-400 cursor-not-allowed'
                            : 'text-gray-700 hover:bg-gray-200'
                    }`}
                >
                    <ChevronLeft className="w-4 h-4 mr-1"/>
                    Previous
                </button>

                <div className="text-sm text-gray-500">
                    Step {currentStep} of {steps.length}
                </div>

                {currentStep < 3 ? (
                    <button
                        onClick={() => setCurrentStep(currentStep + 1)}
                        disabled={
                            (currentStep === 1 && !canProceedToStep2) ||
                            (currentStep === 2 && !canProceedToStep3)
                        }
                        className={`flex items-center px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                            (currentStep === 1 && !canProceedToStep2) ||
                            (currentStep === 2 && !canProceedToStep3)
                                ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                : 'bg-blue-600 text-white hover:bg-blue-700'
                        }`}
                    >
                        Next
                        <ChevronRight className="w-4 h-4 ml-1"/>
                    </button>
                ) : (
                    <button
                        onClick={handleSave}
                        disabled={!canSave}
                        className={`flex items-center px-6 py-2 rounded-md text-sm font-medium transition-colors ${
                            !canSave
                                ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                : 'bg-green-600 text-white hover:bg-green-700'
                        }`}
                    >
                        Save Collection
                    </button>
                )}
            </div>
        </div>
    );
}

export default function CollectionBuilderButton({
                                                    selectedSources, setSelectedSources
                                                }: any) {
    const [dialogOpen, setDialogOpen] = useState(false);
    const [selectedArtefacts, setSelectedArtefacts] = useState<Artefact[]>([]);

    return <div>
        <button
            className="inline-flex items-center justify-center rounded-md border border-transparent bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors duration-200"
            onClick={() => setDialogOpen(true)}
        >
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                 xmlns="http://www.w3.org/2000/svg">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                      d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
            </svg>
            Collection Builder
            {selectedSources && selectedSources.length > 0 && (
                <span
                    className="ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-200 text-blue-800">
                    {selectedSources.length}
                </span>
            )}
        </button>

        <DialogWrapper showFooter={false} title={"Build Your Collection"} isOpen={dialogOpen} setIsOpen={setDialogOpen}>
            <CollectionBuilder
                selectedArtefacts={selectedArtefacts}
                setSelectedArtefacts={setSelectedArtefacts}
                selectedSources={selectedSources}
                setSelectedSources={setSelectedSources}
                onSave={(collection, isNew) => {
                    setDialogOpen(false);
                }}
            />
        </DialogWrapper>
    </div>
}
