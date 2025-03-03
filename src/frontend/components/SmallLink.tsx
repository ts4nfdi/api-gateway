import Link from "next/link";
import {buttonVariants} from "@/components/ui/button";
import React from "react";

export default function SmallLink({href}: any) {
    return (
        <Link target={'_blank'} href={href} className={buttonVariants({ variant: "link" }) + ' text-sm overflow-hidden text-ellipsis'} style={{display: 'block', padding: '0'}}>
            {href}
        </Link>
    );
}