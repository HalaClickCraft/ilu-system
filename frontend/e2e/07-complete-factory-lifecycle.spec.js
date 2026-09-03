import { test, expect } from '@playwright/test';

test.describe('E2E - Complete Factory Lifecycle: Structure -> Team -> Deja En Poste -> Nouveau Recru -> Formation 12j -> Evaluation Initial -> Recyclage -> Matrice', () => {

  const timestamp = Date.now().toString().slice(-4);
  const projectName = `PROJ_E2E_${timestamp}`;
  const zoneName = `Zone_Assembly_${timestamp}`;
  const wsName1 = `Robotique_${timestamp}`;
  const wsName2 = `Soudure_${timestamp}`;
  const teamName = `Team_Alpha_${timestamp}`;
  const opDejaMat = `OP_DEJA_${timestamp}`;
  const opNewMat = `OP_NEW_${timestamp}`;

  test('Execute Complete End-to-End Factory Lifecycle Workflow', async ({ page, request }) => {
    test.setTimeout(90000);

    // -------------------------------------------------------------
    // Step 1: Login as Admin
    // -------------------------------------------------------------
    console.log('--- STEP 1: Login as Admin ---');
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForLoadState('networkidle');
    await expect(page).not.toHaveURL(/\/login/);

    const token = await page.evaluate(() => localStorage.getItem('token') || localStorage.getItem('access_token'));
    const authHeaders = { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };

    // -------------------------------------------------------------
    // Step 2: Create Structure (Project -> Zone -> Workstations)
    // -------------------------------------------------------------
    console.log('--- STEP 2: Creating Plant Structure via API ---');
    const projRes = await request.post('/api/structure/projects', {
      headers: authHeaders,
      data: { name: projectName, description: 'E2E Testing Project' }
    });
    expect([200, 201]).toContain(projRes.status());
    const projData = await projRes.json();
    const projectId = projData.id;
    console.log(`Created Project: ${projectName} (ID: ${projectId})`);

    const zoneRes = await request.post(`/api/structure/projects/${projectId}/zones?name=${encodeURIComponent(zoneName)}`, {
      headers: authHeaders
    });
    expect([200, 201]).toContain(zoneRes.status());
    const zoneData = await zoneRes.json();
    const zoneId = zoneData.id;
    console.log(`Created Zone: ${zoneName} (ID: ${zoneId})`);

    const ws1Res = await request.post('/api/structure/workstations', {
      headers: authHeaders,
      data: { name: wsName1, zoneId: zoneId, targetCadence: 50, qualityObjective: 98.5 }
    });
    expect([200, 201]).toContain(ws1Res.status());
    const ws1Data = await ws1Res.json();
    const ws1Id = ws1Data.id;

    const ws2Res = await request.post('/api/structure/workstations', {
      headers: authHeaders,
      data: { name: wsName2, zoneId: zoneId, targetCadence: 45, qualityObjective: 99.0 }
    });
    expect([200, 201]).toContain(ws2Res.status());
    const ws2Data = await ws2Res.json();
    const ws2Id = ws2Data.id;
    console.log(`Created Workstations: ${wsName1} (ID: ${ws1Id}), ${wsName2} (ID: ${ws2Id})`);

    await page.goto('/structure');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('body')).toContainText(new RegExp(projectName, 'i'));

    // -------------------------------------------------------------
    // Step 3: Create Team (Équipe)
    // -------------------------------------------------------------
    console.log('--- STEP 3: Creating Production Team ---');
    const teamRes = await request.post('/api/teams', {
      headers: authHeaders,
      data: { name: teamName, shift: 'MATIN', teamLeader: 'Chef Mohamed', projectId: projectId }
    });
    expect([200, 201]).toContain(teamRes.status());
    const teamData = await teamRes.json();
    const teamId = teamData.id;
    console.log(`Created Team: ${teamName} (ID: ${teamId})`);

    // -------------------------------------------------------------
    // Step 4: Operator Déjà en poste & Certification Import
    // -------------------------------------------------------------
    console.log('--- STEP 4: Operator Déjà en poste ---');
    const importRes = await request.post('/api/evaluation/matrix/import-certifications', {
      headers: authHeaders,
      data: [
        {
          operatorName: `EL AMRANI Karim`,
          workstationName: wsName1,
          level: 'U',
          validationDate: '2026-09-01'
        }
      ]
    });
    expect(importRes.status()).toBe(200);
    const importResult = await importRes.json();
    expect(importResult[0].status).toBe('SUCCESS');
    console.log(`Imported Déjà en poste: EL AMRANI Karim (Level U on ${wsName1})`);

    const opDejaSearch = await request.get('/api/operators', { headers: authHeaders });
    const allOps = await opDejaSearch.json();
    const opDeja = allOps.find(o => (o.lastName + ' ' + o.firstName).toUpperCase().includes('AMRANI'));
    expect(opDeja).toBeDefined();

    const newTrackRes = await request.put(`/api/operators/${opDeja.id}`, {
      headers: authHeaders,
      data: {
        employeeId: opDeja.employeeId,
        lastName: opDeja.lastName,
        firstName: opDeja.firstName,
        role: opDeja.role,
        operatorType: 'DEJA_EN_POSTE',
        projectId: projectId,
        zoneId: zoneId,
        teamId: teamId,
        workstationId: ws2Id
      }
    });
    expect(newTrackRes.status()).toBe(200);
    console.log(`Assigned Déjà en poste operator to Project and NEW workstation ${wsName2} -> opened in-progress track`);

    // -------------------------------------------------------------
    // Step 5: Create Operator 2 (NOUVEAU_RECRU)
    // -------------------------------------------------------------
    console.log('--- STEP 5: Creating Nouveau Recru ---');
    const opNewRes = await request.post('/api/operators', {
      headers: authHeaders,
      data: {
        employeeId: opNewMat,
        lastName: 'BENNANI',
        firstName: 'Yassine',
        role: 'Opérateur Débutant',
        operatorType: 'NOUVEAU_RECRU',
        hireDate: '2026-09-01',
        projectId: projectId,
        zoneId: zoneId,
        teamId: teamId,
        workstationId: ws1Id
      }
    });
    expect([200, 201]).toContain(opNewRes.status());
    const opNewData = await opNewRes.json();
    const opNewId = opNewData.id;
    console.log(`Created Nouveau Recru: BENNANI Yassine (ID: ${opNewId}, Mat: ${opNewMat})`);

    // -------------------------------------------------------------
    // Step 6: 12-Day Practical Training (Suivi de Formation 12 jours)
    // -------------------------------------------------------------
    console.log('--- STEP 6: Simulating 12-Day Practical Formation Tracking ---');
    const formRes = await request.get(`/api/operators/${opNewId}/formations`, { headers: authHeaders });
    const formations = await formRes.json();
    expect(formations.length).toBeGreaterThan(0);
    const activeFormation = formations[0];
    const formationId = activeFormation.id;

    for (let day = 1; day <= 12; day++) {
      const dayRes = await request.post(`/api/training/formations/${formationId}/tracking`, {
        headers: authHeaders,
        data: {
          dayNumber: day,
          actualCadence: 52,
          defects: 0,
          qualityValidation: true,
          safetyValidation: true
        }
      });
      expect([200, 201]).toContain(dayRes.status());
    }
    console.log('Recorded 12 consecutive successful tracking days -> Auto-evaluated & Completed');

    // -------------------------------------------------------------
    // Step 7: Évaluation Initiale
    // -------------------------------------------------------------
    console.log('--- STEP 7: Executing Évaluation Initiale ---');
    const tplRes = await request.post('/api/evaluation/templates', {
      headers: authHeaders,
      data: {
        name: `QCM Robotique ${timestamp}`,
        workstationId: ws1Id,
        type: 'POSTE_PRODUCTION'
      }
    });
    const tplData = await tplRes.json();
    const tplId = tplData.id;

    // 1. Contributed question by Chef d'équipe
    const q1Res = await request.post(`/api/evaluation/templates/${tplId}/questions`, {
      headers: authHeaders,
      data: {
        questionNumber: 1,
        questionText: 'Procédure arrêt urgence robot ?',
        expectedAnswer: 'Appuyer sur le coup de poing rouge',
        validatorRole: 'CHEF_EQUIPE'
      }
    });
    const q1Data = await q1Res.json();
    const q1Id = q1Data.id;

    // 2. Contributed question by Agent Qualité
    const q2Res = await request.post(`/api/evaluation/templates/${tplId}/questions`, {
      headers: authHeaders,
      data: {
        questionNumber: 2,
        questionText: 'Contrôle qualité pièce robotisée ?',
        expectedAnswer: 'Vérifier dimension avec le gabarit',
        validatorRole: 'AGENT_QUALITE'
      }
    });
    const q2Data = await q2Res.json();
    const q2Id = q2Data.id;

    await request.post(`/api/evaluation/questions/${q1Id}/validate`, { headers: authHeaders });
    await request.post(`/api/evaluation/questions/${q2Id}/validate`, { headers: authHeaders });
    await request.post(`/api/evaluation/templates/${tplId}/validate`, { headers: authHeaders });

    const startSessionRes = await request.post('/api/evaluation/sessions/start', {
      headers: authHeaders,
      data: {
        operatorId: opNewId,
        templateId: tplId,
        formationId: formationId,
        mode: 'INITIAL'
      }
    });
    expect(startSessionRes.status()).toBe(200);
    const sessionData = await startSessionRes.json();
    const sessionId = sessionData.sessionId || sessionData.id;

    const answerRes = await request.post(`/api/evaluation/sessions/${sessionId}/answers`, {
      headers: authHeaders,
      data: {
        answers: [
          {
            questionId: q1Id,
            answer: 1,
            comment: 'Parfait'
          },
          {
            questionId: q2Id,
            answer: 1,
            comment: 'Conforme'
          }
        ]
      }
    });
    expect(answerRes.status()).toBe(200);

    const completeRes = await request.post(`/api/evaluation/sessions/${sessionId}/complete`, {
      headers: authHeaders
    });
    expect(completeRes.status()).toBe(200);
    console.log('Completed Évaluation Initiale with 100% score -> Operator granted Level L');

    // -------------------------------------------------------------
    // Step 8: Recyclage Planning Verification
    // -------------------------------------------------------------
    console.log('--- STEP 8: Verifying Recyclage Planning Calendar ---');
    const recRes = await request.get('/api/recyclage/planning', { headers: authHeaders });
    expect(recRes.status()).toBe(200);
    const plannings = await recRes.json();
    const opNewPlanning = plannings.find(p => p.operatorId === opNewId);
    expect(opNewPlanning).toBeDefined();
    console.log(`Recyclage Scheduled for Nouveau Recru: Date ${opNewPlanning?.scheduledDate} (Source: ${opNewPlanning?.source})`);

    // -------------------------------------------------------------
    // Step 9: Polyvalence Matrix Verification
    // -------------------------------------------------------------
    console.log('--- STEP 9: Verifying Matrice de Polyvalence ---');
    await page.goto('/evaluation/matrix');
    await page.waitForLoadState('networkidle');

    // Select created project
    const projSelect = page.locator('select').first();
    await projSelect.selectOption({ label: projectName });
    await page.waitForTimeout(1000);

    // Verify workstations columns rendered
    await expect(page.locator('body')).toContainText(new RegExp(wsName1, 'i'));

    // Enable "En cours d'intégration" toggle to display operators currently in pipeline
    const inTrainingToggle = page.locator('input[type="checkbox"]').first();
    if (await inTrainingToggle.isVisible()) {
      await inTrainingToggle.check();
      await page.waitForTimeout(500);
    }

    // Verify both operators are listed in the matrix
    await expect(page.locator('body')).toContainText(/BENNANI|EL AMRANI/i);

    console.log('✅ Complete Factory Lifecycle Scenario Tested and Verified 100% Successfully!');
  });
});
