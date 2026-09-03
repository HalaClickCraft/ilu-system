import { test, expect } from '@playwright/test';

test.describe('E2E - Absences & Teams Management', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForLoadState('networkidle');
  });

  test('06.1 - Gestion des Absences page loads roster and filters', async ({ page }) => {
    await page.goto('/absences');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('body')).toContainText(/Absence|En cours|Historique/i);
    await expect(page.locator('button, select, table, input').first()).toBeVisible();
  });

  test('06.2 - Teams Management page loads team assignments', async ({ page }) => {
    await page.goto('/teams');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('body')).toContainText(/Équipe|Equipe|Chef/i);
    await expect(page.locator('button, select, table, .grid').first()).toBeVisible();
  });
});
