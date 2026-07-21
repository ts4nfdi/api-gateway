"use client"

import {useAuth} from "@/lib/authGuard";
import {useRouter, useSearchParams} from "next/navigation";
import {useEffect} from "react";
import UserProfileLoader from "@/app/auth/profile/components/user-profile-loader";
import UserProfile from "@/app/auth/profile/components/user-profile";
import CollectionsTable from "@/app/auth/profile/components/collections";

export default function Profile() {
    const {user, isLoading, ssoLogin, authRedirect} = useAuth()
    const router = useRouter()

    const searchParams = useSearchParams();
    useEffect(() => {
        const oidcAuthCode = searchParams.get("code");
        if (isLoading && oidcAuthCode) {
            const exchangeTokenAndLogin = async () => {
                await ssoLogin({
                    authCode: {
                        code: oidcAuthCode,
                        redirect_uri: `${process.env.SSO_REDIRECT_URI}`},
                    onError: error => {/* TODO */}
                })
            }

            exchangeTokenAndLogin()
        }
    }, [isLoading, searchParams, ssoLogin]);

    useEffect(() => {
        if (!isLoading && !user) {
            router.push(authRedirect)
        }
    }, [user, isLoading, router, authRedirect])

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
