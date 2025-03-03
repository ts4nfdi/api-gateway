import JsonViewerModal from "@/components/JsonVieweModal";
import React from "react";
import SmallLink from "@/components/SmallLink";

export default function APIUrlInput({url ,variant = 'badge'}:  {url: string,  variant: 'btn' | 'link' | 'badge'}) {
    return (
        <div className="flex space-x-2 items-center">
            {variant == 'link' && <SmallLink href={url}/>}
            <JsonViewerModal url={url} variant={variant}/>
        </div>
    )
}