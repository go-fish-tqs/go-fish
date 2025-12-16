import { test, expect } from '@playwright/test';
import { login, TEST_USERS } from './auth.helpers';

/**
 * Admin Management Feature Tests
 * Based on: admin.feature
 */
test.describe('Admin Management', () => {
    test.beforeEach(async ({ page }) => {
        await login(page, TEST_USERS.admin.email, TEST_USERS.admin.password);
    });

    test('should access admin dashboard', async ({ page }) => {
        await page.goto('/admin');
        await page.waitForLoadState('networkidle');

        // Should see dashboard content
        await expect(page.getByText('Active Bookings')).toBeVisible({ timeout: 10000 });
        await expect(page.getByText('Total Revenue')).toBeVisible();
    });

    test('should access user management', async ({ page }) => {
        await page.goto('/admin/users');
        await page.waitForLoadState('networkidle');

        // Should see user table
        await expect(page.locator('table')).toBeVisible({ timeout: 10000 });
    });

    test('should access item management', async ({ page }) => {
        await page.goto('/admin/items');
        await page.waitForLoadState('networkidle');

        // Should see items table
        await expect(page.locator('table')).toBeVisible({ timeout: 10000 });
    });

    test('should access audit log', async ({ page }) => {
        await page.goto('/admin/audit');
        await page.waitForLoadState('networkidle');

        // Should see filter dropdown
        await expect(page.locator('select')).toBeVisible({ timeout: 10000 });
    });

    test('should navigate between admin pages', async ({ page }) => {
        await page.goto('/admin');

        // Navigate using sidebar - use role-based selectors for sidebar links
        await page.getByRole('link', { name: 'Users' }).click();
        await page.waitForURL('/admin/users');

        await page.getByRole('link', { name: 'Items' }).click();
        await page.waitForURL('/admin/items');

        await page.getByRole('link', { name: 'Audit Log' }).click();
        await page.waitForURL('/admin/audit');
    });
});
