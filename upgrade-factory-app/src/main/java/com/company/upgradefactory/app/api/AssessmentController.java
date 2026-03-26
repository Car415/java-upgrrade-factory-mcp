package com.company.upgradefactory.app.api;

import com.company.upgradefactory.app.dto.AssessmentRequest;
import com.company.upgradefactory.app.dto.AssessmentResponse;
import com.company.upgradefactory.app.dto.MigrationPlanRequest;
import com.company.upgradefactory.app.dto.MigrationPlanResponse;
import com.company.upgradefactory.app.service.AssessmentApplicationService;
import com.company.upgradefactory.app.service.MigrationPlanApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
public class AssessmentController {

    private final AssessmentApplicationService assessmentApplicationService;
    private final MigrationPlanApplicationService migrationPlanApplicationService;

    public AssessmentController(
            AssessmentApplicationService assessmentApplicationService,
            MigrationPlanApplicationService migrationPlanApplicationService
    ) {
        this.assessmentApplicationService = assessmentApplicationService;
        this.migrationPlanApplicationService = migrationPlanApplicationService;
    }

    @PostMapping("/assess")
    public ResponseEntity<AssessmentResponse> assess(@Valid @RequestBody AssessmentRequest request) throws IOException {
        return ResponseEntity.ok(assessmentApplicationService.assess(request));
    }

    @PostMapping("/plan")
    public ResponseEntity<MigrationPlanResponse> plan(@Valid @RequestBody MigrationPlanRequest request) {
        return ResponseEntity.ok(migrationPlanApplicationService.generatePlan(request));
    }
}
