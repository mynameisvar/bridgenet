package me.moonways.bridgenet.api.modern_command.annotation.value;

import me.moonways.bridgenet.api.modern_command.entity.EntityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface EntityLevel {

    EntityType value();
}