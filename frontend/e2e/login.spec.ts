import { test, expect } from '@playwright/test';
import { TEST_USERS } from './auth.helpers';

/**
 * Login Feature Tests
 * Based on: login.feature
 */
test.describe('User Login', () => {
    test('should show login page with required elements', async ({ page }) => {
        await page.goto('/login');

        await expect(page.locator('input[name="email"]')).toBeVisible();
        await expect(page.locator('input[name="password"]')).toBeVisible();
        await expect(page.locator('button[type="submit"]')).toBeVisible();
    });

    test('should login successfully with valid credentials', async ({ page }) => {
        await page.goto('/login');

        await page.locator('input[name="email"]').fill(TEST_USERS.admin.email);
        await page.locator('input[name="password"]').fill(TEST_USERS.admin.password);
        await page.locator('button[type="submit"]').click();

        // Should redirect after login
        await page.waitForURL(/\/(admin|dashboard)/);

        // Should see sidebar navigation
        await expect(page.locator('aside')).toBeVisible();
    });

    test('should show error with invalid credentials', async ({ page }) => {
        await page.goto('/login');

        await page.locator('input[name="email"]').fill('invalid@test.com');
        await page.locator('input[name="password"]').fill('wrongpassword');
        await page.locator('button[type="submit"]').click();

        // Should remain on login page
        await expect(page).toHaveURL('/login');

        // Should show error message
        await expect(page.locator('[class*="red"]').first()).toBeVisible({ timeout: 5000 });
    });

    test('should have link to register page', async ({ page }) => {
        await page.goto('/login');

        const registerLink = page.locator('a[href="/register"]');
        await expect(registerLink).toBeVisible();
    });
});
