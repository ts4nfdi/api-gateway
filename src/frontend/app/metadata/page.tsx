'use client';
import React, {useState} from 'react';
import {Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow} from '@/components/ui/table';
import {Card, CardContent, CardHeader, CardTitle} from '@/components/ui/card';
import {Tabs, TabsContent, TabsList, TabsTrigger} from '@/components/ui/tabs';
import {Badge} from '@/components/ui/badge';
import {TopNav} from "@/app/TopNav";
import {Button} from "@/components/ui/button";
import {Loading} from "@/components/Loading";
import {configurationRestClient} from "@/lib/ConfigurationRestClient";

const MetadataMappingTable = ({data, type}: any) => {
    const [activeTab, setActiveTab] = useState("all");

    if (!data) return null;

    const allFields = Object.keys(Object.values(data)[0] as Object);
    const databases = Object.keys(data);

    const hasAnyValues = (fieldName: string | number) => {
        return databases.some(system => data[system][fieldName] !== null);
    };

    const fieldsWithValues = allFields.filter(hasAnyValues);

    return (
        <Card className="w-full">
            <CardHeader>
                <CardTitle>{type.toString().toUpperCase()} Field Mapping</CardTitle>
            </CardHeader>
            <CardContent>
                <Tabs defaultValue="all" onValueChange={setActiveTab}>
                    <TabsList className="mb-4">
                        <TabsTrigger value="all">All Fields</TabsTrigger>
                        <TabsTrigger value="filled">Non-Empty Fields</TabsTrigger>
                    </TabsList>
                    <TabsContent value="all">
                        <RenderTable data={data} fields={allFields}/>
                    </TabsContent>
                    <TabsContent value="filled">
                        <RenderTable data={data} fields={fieldsWithValues}/>
                    </TabsContent>
                </Tabs>
            </CardContent>
        </Card>
    );
};

const RendValue = ({value}: any) => {
    return value ? (
        <Badge variant="outline" className="bg-blue-50">
            {value}
        </Badge>
    ) : (
        <span className="text-gray-300">-</span>
    )

}
const RenderTable = ({data, fields}: any) => {
    const databases = Object.keys(data);
    const noRender = ['nestedJson', 'key'];
    const countNotEmpty = (database: string) => {
        const notEmptyCount = fields.filter((field: string) => data[database][field] !== null && !noRender.includes(field)).length;
        const totalCount = fields.filter((field: string) => !noRender.includes(field)).length;
        return (notEmptyCount / totalCount * 100).toFixed(2);
    }

    const nestedJsonValue = (system: string) => {
        let nestedJson = data[system]['nestedJson'];
        const key = data[system]['key'];
        if(key) {
            nestedJson =  `${nestedJson} -> ${key}`;
        }
        return nestedJson;
    }

    return (
        <div className="overflow-x-auto">
            <Table>
                <TableCaption>Comparison of field mappings across different terminology systems</TableCaption>
                <TableHeader>
                    <TableRow>
                        <TableHead>Field</TableHead>
                        {databases.map(system => (
                            <TableHead key={system} className="capitalize">
                                {system} (Mapped: {countNotEmpty(system)} %)
                            </TableHead>
                        ))}
                    </TableRow>
                </TableHeader>
                <TableBody>
                    <TableRow>
                        <TableCell className="font-medium">Path to item</TableCell>
                        {databases.map(system => (
                            <TableCell key={system}>
                                <RendValue value={nestedJsonValue(system)}/>
                            </TableCell>
                        ))}
                    </TableRow>
                    <TableRow>
                        <TableCell colSpan={databases.length + 1}></TableCell>
                    </TableRow>
                    {fields.map((field: any) => (
                        !noRender.includes(field) &&
                        <TableRow key={field}>
                            <TableCell className="font-medium">{field}</TableCell>
                            {databases.map(system => (
                                <TableCell key={`${system}-${field}`}>
                                    <RendValue value={data[system][field]}/>
                                </TableCell>
                            ))}
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
};

export default function Metadata() {
    const [loading, setLoading] = useState(false);
    const [metadata, setMetadata] = useState(null);
    const [type, setType] = useState('');
    const [error, setError] = useState(null);
    const fetchMetadata = async (type: string) => {
        setType(type);
        setLoading(true);
        setMetadata(null);
        configurationRestClient.getMetadata(type).then((response) => {
            setMetadata(response.data);
        }).catch((e) => {
            setError(e.message);
        }).finally(() => setLoading(false))
    }


    return (<>
        <TopNav/>
        <div className={'container mx-auto p-4 space-y-2'}>
            <h1 className={'text-4xl'}>API Gateway Metamodel</h1>
            <p className={'text-gray-500 text-lg font-light w-2/3'}>
                Metadata is data that provides information about other data, we federate metadata from different
                systems in a unified metamodel to provide a common ground across the services that we federate with.
            </p>
            <div className={'space-x-4 flex'}>
                <Button variant="outline" onClick={() => fetchMetadata('artefact')}>Get Artefact metamodel</Button>
                <Button variant="outline" onClick={() => fetchMetadata('search')}>Get Search term metamodel</Button>
                <Button variant="outline" onClick={() => fetchMetadata('term')}>Get Term metamodel</Button>
            </div>
            {loading && <Loading/>}
            {error && <div className="text-red-600">{error}</div>}
            {metadata && <MetadataMappingTable data={metadata} type={type}/>}
        </div>
    </>)
}