'use client'

import React, {createContext, useContext, useEffect, useState} from 'react'
import {useRouter} from 'next/navigation'
import httpClient from '@/lib/httpClient'
import {LoginRequest, UserResponse, userRestClient} from "@/app/api/UserRestClient";

interface AuthConfig {
    loginRedirect?: string
    authRedirect?: string
    guestRedirect?: string
}

interface AuthContextType {
    user: UserResponse | null
    login: (params: {
        credentials: LoginRequest;
        onError: (error?: string) => void;
        redirect?: string;
    }) => Promise<void>
    logout: () => Promise<void>
    isLoading: boolean,
    authRedirect: string
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

interface AuthProviderProps {
    children: React.ReactNode
    config?: AuthConfig
}

export function AuthProvider({children, config = {}}: AuthProviderProps) {
    const {
        loginRedirect = '/profile',
        authRedirect = '/login',
        guestRedirect = '/profile'
    } = config

    const [user, setUser] = useState<UserResponse | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const router = useRouter()

    useEffect(() => {
        const initAuth = async () => {
            try {
                const response = await httpClient.get<UserResponse>('/auth/me')
                setUser(response.data)
            } catch (error) {
                setUser(null)
                localStorage.removeItem('token')
            } finally {
                setIsLoading(false)
            }
        }

        initAuth()
    }, [])

    const login = async ({
                             credentials,
                             onError,
                             redirect = loginRedirect
                         }: {
        credentials: LoginRequest
        onError: (error?: string) => void
        redirect?: string
    }) => {
        try {
            const response = await userRestClient.login(credentials)
            localStorage.setItem('token', response.data.token)
            const userResponse = await httpClient.get<UserResponse>('/auth/me')
            setUser(userResponse.data)
            onError(undefined)
            router.push(redirect)
        } catch (err: any) {
            onError(err.response?.data?.message || 'An unexpected error occurred')
        }
    }

    const logout = async () => {
        try {
            await userRestClient.logout()
        } finally {
            setUser(null)
            localStorage.removeItem('token')
            router.push(authRedirect)
        }
    }

    useEffect(() => {
        if (window.location.pathname === authRedirect && user) {
            router.push(guestRedirect)
        }
    }, [user, router, guestRedirect, authRedirect])

    return (
        <AuthContext.Provider value={{user, login, logout, isLoading, authRedirect}}>
            {children}
        </AuthContext.Provider>
    )
}

export const useAuth = () => {
    const context = useContext(AuthContext)
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider')
    }
    return context
}
