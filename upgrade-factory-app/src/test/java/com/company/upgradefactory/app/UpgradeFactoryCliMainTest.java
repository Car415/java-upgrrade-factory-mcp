package com.company.upgradefactory.app;

import com.company.upgradefactory.app.cli.AssessmentCliService;
import com.company.upgradefactory.app.cli.UpgradeCliService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpgradeFactoryCliMainTest {

    @Test
    void shouldDelegateScanCommandToAssessmentCliService() throws Exception {
        CapturingAssessmentCliService assessmentCliService = new CapturingAssessmentCliService();
        CapturingUpgradeCliService upgradeCliService = new CapturingUpgradeCliService();
        UpgradeFactoryApplication application = new UpgradeFactoryApplication(assessmentCliService, upgradeCliService);

        int exitCode = application.run(new String[]{"scan", "--repo", "sample"});

        assertThat(exitCode).isEqualTo(17);
        assertThat(assessmentCliService.invocationCount).isEqualTo(1);
        assertThat(upgradeCliService.invocationCount).isZero();
    }

    @Test
    void shouldDelegateUpgradeCommandToUpgradeCliService() throws Exception {
        CapturingAssessmentCliService assessmentCliService = new CapturingAssessmentCliService();
        CapturingUpgradeCliService upgradeCliService = new CapturingUpgradeCliService();
        UpgradeFactoryApplication application = new UpgradeFactoryApplication(assessmentCliService, upgradeCliService);

        int exitCode = application.run(new String[]{"upgrade", "--repo", "sample"});

        assertThat(exitCode).isEqualTo(23);
        assertThat(assessmentCliService.invocationCount).isZero();
        assertThat(upgradeCliService.invocationCount).isEqualTo(1);
    }

    private static final class CapturingAssessmentCliService extends AssessmentCliService {

        private int invocationCount;

        private CapturingAssessmentCliService() {
            super(null);
        }

        @Override
        public int execute(String[] args) {
            invocationCount++;
            return 17;
        }
    }

    private static final class CapturingUpgradeCliService extends UpgradeCliService {

        private int invocationCount;

        private CapturingUpgradeCliService() {
            super(null);
        }

        @Override
        public int execute(String[] args) {
            invocationCount++;
            return 23;
        }
    }
}
