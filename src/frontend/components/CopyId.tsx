import {useState} from "react";
import {Button} from "@/components/ui/button";

export default function UUIDDisplay({value}: { value: string }) {
    const [uuid] = useState(value);
    const [copied, setCopied] = useState(false);

    const handleCopy = () => {
        navigator.clipboard.writeText(uuid);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <div className="flex space-x-5 items-center">
            <span>{uuid}</span>
            <Button variant="outline" onClick={handleCopy} title="Copy to clipboard">
                {copied ? "✅" : "📋"}
            </Button>
        </div>
    );
}
