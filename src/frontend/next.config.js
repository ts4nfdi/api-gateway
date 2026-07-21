/** @type {import('next').NextConfig} */

module.exports = {
    output: 'export',
    basePath: '/api-gateway',
    env: {
        API_GATEWAY_URL: process.env.API_GATEWAY_URL || 'https://terminology.services.base4nfdi.de/api-gateway',
        SSO_AUTHORITY: process.env.SSO_AUTHORITY || 'https://infraproxy.nfdi-aai.dfn.de',
        SSO_CLIENT_ID: process.env.SSO_CLIENT_ID || 'https://terminology.services.base4nfdi.de',
        SSO_REDIRECT_URI: process.env.SSO_REDIRECT_URI || 'http://localhost:3000/api-gateway/auth/profile',
        SSO_CLIENT_SECRET: process.env.SSO_CLIENT_SECRET,
        SSO_AUTHORIZATION_ENDPOINT: process.env.SSO_AUTHORIZATION_ENDPOINT || 'https://infraproxy.nfdi-aai.dfn.de/idp/profile/oidc/authorize',
        SSO_ISSUER: process.env.SSO_ISSUER || 'https://infraproxy.nfdi-aai.dfn.de',
        SSO_USERINFO_ENDPOINT: process.env.SSO_USERINFO_ENDPOINT || 'https://infraproxy.nfdi-aai.dfn.de/idp/profile/oidc/userinfo',
        SSO_END_SESSION_ENDPOINT: process.env.SSO_END_SESSION_ENDPOINT || 'https://infraproxy.nfdi-aai.dfn.de/idp/profile/Logout',
        SSO_TOKEN_ENDPOINT: process.env.SSO_TOKEN_ENDPOINT || 'https://infraproxy.nfdi-aai.dfn.de/idp/profile/oidc/token',
    }
};
