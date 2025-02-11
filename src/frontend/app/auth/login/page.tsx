import {Card, CardHeader} from "@/components/ui/card";

export default function login() {
    return (
        <div className='flex justify-center h-screen bg-gray-50'>
            <Card className={'mt-12 h-1/4 align-middle flex items-center'}>
                <CardHeader>
                    Not yet enabled, come back soon to use authentification
                </CardHeader>
            </Card>
        </div>
    )

}
