import Axios from 'axios'
import {RestApplicationClient} from "@/lib/RestClient";

require('dotenv').config()

const httpClient = Axios.create({
    baseURL: process.env.API_GATEWAY_URL,
    headers: {
        'X-Requested-With': 'XMLHttpRequest',
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
})

httpClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        if (!config.url?.includes("login") && token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

export const restClient = new RestApplicationClient(httpClient)

export default httpClient
