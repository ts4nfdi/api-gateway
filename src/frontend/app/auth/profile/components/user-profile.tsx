import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {useAuth} from "@/lib/authGuard";
import {router} from "next/client";
import {useRouter} from "next/navigation";

export default function UserProfile() {
    const {user, logout} = useAuth()
    const router = useRouter()
    return (
        <Card className="w-1/2">
            <CardHeader className="flex-row justify-between items-center">
                <CardTitle>Profile Information</CardTitle>
                <div className={"flex space-x-4"}>
                    <Button onClick={() => router.push('/')} >
                        Home
                    </Button>
                    <Button variant="destructive" onClick={logout}>
                        Logout
                    </Button>
                </div>
            </CardHeader>
            <CardContent>
                <div className="space-y-6">
                    <div className="flex justify-between border-b pb-4">
                        <div className="font-medium">Name</div>
                        <div>{user?.username}</div>
                    </div>

                    <div className="flex justify-between">
                        <div className="font-medium">Role</div>
                        <div>{user?.roles}</div>
                    </div>
                </div>
            </CardContent>
        </Card>
    )
}
