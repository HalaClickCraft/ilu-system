package com.ilu.system.evaluation.entity;

import com.ilu.system.structure.entity.Workstation;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "evaluation_templates")
public class EvaluationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType type;

    @ManyToOne
    @JoinColumn(name = "workstation_id")
    private Workstation workstation;

    /**
     * New templates can serve several workstations. workstation is retained for
     * compatibility with templates created before this relationship existed.
     */
    @ManyToMany
    @JoinTable(name = "evaluation_template_workstations",
            joinColumns = @JoinColumn(name = "template_id"),
            inverseJoinColumns = @JoinColumn(name = "workstation_id"))
    private Set<Workstation> workstations = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateStatus status = TemplateStatus.DRAFT;

    @Column(name = "target_niveau")
    private String targetNiveau;

    @Column(name = "created_by_id")
    private Long createdById;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<EvaluationSection> sections;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum TemplateType {
        GENERIC_HSE, GENERIC_QUALITY, GENERIC_COMMON, POSTE_PRODUCTION, ANIMATION
    }

    public enum TemplateStatus {
        DRAFT, VALIDATED, ARCHIVED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TemplateType getType() { return type; }
    public void setType(TemplateType type) { this.type = type; }
    public Workstation getWorkstation() { return workstation; }
    public void setWorkstation(Workstation workstation) { this.workstation = workstation; }
    public Set<Workstation> getWorkstations() { return workstations; }
    public void setWorkstations(Set<Workstation> workstations) { this.workstations = workstations; }
    public TemplateStatus getStatus() { return status; }
    public void setStatus(TemplateStatus status) { this.status = status; }
    public String getTargetNiveau() { return targetNiveau; }
    public void setTargetNiveau(String targetNiveau) { this.targetNiveau = targetNiveau; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<EvaluationSection> getSections() { return sections; }
    public void setSections(List<EvaluationSection> sections) { this.sections = sections; }
}