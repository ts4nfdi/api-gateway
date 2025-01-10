"use client";

import {useState} from "react";
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

    return (
        <div className="flex justify-center items-center h-screen bg-gray-50">
            <Card className="w-full max-w-md">
                <CardHeader>
                    <h2 className="text-lg font-semibold">Login</h2>
                </CardHeader>
                <CardContent>
                    {error && (
                        <Alert variant="destructive" className="mb-4">
                            {error}
                        </Alert>
                    )}
                    <form onSubmit={handleLogin} className="space-y-4">
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
                    <span>Don't have an account? </span>
                    <Link href="/auth/register" className="text-blue-600">Sign up</Link>
                    <span> or go back to </span>
                    <Link href="/" className="text-blue-600">Homepage</Link>

                </CardFooter>
            </Card>
        </div>
    );
}
