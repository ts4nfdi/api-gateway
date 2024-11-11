/** @type {import('next').NextConfig} */

module.exports = {
    output: 'export',
    basePath: '/api-gateway',
    env: {
        API_GATEWAY_URL: process.env.API_GATEWAY_URL || 'https://ts4nfdi-api-gateway.prod.km.k8s.zbmed.de'
    }
};
