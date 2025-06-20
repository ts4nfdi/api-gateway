import React, {useCallback, useState} from "react";
import {ChevronDown, ChevronRight, FileText, Folder, FolderOpen} from "lucide-react";
import {ArtefactTerm} from "@/app/api/SearchRestClient";
import {useArtefactConceptChildren} from "@/app/api/ArtefactsRestClient";

const Loading = () => (
    <div className="flex items-center justify-center p-4">
        <div className="animate-spin rounded-full h-6 w-6 border-2 border-blue-500 border-t-transparent"></div>
        <span className="ml-2 text-sm text-gray-600">Loading...</span>
    </div>
);

const TreeExpandChildren = ({
                                concept,
                                level,
                                onSelect,
                                selectedConcept,
                                searchTerm
                            }: {
    concept: ArtefactTerm;
    level: number;
    onSelect: (concept: ArtefactTerm) => void;
    selectedConcept: ArtefactTerm | null;
    searchTerm: string;
}) => {
    const {children, loading} = useArtefactConceptChildren(concept);

    console.log("concept children", concept.iri, children);

    if (loading) {
        return <Loading/>;
    }

    if (!children || children.length === 0) {
        return (
            <div className="p-3 text-sm text-gray-500 italic ml-8">
                No child concepts found
            </div>
        );
    }

    return (
        <div className="space-y-1">
            <TreeView
                concepts={children}
                onSelect={onSelect}
                level={level}
                selectedConcept={selectedConcept}
                searchTerm={searchTerm}
            />
        </div>
    );
};

const TreeNode = ({
                      concept,
                      level,
                      expandedNodes,
                      setExpandedNodes,
                      onSelect,
                      selectedConcept,
                  }: {
    concept: ArtefactTerm;
    level: number;
    expandedNodes: Set<string>;
    setExpandedNodes: React.Dispatch<React.SetStateAction<Set<string>>>;
    onSelect: (concept: ArtefactTerm) => void;
    selectedConcept: ArtefactTerm | null;
    searchTerm: string;
}) => {
    const isExpanded = expandedNodes.has(concept.iri) || (concept.children && concept.children.length > 0);
    const isSelected = selectedConcept?.iri === concept.iri;
    const [isHovered, setIsHovered] = useState(false);

    const toggleNode = useCallback((e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();

        if (!concept.hasChildren) return;

        setExpandedNodes(prev => {
            const newSet = new Set(prev);
            if (prev.has(concept.iri)) {
                newSet.delete(concept.iri);
            } else {
                newSet.add(concept.iri);
            }
            return newSet;
        });
    }, [concept.iri, concept.hasChildren, setExpandedNodes]);

    const handleSelect = useCallback((e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        onSelect(concept);
    }, [concept, onSelect]);

    const indentLevel = level * 16;

    console.log("Rendering TreeNode for concept:", concept.label, "at level", level, "expanded:", isExpanded, "selected:", isSelected,
        "children:", concept.children);
    return (
        <div className="w-full">
            <div
                className={`
          group flex items-center cursor-pointer transition-all duration-200 ease-in-out
          rounded-md mx-1 my-0.5 relative py-1.5
          ${isSelected
                    ? 'bg-blue-50 border border-blue-200'
                    : isHovered
                        ? 'bg-gray-50'
                        : 'hover:bg-gray-25'
                }
        `}
                style={{paddingLeft: `${indentLevel + 8}px`}}
                onClick={handleSelect}
                onMouseEnter={() => setIsHovered(true)}
                onMouseLeave={() => setIsHovered(false)}
                role="treeitem"
                tabIndex={0}
                aria-expanded={concept.hasChildren ? isExpanded : undefined}
                aria-selected={isSelected}
                onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        handleSelect(e as any);
                    } else if (e.key === 'ArrowRight' && concept.hasChildren && !isExpanded) {
                        toggleNode(e as any);
                    } else if (e.key === 'ArrowLeft' && concept.hasChildren && isExpanded) {
                        toggleNode(e as any);
                    }
                }}
            >
                {/* Expand/Collapse Button */}
                <div
                    className={` flex items-center justify-center w-5 h-5 mr-1 rounded
                                ${concept.hasChildren ? 'hover:bg-gray-200 cursor-pointer' : 'cursor-default'}`}
                    onClick={concept.hasChildren ? toggleNode : undefined}
                >
                    {concept.hasChildren ? (
                        isExpanded ? (
                            <ChevronDown size={14} className="text-gray-600"/>
                        ) : (
                            <ChevronRight size={14} className="text-gray-600"/>
                        )
                    ) : null}
                </div>

                {/* Icon */}
                <div className="flex items-center justify-center w-4 h-4 mr-2">
                    {concept.hasChildren ? (
                        isExpanded ? (
                            <FolderOpen size={14} className="text-blue-500"/>
                        ) : (
                            <Folder size={14} className="text-blue-600"/>
                        )
                    ) : (
                        <FileText size={12} className="text-gray-500"/>
                    )}
                </div>

                {/* Label */}
                <div className="flex-1 min-w-0">
                    <div className="font-medium text-sm text-gray-900 truncate">
                        {concept.label}
                    </div>
                </div>

                {/* Selection indicator */}
                {isSelected && (
                    <div className="absolute left-0 top-0 bottom-0 w-1 bg-blue-500 rounded-l-lg"></div>
                )}
            </div>

            {/* Children */}
            {isExpanded && concept.hasChildren && (
                <TreeExpandChildren
                    concept={concept}
                    level={level + 1}
                    onSelect={onSelect}
                    selectedConcept={selectedConcept}
                    searchTerm=""
                />
            )}
        </div>
    );
};

type TreeViewProps = {
    concepts: ArtefactTerm[];
    onSelect: (concept: ArtefactTerm) => void;
    level?: number;
    selectedConcept: ArtefactTerm | null;
    searchTerm?: string;
};

export const TreeView = ({
                             concepts,
                             onSelect,
                             level = 0,
                             selectedConcept,
                         }: TreeViewProps) => {
    const [expandedNodes, setExpandedNodes] = useState<Set<string>>(new Set());


    if (!concepts || concepts.length === 0) {
        return <div className="flex flex-col items-center justify-center p-8 text-gray-500">
            <Folder size={48} className="mb-4 text-gray-300"/>
            <p className="text-lg font-medium mb-2">Tree view not supported for this source</p>
            <p className="text-sm text-center">
                There are no concepts to display in this tree view.
            </p>
        </div>
    }

    return <div className="h-full bg-white">
            <div className="py-1" role="tree">
                {concepts.map((concept) => (
                    <TreeNode
                        key={concept.iri}
                        concept={concept}
                        level={level}
                        expandedNodes={expandedNodes}
                        setExpandedNodes={setExpandedNodes}
                        onSelect={onSelect}
                        selectedConcept={selectedConcept}
                        searchTerm=""
                    />
                ))}
            </div>
    </div>
};