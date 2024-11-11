'use client';

import { EuiProvider } from "@elastic/eui";
import { QueryClient, QueryClientProvider } from "react-query";
import "@elastic/eui/dist/eui_theme_light.css";
import { HomePage } from '@/app/HomePage'

export default function Home() {
    const queryClient = new QueryClient();
    return (
        <EuiProvider>
            <QueryClientProvider client={queryClient}>
                <HomePage apiUrl={`${process.env.API_GATEWAY_URL}/api-gateway`}/>
            </QueryClientProvider>
        </EuiProvider>
    )
}
