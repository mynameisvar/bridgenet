package me.moonways.bridgenet.api.inject.bean.factory.type;

import me.moonways.bridgenet.api.inject.Inject;
import me.moonways.bridgenet.api.inject.bean.BeanException;
import me.moonways.bridgenet.api.inject.bean.factory.BeanFactory;
import me.moonways.bridgenet.api.inject.bean.service.BeansStore;
import me.moonways.bridgenet.api.util.reflection.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConstructorFactory implements BeanFactory {

    @Inject
    private BeansStore store;

    @SuppressWarnings("unchecked")
    private <T> Optional<Constructor<T>> lookupConstructor(Class<T> cls) throws NoSuchMethodException {
        Constructor<T>[] constructorsArr = (Constructor<T>[]) cls.getConstructors();

        if (constructorsArr.length == 0) {
            return Optional.empty();
        }

        Constructor<T> first = constructorsArr[0];
        if (constructorsArr.length == 1) {
            return Optional.of(first);
        }

        List<Constructor<T>> list = Stream.of(constructorsArr).filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            return Optional.ofNullable(cls.getConstructor()).map(tConstructor -> first);
        }
        if (list.size() > 1) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    @Override
    public <T> T create(Class<T> cls) {
        try {
            Constructor<T> constructor = lookupConstructor(cls)
                    .orElseThrow(() -> new BeanException("no constructor found for " + cls));

            ReflectionUtils.grantAccess(constructor);
            Object[] args = parametersToArguments(constructor.getParameterTypes());

            return constructor.newInstance(args);
        } catch (Exception exception) {
            throw new BeanException(cls.getName(), exception);
        }
    }

    private Object[] parametersToArguments(Class<?>[] parameters) {
        if (parameters.length == 0) {
            return new Object[0];
        }
        return Stream.of(parameters).map(aClass -> store.find(aClass).orElse(null))
                .map(bean -> bean == null ? null : bean.getRoot())
                .toArray(Object[]::new);
    }
}
