/*
 * Copyright 2017-2024 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.typepollution;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.TypeConstantAdjustment;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * {@link AgentBuilder.Transformer} implementation that installs the necessary hooks to call into
 * the {@link FocusListener}.
 */
public final class TypePollutionTransformer implements AgentBuilder.Transformer {
    private TypePollutionTransformer() {
    }

    /**
     * Create a new instance of this transformer.
     *
     * @return The instance
     */
    public static AgentBuilder.Transformer create() {
        return new TypePollutionTransformer();
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
        if (classLoader != null) {
            try {
                classLoader.loadClass(HookBootstrap.class.getName());
            } catch (ClassNotFoundException e) {
                return builder;
            }
        }

        return builder
            .visit(TypeConstantAdjustment.INSTANCE)
            .visit(new VisitorWrapperImpl());
    }

    /**
     * Install this transformer into the given {@link Instrumentation}.
     *
     * @param instrumentation The instrumentation used for modifying classes
     */
    public static void install(Instrumentation instrumentation) {
        new AgentBuilder.Default()
            .with(AgentBuilder.Listener.StreamWriting.toSystemError().withErrorsOnly())
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(AgentBuilder.LambdaInstrumentationStrategy.DISABLED)
            .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
            .type(ElementMatchers.any()
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("net.bytebuddy.")))
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("com.sun")))
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith(TypePollutionTransformer.class.getPackageName())))
            )
            .transform(create())
            .installOn(instrumentation);
    }
}
