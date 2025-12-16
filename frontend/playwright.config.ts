import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright configuration for GoFish E2E tests
 */
export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    forbidOnly: !!process.env.CI,
    retries: process.env.CI ? 1 : 0,
    workers: process.env.CI ? 10 : undefined,
    reporter: process.env.CI ? 'github' : 'html',
    timeout: 30000,

    use: {
        baseURL: 'http://localhost:3000',
        trace: 'off',
        screenshot: 'only-on-failure',
        actionTimeout: 10000,
        navigationTimeout: 15000,
    },

    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] },
        },
    ],
});

