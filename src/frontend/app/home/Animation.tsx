import React, {useEffect, useState} from 'react';
import {motion} from 'framer-motion';
import {Cloud, Cog, Database, FileJson, Server} from 'lucide-react';
import dedent from "dedent";

const steps = [
    'Initializing data sources...',
    'Connecting to OntoPortal...',
    'Fetching data from Ols...',
    'Retrieving SKOSMOS data...',
    // 'Processing JSKOS data...',
    // 'Retrieving GND data...',
    'Processing and merging data into an unified model based on MOD...'
]
const ProgressBars = ({step}: any) => {
    return <div className="mt-8 flex justify-center gap-2">
        {Array.from({length: steps.length}).map((_, index) => (
            <div
                key={`step-${index}`}
                className={`h-2 w-8 rounded-full ${index < step ? 'bg-blue-500' : 'bg-white'}`}
            />
        ))}
    </div>
}

const StatusMessage = ({step}: any) => {
    return <div className="mt-4 text-center text-lg font-medium">
        {steps[Math.min(step, steps.length - 1)]}
    </div>
}

const UnifiedJsonPreview = ({nodes}: any) => {
    const json = dedent`{
        sources: 5,
        records: 5353,
        collection: [{
            uri: "http://protege.org/pizza",
            label: "Pizza Ontology",
            synonyms: ['Pizza', 'Pizza Ontology'],
            description: ['The Pizza Ontology is an ontology that describes pizzas and related things.'],
            },...
        ]
    }`;
    const finalNode = nodes.find((node: any) => node.id === finalNodeId);

    return <motion.div
        className="absolute"
        style={{
            left: `${finalNode.x + 30}px`,
            top: `${finalNode.y - 2 * nodeSize}px`,
            width: '300px',
            overflow: 'hidden'
        }}
        initial={{opacity: 0, x: 20}}
        animate={{opacity: 1, x: 0}}
        transition={{delay: 0.5}}
    >
        <div className="bg-gray-800 p-3 rounded-lg text-xs font-mono w-full">
            <div className="mb-1 flex items-center gap-1 text-green-400">
                <FileJson size={12}/>
                <span>unified-data.jsonld</span>
            </div>
            <div className="text-gray-300 text-xs">
                <pre>{json}</pre>
            </div>
        </div>
    </motion.div>
}

const nodeSize = 100;
const nodeMiddle = nodeSize / 2 - 16;
const centerNodeId = 6;
const finalNodeId = 7;

const DrawEdges = ({edges, nodes}: any) => {

    const getLineCoordinates = (from: number, to: number) => {
        const fromNode: any = nodes.find((node: any) => node.id === from);
        const toNode: any = nodes.find((node: any) => node.id === to);

        return {
            x1: fromNode.x + nodeMiddle,
            y1: fromNode.y + nodeMiddle,
            x2: toNode.x + nodeMiddle,
            y2: toNode.y + nodeMiddle
        };
    };
    return edges.map((edge: any, index: number) => {
        const {x1, y1, x2, y2} = getLineCoordinates(edge.from, edge.to);
        return (
            <g key={`edge-${index}`}>
                <line
                    x1={x1}
                    y1={y1}
                    x2={x2}
                    y2={y2}
                    stroke={edge.active ? "#ffffff" : "#4b5563"}
                    strokeWidth="2"
                    strokeDasharray={edge.active ? "0" : "5,5"}
                />

                {edge.active && (
                    <motion.circle
                        cx={x1}
                        cy={y1}
                        r={4}
                        fill="#ffffff"
                        initial={{cx: x1, cy: y1}}
                        animate={{cx: x2, cy: y2}}
                        transition={{
                            duration: 1.5,
                            repeat: Infinity,
                            repeatType: "loop"
                        }}
                    />
                )}
            </g>
        );
    })
}

const DrawCenterEdge = ({nodes}: any) => {
    const centerNode = nodes.find((node: any) => node.id === centerNodeId);
    return <motion.circle
        cx={centerNode.x + nodeMiddle + 20}
        cy={centerNode.y + nodeMiddle}
        r={40}
        fill="none"
        stroke="#10b981"
        strokeWidth="2"
        initial={{r: 40, opacity: 0}}
        animate={{r: 60, opacity: 0.5}}
        transition={{
            duration: 1.5,
            repeat: Infinity,
            repeatType: "loop"
        }}
    />
}

const DrawNodes = ({nodes, step}: any) => {

    return nodes.map((node: any, index: number) => (
        <motion.div
            key={`node-${node.id}`}
            className="absolute flex flex-col items-center"
            style={{
                left: `${node.x}px`,
                top: `${node.y}px`,
                transform: 'translate(-50%, -50%)'
            }}
            initial={{opacity: 0, scale: 0.8}}
            animate={{
                opacity: node.id <= step || node.id === 6 ? 1 : 0.6,
                scale: node.id <= step || node.id === 6 ? 1 : 0.8
            }}
            transition={{delay: index * 0.1}}
        >
            <div
                className={`${node.color} p-4 rounded-lg shadow-lg flex items-center justify-center h-16 w-16 ${node.id === 6 && step >= 6 ? 'ring-2 ring-green-500' : ''}`}>
                {node.id === 6 ? (
                    <motion.div
                        animate={{rotate: 360}}
                        transition={{duration: 3, repeat: Infinity, ease: "linear"}}
                    >
                        <node.icon size={32} className={step >= 6 ? 'text-green-300' : 'text-white'}/>
                    </motion.div>
                ) : (
                    <node.icon size={32}/>
                )}
            </div>
            <div className="mt-2 px-3 py-1 bg-gray-800 rounded text-sm whitespace-nowrap">
                {node.name}
            </div>

            {/* Show status for processed nodes */}
            {node.id < 6 && node.id <= step && (
                <div
                    className="absolute -top-3  h-6 w-6 bg-green-500 rounded-full flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-white"
                         viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd"
                              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                              clipRule="evenodd"/>
                    </svg>
                </div>
            )}
        </motion.div>
    ))
}

const DataMergeGraph = ({width, height}: any) => {
    const [step, setStep] = useState(0);

    // Define the nodes with improved positioning
    const nodes = [
        {id: 1, name: 'OntoPortal', icon: Database, color: 'bg-blue-500', x: 0, y: 0},
        {id: 2, name: 'OLS', icon: Server, color: 'bg-green-500', x: 0, y: height / 2 - nodeMiddle},
        {id: 3, name: 'SKOSMOS', icon: Cloud, color: 'bg-purple-500', x: 0, y: height - nodeSize},
        // {id: 4, name: 'JSKOS', icon: Globe, color: 'bg-pink-500', x: 350, y: 150},
        // {id: 5, name: 'GND', icon: Database, color: 'bg-yellow-500', x: 350, y: 250},
        {
            id: 6,
            name: 'Our Gateway',
            icon: Cog,
            color: 'bg-gray-700',
            x: width / 2 - nodeSize / 2,
            y: height / 2 - nodeMiddle
        },
        {
            id: 7,
            name: 'Unified Model',
            icon: FileJson,
            color: 'bg-gradient-to-r from-blue-600 to-purple-600',
            x: width - (nodeSize * 1.5),
            y: height / 2
        }
    ];

    // Define the edges (connections between nodes)
    const edges = [
        {from: 1, to: 6, active: step >= 1},
        {from: 2, to: 6, active: step >= 2},
        {from: 3, to: 6, active: step >= 3},
        // {from: 4, to: 6, active: step >= 4},
        // {from: 5, to: 6, active: step >= 5},
        {from: 6, to: 7, active: step >= 6}
    ];

    useEffect(() => {
        const timer = setInterval(() => {
            setStep(prevStep => {
                if (prevStep < 7) {
                    return prevStep + 1;
                } else {
                    clearInterval(timer);
                    return prevStep;
                }
            });
        }, 800);

        return () => clearInterval(timer);
    }, []);


    return (
        <div>
            <div className={`relative text-white`} style={{height: `${height}px`, width: `${width}px`}}>
                <svg width="100%" height="100%" className="absolute top-0 left-0">
                    {/* Draw edges */}
                    <DrawEdges edges={edges} nodes={nodes}/>

                    {/* Highlight active central node */}
                    {step >= 6 && <DrawCenterEdge nodes={nodes}/>}
                </svg>

                {/* Render nodes */}
                <DrawNodes nodes={nodes} step={step}/>

                {/* Unified JSON output preview */}
                {step >= 6 && <UnifiedJsonPreview nodes={nodes}/>}

            </div>
            <div className="text-center mt-4 text-sm text-gray-400">
                <ProgressBars step={step}/>
                <StatusMessage step={step}/>
            </div>
        </div>
    );
};

export default DataMergeGraph;