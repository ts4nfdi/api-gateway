'use client'
import React, {useState} from "react";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardHeader} from "@/components/ui/card";
import {userRestClient} from "@/app/auth/lib/UserRestClient";
import {useRouter} from "next/navigation";
import {Alert} from "@/components/ui/alert";
import {useAuth} from "@/lib/authGuard";

export default function RegisterPage() {
    const [error, setError] = useState("");
    const router = useRouter();
    const [formData, setFormData] = useState({
        username: "",
        password: "",
        role: "user",
    });
    const {login} = useAuth();

    const handleInputChange = (e: any) => {
        const {name, value} = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        try {
            const response: any = await userRestClient.createUser(formData)
            if (response.status === 201)
                await login({
                    credentials: {username: formData.username, password: formData.password},
                    onError: error => setError(error || '')
                })
        } catch (error: any) {
            setError(error)
        }
    };

    return (

        <div className="flex justify-center items-center h-screen bg-gray-50">
            <Card className="w-full max-w-md">
                <CardHeader>
                    <h2 className="text-lg font-semibold">Register</h2>
                </CardHeader>
                <CardContent>
                    {error && (
                        <Alert variant="destructive" className="mb-4">
                            {error.toString()}
                        </Alert>
                    )}
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <Label htmlFor="username">Username</Label>
                            <Input
                                id="username"
                                name="username"
                                type="text"
                                placeholder="Enter your username"
                                value={formData.username}
                                onChange={handleInputChange}
                                required
                            />
                        </div>

                        <div>
                            <Label htmlFor="password">Password</Label>
                            <Input
                                id="password"
                                name="password"
                                type="password"
                                placeholder="Enter your password"
                                value={formData.password}
                                onChange={handleInputChange}
                                required
                            />
                        </div>

                        <Button type="submit" className="w-full">
                            Register
                        </Button>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}
