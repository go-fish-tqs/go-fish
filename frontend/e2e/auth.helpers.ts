import { Page } from '@playwright/test';

/**
 * Test credentials
 */
export const TEST_USERS = {
    admin: {
        email: 'admin@gofish.pt',
        password: 'admin123',
    },
    regular: {
        name: 'Test User',
        email: `test-${Date.now()}@playwright.com`,
        password: 'Test123!',
        location: 'Lisbon',
    },
};

/**
 * Generate unique test user credentials
 */
export function generateTestUser(prefix: string) {
    const timestamp = Date.now();
    return {
        name: `${prefix} Test User`,
        email: `${prefix.toLowerCase()}-${timestamp}@playwright.com`,
        password: 'Test123!',
        location: 'Lisbon',
    };
}

/**
 * Login helper function
 */
export async function login(page: Page, email: string, password: string): Promise<void> {
    await page.goto('/login');
    await page.locator('input[name="email"]').fill(email);
    await page.locator('input[name="password"]').fill(password);
    await page.locator('button[type="submit"]').click();

    // Wait for either successful redirect or error message
    const result = await Promise.race([
        page.waitForURL(/\/(admin|dashboard|items)/, { timeout: 15000 })
            .then(() => ({ success: true, error: null })),
        page.locator('.bg-red-50, [class*="error"], [class*="red"]').first()
            .waitFor({ state: 'visible', timeout: 15000 })
            .then(async () => {
                const errorText = await page.locator('.bg-red-50 p, [class*="error"]').first().textContent();
                return { success: false, error: errorText || 'Login failed with unknown error' };
            }),
    ]);

    if (!result.success) {
        throw new Error(`Login failed for ${email}: ${result.error}`);
    }
}

/**
 * Logout helper function - clears storage and navigates to login
 */
export async function logout(page: Page): Promise<void> {
    await page.evaluate(() => {
        localStorage.clear();
    });
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
}

/**
 * Register helper function
 */
export async function register(
    page: Page,
    name: string,
    email: string,
    password: string,
    location: string
): Promise<void> {
    await page.goto('/register');
    await page.locator('input[name="name"]').fill(name);
    await page.locator('input[name="email"]').fill(email);
    await page.locator('input[name="password"]').fill(password);
    await page.locator('input[name="confirmPassword"]').fill(password);
    await page.locator('input[name="location"]').fill(location);
    await page.locator('button[type="submit"]').click();
}

/**
 * Register and login a new user in one flow
 */
export async function registerAndLogin(
    page: Page,
    name: string,
    email: string,
    password: string,
    location: string
): Promise<void> {
    // Handle alert dialogs
    page.on('dialog', dialog => dialog.accept());

    await register(page, name, email, password, location);
    await page.waitForURL('/login', { timeout: 10000 });

    await login(page, email, password);
}
