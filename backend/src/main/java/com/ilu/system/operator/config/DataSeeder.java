package com.ilu.system.operator.config;

import com.ilu.system.operator.service.OnboardingService;
import com.ilu.system.auth.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserService userService;
    private final OnboardingService onboardingService;

    public DataSeeder(UserService userService, OnboardingService onboardingService) {
        this.userService = userService;
        this.onboardingService = onboardingService;
    }

    @Override
    public void run(String... args) {
        userService.seedRoles();
        onboardingService.seedModules();
    }
}