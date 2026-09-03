import { test, expect } from '@playwright/test';

test.describe('E2E - Notification System (Bell, In-App Alerts, Clean Box & Delete)', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForLoadState('networkidle');
  });

  test('08.1 - Notification Bell icon is rendered in the top header', async ({ page }) => {
    const bellBtn = page.locator('button[title="Notifications"]').first();
    await expect(bellBtn).toBeVisible();
  });

  test('08.2 - Absence creation triggers real-time notification in bell dropdown', async ({ page, request }) => {
    const token = await page.evaluate(() => localStorage.getItem('token') || localStorage.getItem('access_token'));
    const authHeaders = { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };

    const opsRes = await request.get('/api/operators', { headers: authHeaders });
    const ops = await opsRes.json();
    const op = ops[ops.length - 1];
    expect(op).toBeDefined();

    const absenceRes = await request.post('/api/absence/mark-absent', {
      headers: authHeaders,
      data: {
        operatorId: op.id,
        startDate: '2026-09-03',
        expectedReturnDate: '2026-09-20'
      }
    });
    expect([200, 201]).toContain(absenceRes.status());

    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const bellBtn = page.locator('button[title="Notifications"]').first();
    await bellBtn.click();

    await expect(page.locator('body')).toContainText(/Notifications/i);
    await expect(page.locator('body')).toContainText(/absence|Début/i);
  });

  test('08.3 - Clear all notifications ("Vider") empties the notification box', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const bellBtn = page.locator('button[title="Notifications"]').first();
    await bellBtn.click();

    const clearBtn = page.locator('button:has-text("Vider")').first();
    if (await clearBtn.isVisible()) {
      await clearBtn.click();
      await page.waitForTimeout(500);
      await expect(page.locator('body')).toContainText(/Aucune notification pour le moment/i);
    }
  });

  test('08.4 - Test Email Notification Endpoint', async ({ page, request }) => {
    const token = await page.evaluate(() => localStorage.getItem('token') || localStorage.getItem('access_token'));
    const authHeaders = { 'Authorization': `Bearer ${token}` };

    const testMailRes = await request.post('/api/notifications/test-email?email=test.rh@opmobility.com', {
      headers: authHeaders
    });
    expect(testMailRes.status()).toBe(200);
    const mailData = await testMailRes.json();
    expect(mailData.recipient).toBe('test.rh@opmobility.com');
  });
});
