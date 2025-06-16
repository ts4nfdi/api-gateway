import {type ClassValue, clsx} from "clsx"
import {twMerge} from "tailwind-merge"
import {Artefact} from "@/app/api/ArtefactsRestClient";

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs))
}


function debounce(this: any, func: { (query: any): void; apply?: any; }, wait: number) {
    let timeout: string | number | NodeJS.Timeout | undefined;
    return (...args: any) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

export function msToSeconds(ms: number): string {
    if (ms < 1000) {
        return `${ms} ms`;
    }
    const seconds = (ms / 1000).toFixed(2);
    return `${seconds} s`;
}

export const getSourcesFromArtefacts = (artefacts: Artefact[]) => {
    if (!artefacts || artefacts.length === 0) {
        return [];
    }
    const sources = artefacts.map((artefact: Artefact) => artefact.source_name || artefact.source);
    return Array.from(new Set(sources));
}
