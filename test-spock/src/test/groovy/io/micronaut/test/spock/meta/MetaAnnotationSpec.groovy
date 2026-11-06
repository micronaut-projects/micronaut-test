package io.micronaut.test.spock.meta

import io.micronaut.context.ApplicationContext
import io.micronaut.test.annotation.AnnotationUtils
import io.micronaut.test.extensions.AbstractMicronautExtension
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@MicronautTest(transactional = false, environments = "meta")
@interface NonTransactionalMicronautTest {
}

@NonTransactionalMicronautTest
class MetaAnnotationSpec extends Specification {

    @Inject
    ApplicationContext applicationContext

    void "attributes of the meta-annotation apply"() {
        expect:
        applicationContext != null
        applicationContext.environment.activeNames.contains("meta")
        applicationContext.getProperty(AbstractMicronautExtension.TEST_TRANSACTIONAL, Boolean).orElse(null) == false
    }
}

@NonTransactionalMicronautTest
abstract class MetaAnnotatedBase {
}

class MetaAnnotatedSubclass extends MetaAnnotatedBase {
}

class MetaAnnotationLookupSpec extends Specification {

    void "a meta-annotation on a base class is found although it is not itself inherited"() {
        expect:
        AnnotationUtils.findAnnotation(MetaAnnotatedSubclass, MicronautTest).get().environments() == ["meta"] as String[]
    }

    void "a class without the annotation has none"() {
        expect:
        !AnnotationUtils.findAnnotation(MetaAnnotationLookupSpec, MicronautTest).isPresent()
    }
}
