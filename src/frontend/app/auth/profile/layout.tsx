import React from "react";

export default function layout({children}: { children: React.ReactNode }) {
    return (<div className="container mx-auto  flex flex-col space-y-4 py-12">
        {children}
    </div>)
}
