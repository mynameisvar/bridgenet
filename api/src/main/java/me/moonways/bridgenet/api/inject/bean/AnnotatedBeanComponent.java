package me.moonways.bridgenet.api.inject.bean;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public abstract class AnnotatedBeanComponent<V extends AnnotatedElement> {

    @Getter
    @ToString.Exclude
    private final transient Bean bean;

    @Getter
    @EqualsAndHashCode.Include
    private final V root;

    /**
     * Получить аннотацию из компонента бина по ее типу.
     *
     * @param annotationType - тип аннотации, которую ищем.
     */
    public <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationType) {
        return Optional.ofNullable(root.getDeclaredAnnotation(annotationType));
    }

    /**
     * Проверить на наличие аннотации у компонента по ее типу.
     *
     * @param annotationType - тип аннотации, которую ищем.
     */
    public boolean isAnnotated(Class<? extends Annotation> annotationType) {
        return root.isAnnotationPresent(annotationType);
    }
}
