import { test, expect } from '@playwright/test';
import { login, TEST_USERS } from './auth.helpers';

/**
 * Item Management Feature Tests
 * Based on: item.feature
 */
test.describe('Item Management', () => {
    test.beforeEach(async ({ page }) => {
        await login(page, TEST_USERS.admin.email, TEST_USERS.admin.password);
    });

    test('should display items page', async ({ page }) => {
        await page.goto('/items');
        await page.waitForLoadState('networkidle');

        // Should see available gear heading
        await expect(page.getByRole('heading', { name: /gear|items/i })).toBeVisible();
    });

    test('should have search/filter functionality', async ({ page }) => {
        await page.goto('/items');
        await page.waitForLoadState('networkidle');

        // Should have search input
        await expect(page.locator('input[type="text"]').first()).toBeVisible();
    });

    test('should navigate to add item page', async ({ page }) => {
        await page.goto('/items/add');

        // Should see form elements
        await expect(page.locator('input[name="name"], #name').first()).toBeVisible();
        await expect(page.locator('button[type="submit"]')).toBeVisible();
    });

    test('should display my items page', async ({ page }) => {
        await page.goto('/my-items');
        await page.waitForLoadState('networkidle');

        // Should see my items heading or content
        await expect(page.locator('main')).toBeVisible();
    });
});
