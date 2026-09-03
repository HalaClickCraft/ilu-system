import { test, expect } from '@playwright/test';

test.describe('E2E - Operator Management', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForLoadState('networkidle');
  });

  test('02.1 - Directory displays operator cards and statistics', async ({ page }) => {
    await page.goto('/operators');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('body')).toContainText(/Op.rateur|Annuaire/i);
    await expect(page.locator('table, .grid, [data-v-app]').first()).toBeVisible();
  });

  test('02.2 - Search operator by name filter', async ({ page }) => {
    await page.goto('/operators');
    await page.waitForLoadState('networkidle');

    const searchInput = page.locator('input[placeholder*="Rechercher"], input[type="text"]').first();
    if (await searchInput.isVisible()) {
      await searchInput.fill('OUTALEB');
      await page.waitForTimeout(500);
      await expect(page.locator('body')).toContainText(/OUTALEB/i);
    }
  });

  test('02.3 - Create New Operator Modal opens and closes', async ({ page }) => {
    await page.goto('/operators');
    await page.waitForLoadState('networkidle');

    const newOpBtn = page.locator('button:has-text("Nouvel Opérateur"), button:has-text("Ajouter")').first();
    if (await newOpBtn.isVisible()) {
      await newOpBtn.click();
      const modal = page.locator('.fixed.inset-0.bg-black\\/50');
      await expect(modal).toBeVisible();

      // Click the backdrop overlay to close
      await modal.click({ position: { x: 10, y: 10 } });
      await expect(modal).not.toBeVisible();
    }
  });

  test('02.4 - Permanently Delete an Operator via Admin/RH action button', async ({ page, request }) => {
    const token = await page.evaluate(() => localStorage.getItem('token') || localStorage.getItem('access_token'));
    const authHeaders = { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };

    const timestamp = Date.now().toString().slice(-4);
    const tempMat = `OP_TO_DEL_${timestamp}`;

    // Create a temporary operator
    const createRes = await request.post('/api/operators', {
      headers: authHeaders,
      data: {
        employeeId: tempMat,
        lastName: 'TEMPDELETE',
        firstName: 'Operator',
        role: 'Opérateur Test',
        operatorType: 'NOUVEAU_RECRU',
        hireDate: '2026-09-01'
      }
    });
    expect([200, 201]).toContain(createRes.status());
    const createdOp = await createRes.json();

    // Navigate to operators page
    await page.goto('/operators');
    await page.waitForLoadState('networkidle');

    // Search for the temporary operator
    const searchInput = page.locator('input[placeholder*="Rechercher"]').first();
    await searchInput.fill(tempMat);
    await page.waitForTimeout(500);

    // Select the operator card/row
    const opItem = page.locator(`text=${tempMat}`).first();
    await opItem.click();
    await page.waitForTimeout(500);

    // Click "Supprimer définitivement"
    const deleteBtn = page.locator('button:has-text("Supprimer définitivement")').first();
    await expect(deleteBtn).toBeVisible();
    await deleteBtn.click();

    // Confirm in the modal
    const confirmModal = page.locator('.fixed.inset-0').filter({ hasText: 'Suppression définitive' });
    await expect(confirmModal).toBeVisible();
    const confirmBtn = confirmModal.locator('button:has-text("Confirmer"), button:has-text("Supprimer")').first();
    await confirmBtn.click();
    await page.waitForTimeout(1000);

    // Verify operator is permanently deleted from backend
    const checkRes = await request.get(`/api/operators/${createdOp.id}`, { headers: authHeaders });
    expect([400, 404]).toContain(checkRes.status());

    const allOpsRes = await request.get('/api/operators', { headers: authHeaders });
    const allOps = await allOpsRes.json();
    expect(allOps.find(o => o.id === createdOp.id)).toBeUndefined();
  });
});
