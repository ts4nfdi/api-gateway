'use client';

import {QueryClient, QueryClientProvider} from "react-query";
import {MainPage} from "@/app/home/MainPage";

export default function Home() {
    const queryClient = new QueryClient();
    return (
        <QueryClientProvider client={queryClient}>
            <MainPage apiUrl={`${process.env.API_GATEWAY_URL}`}/>
        </QueryClientProvider>
    )
}
