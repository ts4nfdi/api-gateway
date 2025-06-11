/** @type {import('next').NextConfig} */

module.exports = {
    output: 'export',
    basePath: '/api-gateway',
    env: {
        API_GATEWAY_URL: process.env.API_GATEWAY_URL || 'https://terminology.services.base4nfdi.de/api-gateway'
    }
};
