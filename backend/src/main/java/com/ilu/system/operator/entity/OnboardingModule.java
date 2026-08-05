package com.ilu.system.operator.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "onboarding_modules")
public class OnboardingModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(length = 500)
    private String description;

    public OnboardingModule() {}

    public OnboardingModule(String name, String department, Integer displayOrder) {
        this.name = name;
        this.department = department;
        this.displayOrder = displayOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}