import React from 'react';
import {Separator} from "@/components/ui/separator";
import ArtefactsTable from "@/app/home/browse/BrowseResources";
import Autocomplete from "@/app/home/search/AutoComplete";
import Header from "@/app/home/Header";


export function MainPage({apiUrl}: { apiUrl: string }) {
    return (<>
            <Header/>
            <div className="bg-white w-full">
                <div className='container mx-auto space-y-2'>
                    <Autocomplete apiUrl={`${apiUrl}/search?query=`}/>
                </div>
                <Separator/>
                <div className='bg-white w-full min-h-[50vh]'>
                    <div className='container mx-auto space-y-2'>
                        <ArtefactsTable apiUrl={`${apiUrl}/artefacts?showResponseConfiguration=true`}/>
                    </div>
                </div>
            </div>
        </>
    )
}
