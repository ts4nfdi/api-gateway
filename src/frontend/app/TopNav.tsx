import {useAuth} from "@/lib/authGuard";
import Link from "next/link";
import {Button} from "@/components/ui/button";
import React from "react";

export const LoginButton = ({isLoggedIn = false}) => {
    return (<>
        {
            isLoggedIn ? (
                <Button className="bg-blue-600">
                    <Link href="/auth/profile">
                        My Profile
                    </Link>
                </Button>
            ) : (
                <Button className="bg-blue-600"><Link href={'/auth/login'}>Login</Link></Button>
            )
        }</>)
}

const LinkButton = ({href, children}: any) => {
    return (
        <Button variant={'link'} >
            <Link href={href}>{children}</Link>
        </Button>)
}
export const TopNav = ({showLink = true}) => {
    const {user, isLoading, logout, authRedirect} = useAuth()
    const isLoggedIn = user != null;

    return (
        <div>
            <header className="border-b">
                <div className="flex h-16 items-center px-4 container mx-auto">
                    <Link href='/' className='flex-1 flex items-center gap-2'>
                        <img src={'/api-gateway/logo.png'} alt="Icon" className="w-6 h-6"/>
                        <h1 className="text-xl font-bold">TSNFDI API Gateway</h1>
                    </Link>
                    {showLink && (
                        <div>
                            <LinkButton href={'/metadata'}>Metamodel</LinkButton>
                            <LinkButton href={'/compare'}>Examples</LinkButton>
                            <LinkButton href={'/status'}>Status</LinkButton>
                            <LoginButton isLoggedIn={isLoggedIn}/>
                        </div>
                    )}

                </div>
            </header>
        </div>
    );
};


