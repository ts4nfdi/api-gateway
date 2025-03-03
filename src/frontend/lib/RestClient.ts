export type RestResponse<R> = Promise<R>

export interface HttpClient {
    request<R>(requestConfig: {
        method: string;
        url: string;
        params?: any;
        data?: any;
        copyFn?: (data: R) => R;
    }): RestResponse<R>;
}

export class RestApplicationClient {

    constructor(protected httpClient: HttpClient) {
        this.httpClient = httpClient;
    }
}

