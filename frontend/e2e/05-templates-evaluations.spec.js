import { test, expect } from '@playwright/test';

test.describe('E2E - Evaluation Templates & Questionnaires', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForLoadState('networkidle');
  });

  test('05.1 - Evaluation Templates page renders templates and action buttons', async ({ page }) => {
    await page.goto('/evaluation/templates');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('body')).toContainText(/Questionnaire|Template|Evaluation/i);
    await expect(page.locator('input[placeholder*="Rechercher"], select, button').first()).toBeVisible();
  });

  test('05.2 - Question Validation page renders approval workflow', async ({ page }) => {
    await page.goto('/evaluation/questions');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('body')).toContainText(/Validation|Question|En attente|Statut/i);
  });
});
