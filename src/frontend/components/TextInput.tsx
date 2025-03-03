import {Input} from "@/components/ui/input";
import {X} from "lucide-react";
import React from "react";

export default function TextInput({value, onChange, placeholder, isClearable = true}: any) {
    const handleClear = () => {
        onChange({target: {value: ''}});
    };

    return (
        <div className="relative w-full">
            <Input
                type="text"
                placeholder={placeholder}
                value={value}
                onChange={onChange}
                className="pr-8 w-full"
            />
            {isClearable && value && (
                <button
                    onClick={handleClear}
                    className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                    type="button"
                >
                    <X size={16}/>
                </button>
            )}
        </div>
    );
};
