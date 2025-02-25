export type RestResponse<R> = Promise<R>

export interface HttpClient {
    request<R>(requestConfig: {
        method: string;
        url: string;
        queryParams?: any;
        data?: any;
        copyFn?: (data: R) => R;
    }): RestResponse<R>;
}

export class RestApplicationClient {

    constructor(protected httpClient: HttpClient) {
        this.httpClient = httpClient;
    }


}

export function uriEncoding(template: string, ...substitutions: any[]): string {
    let result = "";
    for (let i = 0; i < substitutions.length; i++) {
        result += template[i];
        result += encodeURIComponent(substitutions[i]);
    }
    result += template[template.length - 1];
    return result;
}
