import {Card, CardContent, CardHeader} from "@/components/ui/card";
import {Skeleton} from "@/components/ui/skeleton";

export default function UserProfileLoader() {
    return (
        <Card>
            <CardHeader>
                <Skeleton className="h-6 w-48"/>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    <Skeleton className="h-4 w-full"/>
                    <Skeleton className="h-4 w-full"/>
                    <Skeleton className="h-4 w-full"/>
                </div>
            </CardContent>
        </Card>)
}
