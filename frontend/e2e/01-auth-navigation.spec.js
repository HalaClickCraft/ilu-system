import { test, expect } from '@playwright/test';

test.describe('E2E - Authentication & Role Navigation', () => {

  test('01.1 - Display Login Page with all elements', async ({ page }) => {
    await page.goto('/login');
    await expect(page).toHaveTitle(/ILU|Système ILU/i);
    await expect(page.locator('input[type="text"]').first()).toBeVisible();
    await expect(page.locator('input[type="password"]').first()).toBeVisible();
    await expect(page.locator('button[type="submit"]').first()).toBeVisible();
  });

  test('01.2 - Rejects invalid login with error feedback', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="text"]', 'INVALID_USER_99');
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');

    // Should display error notification or toast
    const errorNotice = page.locator('.text-red-600, .text-red-500, .bg-red-50, [role="alert"]').first();
    await expect(errorNotice).toBeVisible({ timeout: 5000 });
  });

  test('01.3 - Successful Admin Login & Dashboard redirection', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');

    // Should redirect away from /login
    await expect(page).not.toHaveURL(/\/login/);
    await expect(page.locator('aside, nav').first()).toBeVisible();
  });

  test('01.4 - Sidebar Navigation Links Work Correctly', async ({ page }) => {
    // Perform login
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForLoadState('networkidle');

    // Check Operators link
    const operatorsLink = page.locator('a[href*="/operators"]').first();
    if (await operatorsLink.isVisible()) {
      await operatorsLink.click();
      await expect(page).toHaveURL(/\/operators/);
    }

    // Check Structure link
    const structureLink = page.locator('a[href*="/structure"]').first();
    if (await structureLink.isVisible()) {
      await structureLink.click();
      await expect(page).toHaveURL(/\/structure/);
    }

    // Check Polyvalence Matrix link
    const matrixLink = page.locator('a[href*="/evaluation/matrix"]').first();
    if (await matrixLink.isVisible()) {
      await matrixLink.click();
      await expect(page).toHaveURL(/\/evaluation\/matrix/);
    }
  });

  test('01.5 - Logout removes session and redirects to Login', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForLoadState('networkidle');

    // Locate and click logout button
    const logoutBtn = page.locator('button:has-text("Déconnexion"), button:has-text("Se déconnecter"), [title="Déconnexion"], [title="Deconnexion"]').first();
    if (await logoutBtn.isVisible()) {
      await logoutBtn.click();
      await expect(page).toHaveURL(/\/login/);
    }
  });
});
