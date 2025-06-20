import * as React from "react"
import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarGroup,
    SidebarGroupContent,
    SidebarGroupLabel,
    SidebarHeader,
    SidebarInset,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarProvider,
    SidebarRail,
    SidebarTrigger,
} from "@/components/ui/sidebar"
import {gotToURI} from "@/components/TermViewer"
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card"
import {Badge} from "@/components/ui/badge"
import {Separator} from "@/components/ui/separator"
import {Archive, Database, FileText, Globe, Layers} from "lucide-react"
import ArtefactMetadata from "@/app/home/browse/components/ArtefactMetadata";
import ArtefactConcepts from "@/app/home/browse/components/ArtefactConcepts";
import {Artefact, useArtefactConceptRoots} from "@/app/api/ArtefactsRestClient";
import {Loading} from "@/components/Loading";

// Content components for different tabs
function GlobalMetadataContent({data}: { data: Artefact }) {
    return <ArtefactMetadata data={data}/>

}

function ConceptsContent({data}: { data: Artefact }) {
    const acronym = data.short_form;
    const databaseType = data.backend_type;

    const { roots, loading } = useArtefactConceptRoots(acronym, databaseType);

    if (!acronym) {
        return <div className="p-4">No concepts available for this artefact.</div>
    }

    if (loading) {
        return <Loading/>
    }
    console.log("ArtefactConcepts roots:", roots);
    return <div className={'p-2'}> <ArtefactConcepts concepts={roots || []}/> </div>
}

function ComingSoonContent({title}: { title: string }) {
    return (
        <div className="flex flex-col items-center justify-center h-64 space-y-4">
            <div className="h-16 w-16 rounded-full bg-muted flex items-center justify-center">
                <Archive className="h-8 w-8 text-muted-foreground"/>
            </div>
            <div className="text-center">
                <h3 className="text-lg font-semibold">{title}</h3>
                <p className="text-sm text-muted-foreground">
                    This section is coming soon. Check back later for updates.
                </p>
            </div>
        </div>
    )
}

function SidebarFooterCallActions({data}: { data: any }) {
    return (
        <Card
            className="border-0 bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-blue-950/20 dark:to-indigo-950/20 shadow-sm">
            <CardHeader className="px-4 pb-3">
                <CardTitle className="text-sm font-medium flex items-center gap-2">
                    <Globe className="h-4 w-4 text-blue-600 dark:text-blue-400"/>
                    Resource Sources
                </CardTitle>
                <CardDescription className="text-xs">
                    Access original data and metadata
                </CardDescription>
            </CardHeader>
            <CardContent className="px-4 pt-0">
                <div className="flex flex-col space-y-2">
                    <div className="flex flex-wrap gap-2">
                        {gotToURI(data.iri, 'Original URI')}
                        {gotToURI(data.source_url, data.source_name)}
                    </div>
                </div>
            </CardContent>
        </Card>
    )
}

// Enhanced data with content mapping
const data = {
    navMain: [
        {
            title: "Metadata",
            url: "#",
            icon: FileText,
            items: [
                {
                    id: "global-metadata",
                    title: "Global Metadata",
                    url: "#",
                    isActive: true,
                    component: GlobalMetadataContent
                },
                {
                    id: "versions-metadata",
                    title: "Versions",
                    url: "#",
                    disabled: true,
                }
            ],
        },
        {
            title: "Data",
            url: "#",
            icon: Database,
            items: [
                {
                    id: "concepts",
                    title: "Concepts",
                    url: "#",
                    component: ConceptsContent
                },
                {
                    id: "properties",
                    title: "Properties",
                    url: "#",
                    disabled: true,
                },
                {
                    id: "individuals",
                    title: "Individuals",
                    url: "#",
                    disabled: true,
                },
                {
                    id: "collections",
                    title: "Collections",
                    url: "#",
                    disabled: true,
                },
                {
                    id: "schemes",
                    title: "Schemes",
                    url: "#",
                    disabled: true,
                },
            ],
        },
    ]
}

export function ArtefactViewer({artefact}: { artefact: any }) {
    const [activeTab, setActiveTab] = React.useState("global-metadata")

    // Flatten all items for easy lookup
    const allItems = data.navMain.flatMap(section => section.items)
    const activeItem = allItems.find(item => item.id === activeTab)

    const handleTabChange = (itemId: string, disabled?: boolean) => {
        if (!disabled) {
            setActiveTab(itemId)
        }
    }

    const renderContent = () => {
        if (activeItem?.component) {
            const Component = activeItem.component
            return <Component data={artefact}/>
        }

        if (activeItem?.disabled) {
            return <ComingSoonContent title={activeItem.title}/>
        }
    }

    return (
        <SidebarProvider defaultOpen={true}>
            <Sidebar
                className="border-r border-border/50 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 w-72">
                <SidebarHeader className="border-b border-border/50 bg-gradient-to-r from-background to-muted/20">
                    <div className="flex flex-col space-y-3 p-4">
                        <div className="flex flex-col gap-3">
                            <div>
                                <h2 className="text-lg font-semibold tracking-tight">{artefact.short_form}</h2>
                                <div className="flex items-center align-baseline text-xs space-x-2">
                                    <p className="text-muted-foreground">{artefact.label}</p>
                                    {artefact?.version && (
                                        <div>
                                            <Badge variant="outline"
                                                   className="font-mono bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/50 dark:text-blue-300 dark:border-blue-800">
                                                {artefact?.version}
                                            </Badge>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                </SidebarHeader>

                <SidebarContent className="px-2 py-4 overflow-y-auto">
                    <div className="space-y-6">
                        {data.navMain.map((section, index) => (
                            <div key={section.title}>
                                <SidebarGroup>
                                    <SidebarGroupLabel
                                        className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-foreground/80">
                                        <section.icon className="h-4 w-4"/>
                                        {section.title}
                                    </SidebarGroupLabel>
                                    <SidebarGroupContent className="mt-2">
                                        <SidebarMenu className="space-y-1">
                                            {section.items.map((item) => (
                                                <SidebarMenuItem key={item.id}>
                                                    <SidebarMenuButton
                                                        onClick={() => handleTabChange(item.id, item.disabled)}
                                                        isActive={activeTab === item.id}
                                                        disabled={item.disabled}
                                                        title={item.disabled ? "Coming soon" : item.title}
                                                        className={`
                                                                group relative rounded-lg px-3 py-2.5 text-sm transition-all duration-200 cursor-pointer
                                                                ${activeTab === item.id
                                                            ? 'bg-gradient-to-r from-blue-500/10 to-indigo-500/10 text-blue-700 dark:text-blue-300 border border-blue-200/50 dark:border-blue-800/50'
                                                            : 'hover:bg-muted/50'
                                                        }
                                                                ${item.disabled
                                                            ? 'opacity-50'
                                                            : ''
                                                        }
                                                            `}
                                                    >
                                                        <div className="flex flex-col">
                                                            <span className="font-medium">{item.title}</span>
                                                            {activeTab === item.id && (
                                                                <div
                                                                    className="absolute right-2 top-1/2 -translate-y-1/2">
                                                                    <div className="h-2 w-2 rounded-full bg-blue-500"/>
                                                                </div>
                                                            )}
                                                        </div>
                                                    </SidebarMenuButton>
                                                </SidebarMenuItem>
                                            ))}
                                        </SidebarMenu>
                                    </SidebarGroupContent>
                                </SidebarGroup>
                                {index < data.navMain.length - 1 && (
                                    <Separator className="my-4 mx-auto"/>
                                )}
                            </div>
                        ))}
                    </div>
                </SidebarContent>

                <SidebarFooter className="border-t border-border/50 bg-gradient-to-t from-muted/20 to-background p-3">
                    <SidebarFooterCallActions data={artefact}/>
                </SidebarFooter>
                <SidebarRail/>
            </Sidebar>

            <SidebarInset className="overflow-hidden">
                <div className="flex h-90 flex-col px-2">
                    <header
                        className="sticky top-0 z-10 border-b border-border/50 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
                        <div className="flex h-14 items-center px-6 justify-between">
                            <div className="flex items-center gap-3">
                                <div>
                                    <SidebarTrigger/>
                                </div>
                                <div
                                    className="flex h-8 w-8 items-center justify-center rounded-md bg-gradient-to-br from-emerald-500 to-teal-600 text-white">
                                    <Layers className="h-4 w-4"/>
                                </div>
                                <div>
                                    <h1 className="text-sm font-semibold">
                                        {activeItem?.title || 'Resource Details'}
                                    </h1>
                                </div>
                            </div>
                        </div>
                    </header>

                    <main className="flex-1 overflow-y-auto bg-gradient-to-br from-background to-muted/10">
                        <div className="h-full">
                            {renderContent()}
                        </div>
                    </main>
                </div>
            </SidebarInset>
        </SidebarProvider>
    )
}
