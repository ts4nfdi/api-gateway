'use client';

import { EuiProvider } from "@elastic/eui";
import { QueryClient, QueryClientProvider } from "react-query";
import "@elastic/eui/dist/eui_theme_light.css";
import { MainPage } from '@/app/MainPage'

export default function Home() {
    const queryClient = new QueryClient();
    return (
        <EuiProvider>
            <QueryClientProvider client={queryClient}>
                <MainPage/>
            </QueryClientProvider>
        </EuiProvider>
    )
}
