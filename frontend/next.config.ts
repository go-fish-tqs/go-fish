import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    // NEXT_PUBLIC_* variables are automatically exposed to the browser
    images: {
        remotePatterns: [
            {
                protocol: 'https',
                hostname: '**',
            },
            {
                protocol: 'http',
                hostname: '**',
            },
        ],
        unoptimized: true, // Para development, desativa otimização de imagens
    },
};

export default nextConfig;
