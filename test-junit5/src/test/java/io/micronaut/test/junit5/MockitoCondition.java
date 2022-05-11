package io.micronaut.test.junit5;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MockitoCondition implements ExecutionCondition {
    static final String MOCKITO_ENABLED = "mockito.test.enabled";
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        boolean isEnabled = Boolean.getBoolean(MOCKITO_ENABLED);
        if (isEnabled) {
            return ConditionEvaluationResult.enabled("Mockito enabled");
        } else {
            return ConditionEvaluationResult.disabled("Mockito disabled");
        }
    }
}
