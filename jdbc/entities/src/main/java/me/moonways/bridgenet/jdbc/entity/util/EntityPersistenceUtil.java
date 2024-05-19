package me.moonways.bridgenet.jdbc.entity.util;

import lombok.*;
import lombok.experimental.UtilityClass;
import me.moonways.bridgenet.jdbc.core.compose.ParameterAddon;
import me.moonways.bridgenet.jdbc.entity.DatabaseEntityException;
import me.moonways.bridgenet.jdbc.entity.descriptor.EntityParametersDescriptor;
import me.moonways.bridgenet.jdbc.entity.persistence.Entity;
import me.moonways.bridgenet.jdbc.entity.persistence.EntityExternalParameter;
import me.moonways.bridgenet.jdbc.entity.persistence.EntityId;
import me.moonways.bridgenet.jdbc.entity.persistence.EntityParameter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class EntityPersistenceUtil {

    private static final int DEFAULT_ENTITY_ID_ORDER = -2;

    public boolean isEntity(Object object) {
        return isEntity(object.getClass());
    }

    public boolean isEntity(Class<?> entityClass) {
        return entityClass.isAnnotationPresent(Entity.class);
    }

    public String getEntityName(Object object) {
        return getEntityName(object.getClass());
    }

    public String getEntityName(Class<?> entityClass) {
        if (!isEntity(entityClass)) {
            throw new DatabaseEntityException("Class " + entityClass.getName() + " is not annotated as Entity");
        }
        return entityClass.getDeclaredAnnotation(Entity.class).name();
    }

    public boolean isEntityId(Method method) {
        return method.isAnnotationPresent(EntityId.class);
    }

    private boolean isExternalEntity(Method method) {
        return method.isAnnotationPresent(EntityExternalParameter.class);
    }

    public boolean isParameter(Method method) {
        return method.isAnnotationPresent(EntityParameter.class)
                || method.isAnnotationPresent(EntityId.class)
                || method.isAnnotationPresent(EntityExternalParameter.class);
    }

    public int getParameterOrderId(Method method) {
        if (isEntityId(method)) {
            return DEFAULT_ENTITY_ID_ORDER;
        }
        if (isExternalEntity(method)) {
            return method.getDeclaredAnnotation(EntityExternalParameter.class).order();
        }
        if (isParameter(method)) {
            return method.getDeclaredAnnotation(EntityParameter.class).order();
        }

        throw new DatabaseEntityException("method is not an entity parameter getter");
    }

    private String getIdOrDefault(String annotatedId, Method method) {
        return annotatedId == null || annotatedId.isEmpty() ? EntityParameterNameUtil.fromGetter(method).orElse(annotatedId) : annotatedId;
    }

    public String getParameterId(Method method) {
        if (isEntityId(method)) {
            return getIdOrDefault(method.getDeclaredAnnotation(EntityId.class).id(), method);
        }
        if (isExternalEntity(method)) {
            return getIdOrDefault(method.getDeclaredAnnotation(EntityExternalParameter.class).id(), method);
        }
        if (isParameter(method)) {
            return getIdOrDefault(method.getDeclaredAnnotation(EntityParameter.class).id(), method);
        }

        throw new DatabaseEntityException("method is not an entity parameter getter");
    }

    public ParameterAddon[] getParameterIndexes(Method method) {
        if (isEntityId(method)) {
            return method.getDeclaredAnnotation(EntityId.class).indexes();
        }
        if (isExternalEntity(method)) {
            return method.getDeclaredAnnotation(EntityExternalParameter.class).indexes();
        }
        if (isParameter(method)) {
            return method.getDeclaredAnnotation(EntityParameter.class).indexes();
        }

        throw new DatabaseEntityException("method is not an entity parameter getter");
    }

    public WrappedEntityParameter toParameterWrapper(Object source, Method method) {
        WrappedEntityParameter wrapper = toParameterUnvaluedWrapper(method);
        try {
            wrapper.unit = wrapper.getUnit().toBuilder()
                    .value(method.invoke(source))
                    .build();
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new DatabaseEntityException(exception);
        }
        return wrapper;
    }

    public WrappedEntityParameter toParameterUnvaluedWrapper(Method method) {
        List<ParameterAddon> parameterIndexes = new ArrayList<>(Arrays.asList(getParameterIndexes(method)));
        if (isEntityId(method)) {
            parameterIndexes.add(ParameterAddon.INCREMENTING);
        }
        EntityParametersDescriptor.ParameterUnit parameterUnit =
                EntityParametersDescriptor.ParameterUnit.builder()
                        .order(getParameterOrderId(method))
                        .id(getParameterId(method))
                        .indexes(parameterIndexes.toArray(new ParameterAddon[0]))
                        .isExternal(isExternalEntity(method))
                        .isAutoGenerated(isEntityId(method))
                        .type(method.getReturnType())
                        .build();
        return new WrappedEntityParameter(parameterUnit, method);
    }

    public List<WrappedEntityParameter> getParameters(Object source) {
        if (!isEntity(source)) {
            throw new DatabaseEntityException("Class " + source.getClass().getName() + " is not annotated as Entity");
        }
        return Arrays.stream(source.getClass().getDeclaredMethods())
                .filter(EntityPersistenceUtil::isParameter)
                .map(method -> toParameterWrapper(source, method))
                .collect(Collectors.toList());
    }

    public List<WrappedEntityParameter> getParameters(Class<?> entityClass) {
        if (!isEntity(entityClass)) {
            throw new DatabaseEntityException("Class " + entityClass.getName() + " is not annotated as Entity");
        }
        return Arrays.stream(entityClass.getDeclaredMethods())
                .filter(EntityPersistenceUtil::isParameter)
                .map(EntityPersistenceUtil::toParameterUnvaluedWrapper)
                .collect(Collectors.toList());
    }

    public Optional<WrappedEntityParameter> findEntityIDWrapper(Class<?> entityClass) {
        for (WrappedEntityParameter parameter : getParameters(entityClass)) {
            if (parameter.getUnit().isAutoGenerated()) {
                return Optional.of(parameter);
            }
        }

        return Optional.empty();
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class WrappedEntityParameter {

        private EntityParametersDescriptor.ParameterUnit unit;
        private Method invocation;
    }
}
