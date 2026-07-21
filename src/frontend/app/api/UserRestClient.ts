import httpClient from "@/lib/httpClient";
import {RestApplicationClient, RestResponse} from "@/lib/RestClient";

export interface LoginRequest {
    username: string;
    password: string;
}

export interface SsoLoginRequest {
    id_token: string;
    access_token: string;
}

export interface UserResponse {
    email?: string;
    username: string;
    roles?: Array<string>;
    oidcSubjectIdentifier? : string;
}

export interface LoginResponse {
    data: {
        username: string;
        token: string;
        role: string;
    }
}

export interface SsoTokenExchangeRequest {
    code: string;
    redirect_uri: string;
    code_verifier?: string;
}

export interface SsoTokenExchangeResponse {
    data: {
        access_token: string;
        scope: string;
        id_token: string;
        token_type: string;
        expires_in: bigint;
    }
}

export interface CreateUserRequest {
    username: string;
    password: string;
}

interface UpdateUserRequest {
    username: string;
    password: string;
}

export class UserRestClient extends RestApplicationClient {

    login(body: LoginRequest): RestResponse<LoginResponse> {
        return this.httpClient.request({method: "POST", url: '/auth/login', data: body});
    }

    ssoExchangeToken(body: SsoTokenExchangeRequest): RestResponse<SsoTokenExchangeResponse> {
        return this.httpClient.request({method: "POST", url: 'auth/sso/token', data: body});
    }

    ssoLogin(body: SsoLoginRequest): RestResponse<LoginResponse> {
        return this.httpClient.request({method: "POST", url: 'auth/sso/login', data: body});
    }

    logout(): RestResponse<void> {
        return this.httpClient.request({method: "POST", url: '/auth/logout'});
    }

    getAllUsers(): RestResponse<UserResponse[]> {
        return this.httpClient.request({method: 'GET', url: '/users/'})
    }
    // getSession(): RestResponse<UserResponse> {
    //     // return this.httpClient.request({method: "GET", url: 'api/auth/me'});
    // }

    createUser(request: CreateUserRequest): RestResponse<UserResponse> {
        return this.httpClient.request({method: "POST", url: 'auth/register', data: request});
    }

    // updateUser(id: string, request: UpdateUserRequest): RestResponse<UserResponse> {
    //     return this.httpClient.request({method: "PUT", url: 'api/auth/users', data: request});
    // }

}

export const userRestClient = new UserRestClient(httpClient)
