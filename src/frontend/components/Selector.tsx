import * as React from "react"

import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectLabel,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"

interface SelectorValue {
    label: string,
    value: string,
}

export function Selector({placeholder, label, values}: {
    placeholder?: string,
    label: string,
    values: Array<SelectorValue>
}) {
    return (
        <Select>
            <SelectTrigger className="w-[180px]">
                <SelectValue placeholder={placeholder}/>
            </SelectTrigger>
            <SelectContent>
                <SelectGroup>
                    <SelectLabel>{label}</SelectLabel>
                    {values.map((value, index) => (
                        <SelectItem value={value.value}>{value.value}</SelectItem>
                    ))}
                </SelectGroup>
            </SelectContent>
        </Select>
    )
}
