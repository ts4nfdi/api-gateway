"use client"

import {useAuth} from "@/lib/authGuard";
import {useRouter} from "next/navigation";
import {useEffect} from "react";
import UserProfileLoader from "@/app/auth/profile/components/user-profile-loader";
import UserProfile from "@/app/auth/profile/components/user-profile";
import CollectionsTable from "@/app/auth/profile/components/collections";
import {PageHeader} from "@/app/Header";

export default function Profile() {
    const {user, isLoading, logout, authRedirect} = useAuth()
    const router = useRouter()

    useEffect(() => {
        if (!isLoading && !user) {
            router.push(authRedirect)
        }
    }, [user, isLoading, router])

    if (isLoading) {
        return <UserProfileLoader/>
    }

    if (!user) {
        return null
    }

    return (
        <>

            <div>
                <UserProfile/>
            </div>
            <div className="space-y-2">
                <CollectionsTable/>
            </div>
        </>
    )

}
