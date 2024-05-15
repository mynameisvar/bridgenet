package me.moonways.bridgenet.test.engine.persistance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestSleeping {

    PersistenceAcceptType acceptType() default PersistenceAcceptType.BEFORE_EXECUTION;

    int value();
}