import type {Metadata} from 'next'
import {Inter} from 'next/font/google'
import './globals.css'
import {AuthProvider} from "@/lib/authGuard";

const inter = Inter({subsets: ['latin']})

export const metadata: Metadata = {
    title: 'Widgets Demo',
    description: 'Widgets demo',
}

export default function RootLayout({
                                       children,
                                   }: {
    children: React.ReactNode
}) {
    return (
        <html lang="en">
        <body className={inter.className + " bg-gray-100"}>
        <AuthProvider
            config={{loginRedirect: '/auth/profile', guestRedirect: '/auth/profile', authRedirect: '/auth/login'}}>
            {children}
        </AuthProvider>
        </body>
        </html>
    )
}
