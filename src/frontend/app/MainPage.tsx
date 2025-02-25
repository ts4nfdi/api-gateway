import React from 'react';
import {CardContent, CardHeader} from "@/components/ui/card";
import PageHeader from './Header';
import {Separator} from "@/components/ui/separator";
import ArtefactsTable from "@/app/home/browse/BrowseResources";
import Autocomplete from "@/app/home/search/AutoComplete";


export function MainPage({apiUrl}: { apiUrl: string }) {
    return (<>
            <PageHeader/>
            <div className="bg-white w-full">
                <div className='container mx-auto space-y-2'>
                    <CardHeader>
                        <h2 className='text-2xl font-semibold text-gray-900'>Find a concept</h2>
                    </CardHeader>
                    <CardContent>
                        <Autocomplete apiUrl={`${apiUrl}/search?query=`}/>
                    </CardContent>
                </div>
                <Separator/>
                <div className='bg-white w-full'>
                    <div className='container mx-auto my-5 space-y-2'>
                        <CardHeader>
                            <h2 className='text-2xl font-semibold text-gray-900'>Browse resources</h2>
                        </CardHeader>
                        <CardContent>
                            <ArtefactsTable apiUrl={`${apiUrl}/artefacts?showResponseConfiguration=true`}/>
                        </CardContent>
                    </div>
                </div>
            </div>
        </>
    )
}
