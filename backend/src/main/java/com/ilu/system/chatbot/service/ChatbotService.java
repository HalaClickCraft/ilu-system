package com.ilu.system.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilu.system.absence.entity.Absence;
import com.ilu.system.absence.repository.AbsenceRepository;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.Team;
import com.ilu.system.operator.entity.WorkstationFormation;
import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.operator.repository.TeamRepository;
import com.ilu.system.operator.repository.WorkstationFormationRepository;
import com.ilu.system.recyclage.entity.RecyclagePlanning;
import com.ilu.system.recyclage.repository.RecyclagePlanningRepository;
import com.ilu.system.structure.entity.Project;
import com.ilu.system.structure.entity.Workstation;
import com.ilu.system.structure.repository.ProjectRepository;
import com.ilu.system.structure.repository.WorkstationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatbotService {
    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    @Value("${app.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${app.ollama.model:llama3}")
    private String ollamaModel;

    private final OperatorRepository operatorRepo;
    private final WorkstationFormationRepository formationRepo;
    private final WorkstationRepository workstationRepo;
    private final TeamRepository teamRepo;
    private final AbsenceRepository absenceRepo;
    private final RecyclagePlanningRepository recyclageRepo;
    private final ProjectRepository projectRepo;

    private final RestTemplate restTemplate = buildRestTemplate();

    private static RestTemplate buildRestTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);   // 10 s to connect
        factory.setReadTimeout(240_000);     // 4 min to read (LLM can be slow)
        return new RestTemplate(factory);
    }
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cache of chat history: SessionId -> List of messages (excluding the system message)
    private final Map<String, List<Map<String, String>>> sessionHistories = new ConcurrentHashMap<>();

    public ChatbotService(OperatorRepository operatorRepo,
                          WorkstationFormationRepository formationRepo,
                          WorkstationRepository workstationRepo,
                          TeamRepository teamRepo,
                          AbsenceRepository absenceRepo,
                          RecyclagePlanningRepository recyclageRepo,
                          ProjectRepository projectRepo) {
        this.operatorRepo = operatorRepo;
        this.formationRepo = formationRepo;
        this.workstationRepo = workstationRepo;
        this.teamRepo = teamRepo;
        this.absenceRepo = absenceRepo;
        this.recyclageRepo = recyclageRepo;
        this.projectRepo = projectRepo;
    }

    public Map<String, Object> chat(String message, String sessionId) {
        String finalSessionId = (sessionId == null || sessionId.trim().isEmpty())
                ? UUID.randomUUID().toString()
                : sessionId;

        List<Map<String, String>> history = sessionHistories.computeIfAbsent(finalSessionId, k -> new ArrayList<>());

        // Limit history to last 10 messages (5 turns) to conserve token space
        if (history.size() > 10) {
            history = new ArrayList<>(history.subList(history.size() - 10, history.size()));
            sessionHistories.put(finalSessionId, history);
        }

        // Add user message to history
        Map<String, String> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", message);
        history.add(userMsg);

        // Gather latest statistics context
        String systemPrompt = buildSystemPrompt();

        // Build messages payload for Ollama
        List<Map<String, Object>> messagesPayload = new ArrayList<>();

        // Add System message
        Map<String, Object> systemMsg = new LinkedHashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messagesPayload.add(systemMsg);

        // Add conversation history
        for (Map<String, String> msg : history) {
            Map<String, Object> copy = new LinkedHashMap<>();
            copy.put("role", msg.get("role"));
            copy.put("content", msg.get("content"));
            messagesPayload.add(copy);
        }

        String chatbotResponse;
        try {
            chatbotResponse = callOllamaChatApi(messagesPayload);

            // Add Assistant message to history
            Map<String, String> assistantMsg = new LinkedHashMap<>();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", chatbotResponse);
            history.add(assistantMsg);

        } catch (Exception e) {
            log.error("Failed to connect to Ollama service: {}", e.getMessage());
            chatbotResponse = "Désolé, je ne parviens pas à contacter le service d'intelligence artificielle local (Ollama). " +
                    "Veuillez vous assurer que Ollama est en cours d'exécution sur votre machine (URL: " + ollamaUrl + ") " +
                    "et que le modèle '" + ollamaModel + "' a été téléchargé avec `ollama run " + ollamaModel + "`. " +
                    "Erreur technique: " + e.getMessage();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("response", chatbotResponse);
        result.put("sessionId", finalSessionId);
        return result;
    }

    private String callOllamaChatApi(List<Map<String, Object>> messages) throws Exception {
        String endpoint = ollamaUrl + "/api/chat";

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", ollamaModel);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(endpoint, entity, String.class);
        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
            JsonNode root = objectMapper.readTree(responseEntity.getBody());
            JsonNode messageNode = root.path("message");
            if (messageNode.has("content")) {
                return messageNode.get("content").asText();
            }
        }
        throw new RuntimeException("Ollama response has invalid structure or status code: " + responseEntity.getStatusCode());
    }

    private String buildSystemPrompt() {
        // Gather database statistics summaries
        List<Project> projects = projectRepo.findAll();
        List<Operator> operators = operatorRepo.findAll();
        List<Team> teams = teamRepo.findAll();
        List<Workstation> workstations = workstationRepo.findAll();
        List<WorkstationFormation> formations = formationRepo.findAll();
        List<Absence> activeAbsences = absenceRepo.findByStatus(Absence.AbsenceStatus.EN_COURS);
        List<RecyclagePlanning> recyclageList = recyclageRepo.findAll().stream()
                .filter(r -> "PLANIFIEE".equals(r.getStatus().name()) || "EN_COURS".equals(r.getStatus().name()))
                .collect(Collectors.toList());

        // Group counts
        long activeOpsCount = operators.stream().filter(Operator::getActive).count();
        long inactiveOpsCount = operators.stream().filter(o -> !o.getActive()).count();
        long certOpsCount = formations.stream().filter(f -> "VALIDEE".equals(f.getStatus())).count();
        long trainOpsCount = formations.stream().filter(f -> "EN_FORMATION".equals(f.getStatus())).count();

        StringBuilder sb = new StringBuilder();
        sb.append("Vous êtes l'Assistant IA du système ILU de OPmobility.\n");
        sb.append("Votre rôle est de répondre aux questions de l'utilisateur concernant les statistiques de l'usine, les opérateurs, les formations, les équipes, et les absences.\n");
        sb.append("Voici l'état actuel et à jour de la base de données (données réelles) :\n\n");

        sb.append("### STATISTIQUES GLOBALES\n");
        sb.append("- Nombre total d'opérateurs enregistrés: ").append(operators.size()).append("\n");
        sb.append("- Opérateurs actifs: ").append(activeOpsCount).append("\n");
        sb.append("- Opérateurs inactifs: ").append(inactiveOpsCount).append("\n");
        sb.append("- Formations terminées et validées (certifiées): ").append(certOpsCount).append("\n");
        sb.append("- Formations en cours (en formation): ").append(trainOpsCount).append("\n");
        sb.append("- Nombre de postes de travail: ").append(workstations.size()).append("\n");
        sb.append("- Nombre d'équipes: ").append(teams.size()).append("\n");
        sb.append("- Nombre de projets: ").append(projects.size()).append("\n");
        sb.append("- Absences en cours: ").append(activeAbsences.size()).append("\n");
        sb.append("- Tâches de recyclage/recyclage planifiées/en cours: ").append(recyclageList.size()).append("\n\n");

        sb.append("### PROJETS & EFFECTIFS\n");
        for (Project p : projects) {
            long projOps = operators.stream().filter(o -> o.getProject() != null && o.getProject().getId().equals(p.getId())).count();
            sb.append("- Projet '").append(p.getName()).append("' : ").append(projOps).append(" opérateurs.\n");
        }
        sb.append("\n");

        sb.append("### ÉQUIPES & ENCADREMENT (TEAMS & STAFF)\n");
        for (Team t : teams) {
            String pName = t.getProject() != null ? t.getProject().getName() : "Non attribué";
            long teamOps = operators.stream().filter(o -> o.getTeam() != null && o.getTeam().getId().equals(t.getId())).count();
            sb.append("- Équipe '").append(t.getName()).append("' (Projet: ").append(pName).append(") : ").append(teamOps).append(" opérateurs :\n");
            if (t.getTeamLeader() != null) sb.append("  * Chef d'Équipe: ").append(t.getTeamLeader()).append(" (Matricule: ").append(t.getTeamLeaderEmployeeId()).append(")\n");
            if (t.getAgentQualite() != null) sb.append("  * Agent Qualité: ").append(t.getAgentQualite()).append(" (Matricule: ").append(t.getAgentQualiteEmployeeId()).append(")\n");
            if (t.getQualityManager() != null) sb.append("  * Resp. Qualité: ").append(t.getQualityManager()).append(" (Matricule: ").append(t.getQualityManagerEmployeeId()).append(")\n");
            if (t.getProjectManager() != null) sb.append("  * Superviseur: ").append(t.getProjectManager()).append(" (Matricule: ").append(t.getProjectManagerEmployeeId()).append(")\n");
            if (t.getHseManager() != null) sb.append("  * Resp. HSE: ").append(t.getHseManager()).append(" (Matricule: ").append(t.getHseManagerEmployeeId()).append(")\n");
        }
        sb.append("\n");

        sb.append("### POSTES DE TRAVAIL & CERTIFICATIONS\n");
        for (Workstation w : workstations) {
            long certified = formations.stream().filter(f -> f.getWorkstation().getId().equals(w.getId()) && "VALIDEE".equals(f.getStatus())).count();
            long training = formations.stream().filter(f -> f.getWorkstation().getId().equals(w.getId()) && "EN_FORMATION".equals(f.getStatus())).count();
            String zoneName = (w.getZone() != null) ? w.getZone().getName() : "Inconnue";
            sb.append("- Poste '").append(w.getName()).append("' (Zone: ").append(zoneName).append(") : ")
                    .append(certified).append(" certifiés, ")
                    .append(training).append(" en formation.\n");
        }
        sb.append("\n");

        sb.append("### OPÉRATEURS ET LEUR STATUT\n");
        for (Operator o : operators) {
            String status = o.getActive() ? "Actif" : "Absent / Inactif";
            String projName = (o.getProject() != null) ? o.getProject().getName() : "Non assigné";
            String teamName = (o.getTeam() != null) ? o.getTeam().getName() : "Sans équipe";
            sb.append("- ").append(o.getFirstName()).append(" ").append(o.getLastName())
                    .append(" (Matricule: ").append(o.getEmployeeId()).append(") | Statut: ").append(status)
                    .append(" | Projet: ").append(projName)
                    .append(" | Équipe: ").append(teamName).append("\n");
        }
        sb.append("\n");

        sb.append("### ABSENCES EN COURS (DÉTAILS)\n");
        if (activeAbsences.isEmpty()) {
            sb.append("- Aucune absence en cours.\n");
        } else {
            for (Absence a : activeAbsences) {
                String name = (a.getOperator() != null) ? (a.getOperator().getFirstName() + " " + a.getOperator().getLastName()) : "Inconnu";
                sb.append("- ").append(name).append(" (Début: ").append(a.getStartDate()).append(")\n");
            }
        }
        sb.append("\n");

        sb.append("### TÂCHES DE RECYCLAGE EN ATTENTE\n");
        if (recyclageList.isEmpty()) {
            sb.append("- Aucun recyclage en attente.\n");
        } else {
            for (RecyclagePlanning r : recyclageList) {
                String opName = (r.getOperator() != null) ? (r.getOperator().getFirstName() + " " + r.getOperator().getLastName()) : "Inconnu";
                String wName = (r.getWorkstation() != null) ? r.getWorkstation().getName() : "Inconnu";
                sb.append("- Recyclage pour ").append(opName).append(" sur ").append(wName)
                        .append(" (Date prévue: ").append(r.getScheduledDate()).append(", Statut: ").append(r.getStatus()).append(")\n");
            }
        }
        sb.append("\n");

        sb.append("RÈGLES DE RÉPONSE :\n");
        sb.append("1. Répondez de manière professionnelle, précise et cordiale en Français.\n");
        sb.append("2. Utilisez STRICTEMENT les données fournies ci-dessus pour formuler vos réponses. Ne faites pas d'hypothèses sur des données non fournies.\n");
        sb.append("3. Si vous ne trouvez pas une information spécifique dans les données fournies, dites-le poliment (par exemple : 'Je n'ai pas d'information concernant...').\n");
        sb.append("4. Utilisez le formatage Markdown (tableaux, listes à puces, gras) pour rendre les statistiques claires et faciles à lire.\n");

        return sb.toString();
    }
}
