package com.company.upgradefactory.app;

import com.company.upgradefactory.app.service.AssessmentApplicationService;
import com.company.upgradefactory.app.service.MigrationPlanApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class AssessmentControllerPlanEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssessmentApplicationService assessmentApplicationService;

    @MockBean
    private MigrationPlanApplicationService migrationPlanApplicationService;

    @Test
    void shouldRejectInvalidPlanRequest() throws Exception {
        mockMvc.perform(post("/api/v1/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "repoName": "",
                                  "readinessScore": 120,
                                  "migrationTier": "",
                                  "rolloutStrategy": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
