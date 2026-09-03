import { test, expect } from '@playwright/test';

test.describe('E2E - Structure, Recyclage & Double Failures', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForLoadState('networkidle');
  });

  test('04.1 - Plant Structure page loads tree view', async ({ page }) => {
    await page.goto('/structure');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('h1, h2').first()).toContainText(/Structure|Usine/i);
    await expect(page.locator('body')).toContainText(/Projet|Zone|Poste/i);
  });

  test('04.2 - Double Failures (Double Échec) page loads with alert table', async ({ page }) => {
    await page.goto('/evaluation/double-failures');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('h1, h2').first()).toContainText(/Double Échec|Signalements RH/i);
    await expect(page.locator('input[type="text"], table, .bg-white').first()).toBeVisible();
  });

  test('04.3 - Recyclage Planning calendar loads active semesters', async ({ page }) => {
    await page.goto('/recyclage');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('h1, h2').first()).toContainText(/Recyclage|Planification/i);
    await expect(page.locator('select, button, table').first()).toBeVisible();
  });
});
