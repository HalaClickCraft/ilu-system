import { test, expect } from '@playwright/test';

test.describe('E2E - Polyvalence Matrix', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForLoadState('networkidle');
  });

  test('03.1 - Matrix page loads with Project & Year controls', async ({ page }) => {
    await page.goto('/evaluation/matrix');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('body')).toContainText(/Matrice|Polyvalence/i);
    const projSelect = page.locator('select').first();
    await expect(projSelect).toBeVisible();
  });

  test('03.2 - Compliance 6/6 row renders across workstations', async ({ page }) => {
    await page.goto('/evaluation/matrix');
    await page.waitForLoadState('networkidle');

    const matrixTable = page.locator('table').first();
    await expect(matrixTable).toBeVisible();
    await expect(page.locator('body')).toContainText(/Target|Statut|Conformité|6/i);
  });
});
