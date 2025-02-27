import React from "react";
import {Loader, Loader2} from "lucide-react";

export function Loading() {
    return (<div className="flex justify-center w-full py-4">
        <Loader2 className="h-8 w-8 animate-spin text-gray-500"/>
    </div>)
}
