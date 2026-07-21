"use client";

import React, {useState} from "react";
import {Card, CardContent, CardFooter, CardHeader} from "@/components/ui/card";
import {Label} from "@/components/ui/label";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {Alert} from "@/components/ui/alert";
import {useAuth} from "@/lib/authGuard";
import Link from "next/link";

export default function LoginPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const {login} = useAuth();

    const handleLogin = async (e: any) => {
        e.preventDefault();
        setError("");

        if (!username || !password) {
            setError("Please fill in both fields.");
            return;
        }

        await login({
            credentials: {username: username, password: password},
            onError: error => setError(error || '')
        })
    };

    const [isLegacyFormVisible, setIsLegacyFormVisible] = useState(false);

    return (
        <div className="flex justify-center items-center h-screen bg-gray-50">
            <Card className="w-full max-w-md">
                <CardHeader>
                    <h2 className="text-lg font-semibold">Login</h2>
                </CardHeader>
                <CardContent>
                    <div className="mt-4 pt-2">
                        <p className="text-sm text-center text-gray-500 mb-4">Sign in with</p>
                        <div className="grid grid-cols-1 gap-3">
                            <SsoSignin/>
                        </div>
                    </div>

                    <div
                        className="mt-8 py-3 flex items-center text-sm text-stone-800 before:flex-1 before:border-t before:border-stone-200 before:me-6 after:flex-1 after:border-t after:border-stone-200 after:ms-6 dark:text-neutral-200 dark:before:border-neutral-600 dark:after:border-neutral-600">or
                    </div>

                    {error && (
                        <Alert variant="destructive" className="mb-4">
                            {error}
                        </Alert>
                    )}
                    <div className="text-center"><Button onClick={() => setIsLegacyFormVisible(!isLegacyFormVisible)}>Use legacy login</Button></div>
                    <form onSubmit={handleLogin} className={(isLegacyFormVisible ? "block" : "hidden") + " space-y-4 "}>
                        <div>
                            <Label htmlFor="username" className="block text-sm font-medium">
                                Username
                            </Label>
                            <Input
                                id="username"
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                placeholder="Enter your username"
                                className="mt-1"
                            />
                        </div>
                        <div>
                            <Label htmlFor="password" className="block text-sm font-medium">
                                Password
                            </Label>
                            <Input
                                id="password"
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="Enter your password"
                                className="mt-1"
                            />
                        </div>
                        <Button type="submit" variant="default" className="w-full">
                            Log In
                        </Button>
                    </form>
                </CardContent>
                <CardFooter className="text-sm space-x-1 text-gray-500">
                    <span>Don&#39;t have an account? </span>
                    <Link href="/auth/register" className="text-blue-600">Sign up</Link>
                    <span> or go back to </span>
                    <Link href="/" className="text-blue-600">Homepage</Link>
                </CardFooter>
            </Card>
        </div>
    );
}

const SsoSignin = () => {
    const {ssoAuthorize} = useAuth();

    const handleSsoAuthorization = async (e: any) => {
        e.preventDefault();
        await ssoAuthorize();
    }

    return (
        <button
            onClick={handleSsoAuthorization}
            className="flex flex-col items-center justify-center p-5 border border-gray-200 rounded-xl hover:shadow-md hover:bg-gray-50 transition text-sm font-semibold text-gray-700 bg-white">
            <img src="/api-gateway/media/logos/iam4nfdi.png"
                 className="w-24 h-24 object-contain mb-3"
                 alt="IAM4NFDI Infrastructure Proxy"/>
            <span>IAM4NFDI Infrastructure Proxy</span>
        </button>
    )

}