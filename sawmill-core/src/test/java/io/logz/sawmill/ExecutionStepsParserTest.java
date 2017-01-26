package io.logz.sawmill;

import io.logz.sawmill.conditions.AndCondition;
import io.logz.sawmill.conditions.TestCondition;
import io.logz.sawmill.parser.ConditionDefinition;
import io.logz.sawmill.parser.ConditionalExecutionStepDefinition;
import io.logz.sawmill.parser.ExecutionStepDefinition;
import io.logz.sawmill.parser.ExecutionStepsParser;
import io.logz.sawmill.parser.ProcessorDefinition;
import io.logz.sawmill.parser.ProcessorExecutionStepDefinition;
import io.logz.sawmill.processors.AddTagProcessor;
import io.logz.sawmill.processors.TestProcessor;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.logz.sawmill.utilities.JsonUtils.createList;
import static io.logz.sawmill.utilities.JsonUtils.createMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ExecutionStepsParserTest {

    private ExecutionStepsParser executionStepsParser;

    @Before
    public void init() {
        ProcessorFactoryRegistry processorFactoryRegistry = new ProcessorFactoryRegistry();
        processorFactoryRegistry.register("test", new TestProcessor.Factory());
        ProcessorFactoriesLoader.getInstance().loadAnnotatedProcessors(processorFactoryRegistry);

        ConditionFactoryRegistry conditionFactoryRegistry = new ConditionFactoryRegistry();
        conditionFactoryRegistry.register("testCondition", new TestCondition.Factory());
        ConditionalFactoriesLoader.getInstance().loadAnnotatedProcessors(conditionFactoryRegistry);

        executionStepsParser = new ExecutionStepsParser(processorFactoryRegistry, conditionFactoryRegistry);
    }

    @Test
    public void testParseProcessorExecutionStep() {
        String name = RandomStringUtils.randomAlphanumeric(10);
        List<ExecutionStepDefinition> executionStepDefinitionList = Collections.singletonList(
                createAddTagStepDefinition(name)
        );

        List<ExecutionStep> executionSteps = executionStepsParser.parse(executionStepDefinitionList);
        assertThat(executionSteps.size()).isEqualTo(1);

        ProcessorExecutionStep processorExecutionStep = (ProcessorExecutionStep) executionSteps.get(0);
        assertThat(processorExecutionStep.getProcessorName()).isEqualTo(name);
        assertThat(processorExecutionStep.getProcessor()).isInstanceOf(AddTagProcessor.class);

        Optional<List<ExecutionStep>> onFailureExecutionSteps = processorExecutionStep.getOnFailureExecutionSteps();
        assertThat(onFailureExecutionSteps.isPresent()).isFalse();
    }

    @Test
    public void testParseOnFailureExecutionStep() {
        String onFailureName = RandomStringUtils.randomAlphanumeric(10);
        List<ExecutionStepDefinition> onFailureExecutionStepDefinitionList = Collections.singletonList(
            createAddTagStepDefinition(onFailureName)
        );

        List<ExecutionStepDefinition> executionStepDefinitionList = Collections.singletonList(
                createAddTagStepDefinition(RandomStringUtils.randomAlphanumeric(10), onFailureExecutionStepDefinitionList)
        );

        List<ExecutionStep> executionSteps = executionStepsParser.parse(executionStepDefinitionList);

        ProcessorExecutionStep processorExecutionStep = (ProcessorExecutionStep) executionSteps.get(0);
        List<ExecutionStep> onFailureExecutionSteps = processorExecutionStep.getOnFailureExecutionSteps().get();

        assertThat(onFailureExecutionSteps.size()).isEqualTo(1);

        ProcessorExecutionStep onFailureExecutionStep = (ProcessorExecutionStep) onFailureExecutionSteps.get(0);
        assertThat(onFailureExecutionStep.getProcessorName()).isEqualTo(onFailureName);
        assertThat(onFailureExecutionStep.getProcessor()).isInstanceOf(AddTagProcessor.class);
    }

    @Test
    public void testParseConditionalExecutionStep() {
        ConditionDefinition conditionDefinition = createAndExistsConditionDefinition();

        String onTrueProcessorName = RandomStringUtils.randomAlphanumeric(10);

        String onFalseProcessorName = RandomStringUtils.randomAlphanumeric(10);

        ConditionalExecutionStepDefinition conditionalExecutionStepDefinition =
                new ConditionalExecutionStepDefinition(conditionDefinition,
                        Collections.singletonList(
                                createAddTagStepDefinition(onTrueProcessorName, null)
                        ),
                        Collections.singletonList(
                                createAddTagStepDefinition(onFalseProcessorName, null)
                        ));

        List<ExecutionStepDefinition> conditionalExecutionStepDefinitions = Collections.singletonList(conditionalExecutionStepDefinition);

        List<ExecutionStep> executionSteps = executionStepsParser.parse(conditionalExecutionStepDefinitions);
        assertThat(executionSteps.size()).isEqualTo(1);

        ConditionalExecutionStep conditionalExecutionStep = (ConditionalExecutionStep) executionSteps.get(0);

        assertThat(conditionalExecutionStep.getCondition()).isInstanceOf(AndCondition.class);

        ProcessorExecutionStep onTrueExecutionStep = (ProcessorExecutionStep) conditionalExecutionStep.getOnTrue().get(0);
        assertThat(onTrueExecutionStep.getProcessor()).isInstanceOf(AddTagProcessor.class);
        assertThat(onTrueExecutionStep.getProcessorName()).isEqualTo(onTrueProcessorName);

        ProcessorExecutionStep onFalseExecutionStep = (ProcessorExecutionStep) conditionalExecutionStep.getOnFalse().get(0);
        assertThat(onFalseExecutionStep.getProcessor()).isInstanceOf(AddTagProcessor.class);
        assertThat(onFalseExecutionStep.getProcessorName()).isEqualTo(onFalseProcessorName);


    }

    private ConditionDefinition createAndExistsConditionDefinition() {
        return new ConditionDefinition("and", createMap("conditions", createList(
                new ConditionDefinition("exists", createMap(
                        "field", "field1"
                )),
                new ConditionDefinition("exists", createMap(
                        "field", "field2"
                ))
        )));
    }

    private ProcessorExecutionStepDefinition createAddTagStepDefinition(String name) {
        return createAddTagStepDefinition(name, null);
    }

    private ProcessorExecutionStepDefinition createAddTagStepDefinition(String name, List<ExecutionStepDefinition> onFailureExecutionStepDefinitions) {
        return new ProcessorExecutionStepDefinition(createAddTagProcessorDefinition(), name, onFailureExecutionStepDefinitions);
    }

    private ProcessorDefinition createAddTagProcessorDefinition() {
        return new ProcessorDefinition("addTag", createMap("tags", createList("tag1", "tag2")));
    }

}