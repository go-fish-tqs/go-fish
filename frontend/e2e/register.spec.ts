import { test, expect } from '@playwright/test';
import { TEST_USERS } from './auth.helpers';

/**
 * Registration Feature Tests
 * Based on: register.feature
 */
test.describe('User Registration', () => {
    test('should show registration page with required elements', async ({ page }) => {
        await page.goto('/register');

        await expect(page.locator('input[name="name"]')).toBeVisible();
        await expect(page.locator('input[name="email"]')).toBeVisible();
        await expect(page.locator('input[name="password"]')).toBeVisible();
        await expect(page.locator('input[name="confirmPassword"]')).toBeVisible();
        await expect(page.locator('input[name="location"]')).toBeVisible();
        await expect(page.locator('button[type="submit"]')).toBeVisible();
    });

    test('should register successfully with valid data', async ({ page }) => {
        const uniqueEmail = `test-${Date.now()}@playwright.com`;

        await page.goto('/register');

        await page.locator('input[name="name"]').fill('Playwright Test User');
        await page.locator('input[name="email"]').fill(uniqueEmail);
        await page.locator('input[name="password"]').fill('Test123!');
        await page.locator('input[name="confirmPassword"]').fill('Test123!');
        await page.locator('input[name="location"]').fill('Porto');

        // Handle alert on success
        page.on('dialog', dialog => dialog.accept());

        await page.locator('button[type="submit"]').click();

        // Should redirect to login after successful registration
        await page.waitForURL('/login', { timeout: 10000 });
    });

    test('should show error for existing email', async ({ page }) => {
        await page.goto('/register');

        await page.locator('input[name="name"]').fill('Duplicate User');
        await page.locator('input[name="email"]').fill(TEST_USERS.admin.email);
        await page.locator('input[name="password"]').fill('Test123!');
        await page.locator('input[name="confirmPassword"]').fill('Test123!');
        await page.locator('input[name="location"]').fill('Lisbon');

        await page.locator('button[type="submit"]').click();

        // Should remain on register page
        await expect(page).toHaveURL('/register');

        // Should show error
        await expect(page.locator('[class*="red"]').first()).toBeVisible({ timeout: 5000 });
    });

    test('should validate password match', async ({ page }) => {
        await page.goto('/register');

        await page.locator('input[name="name"]').fill('Test User');
        await page.locator('input[name="email"]').fill('mismatch@test.com');
        await page.locator('input[name="password"]').fill('Password1');
        await page.locator('input[name="confirmPassword"]').fill('Password2');
        await page.locator('input[name="location"]').fill('Lisbon');

        await page.locator('button[type="submit"]').click();

        // Should show password mismatch error
        await expect(page.getByText('Passwords do not match')).toBeVisible();
    });
});
