package com.ilu.system.structure;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PosteController {

    private final StructureService structureService;

    public PosteController(StructureService structureService) {
        this.structureService = structureService;
    }

    @GetMapping("/postes")
    public List<PosteTravailDto> getAllPostes() {
        return structureService.getAllPostes();
    }
}
