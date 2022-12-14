package templates

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import util.BasePiperTest
import util.JenkinsReadYamlRule
import util.JenkinsStepRule
import util.PipelineWhenException
import util.Rules

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class abapEnvironmentPipelineStagePublishTest extends BasePiperTest {
    private JenkinsStepRule jsr = new JenkinsStepRule(this)

    @Rule
    public RuleChain rules = Rules
        .getCommonRules(this)
        .around(new JenkinsReadYamlRule(this))
        .around(jsr)

    private stepsCalled = []

    @Before
    void init()  {
        binding.variables.env.STAGE_NAME = 'Publish'

        helper.registerAllowedMethod('piperStageWrapper', [Map.class, Closure.class], {m, body ->
            assertThat(m.stageName, is('Publish'))
            return body()
        })
        helper.registerAllowedMethod('input', [Map], {m ->
            stepsCalled.add('input')
            return null
        })
        helper.registerAllowedMethod('abapAddonAssemblyKitPublishTargetVector', [Map.class], {m -> stepsCalled.add('abapAddonAssemblyKitPublishTargetVector')})
    }

    @Test
    void testPublishExecuted() {

        nullScript.commonPipelineEnvironment.configuration.runStage = []

        jsr.step.abapEnvironmentPipelineStagePublish(script: nullScript)

        assertThat(stepsCalled, hasItem('abapAddonAssemblyKitPublishTargetVector'))
    }

    @Test
    void testPublishSkipped4testBuild() {
        nullScript.commonPipelineEnvironment.configuration.runStage = []
        jsr.step.abapEnvironmentPipelineStagePublish(script: nullScript, testBuild: true)
        assertThat(stepsCalled, not(hasItem('abapAddonAssemblyKitPublishTargetVector')))
    }
}
