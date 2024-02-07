package me.moonways.bridgenet.api.inject.bean;

import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.api.inject.PostConstruct;
import me.moonways.bridgenet.api.inject.PreConstruct;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Log4j2
public class BeanConstructFunction extends AnnotatedBeanComponent<Method> {
    public BeanConstructFunction(Bean bean, Method root) {
        super(bean, root);
    }

    /**
     * Проверяем, должна ли функция выполняться после
     * инициализации и сохранения бина.
     */
    public boolean isBefore() {
        return isAnnotated(PreConstruct.class);
    }

    /**
     * Проверяем, должна ли функция выполняться перед
     * инициализацией и сохранением бина.
     */
    public boolean isAfter() {
        return isAnnotated(PostConstruct.class);
    }

    /**
     * Проверить функцию на наличие конфликтов.
     */
    public boolean hasConflicts() {
        return (isAfter() && isBefore()) || (isBefore() && !isStatic()) || getRoot().getParameterCount() != 0;
    }

    /**
     * Проверить функцию на статичность.
     */
    private boolean isStatic() {
        return (getRoot().getModifiers() & Modifier.STATIC) == Modifier.STATIC;
    }

    /**
     * Вызвать исполнение функции
     * инициализации бина.
     */
    public void invoke() {
        if (hasConflicts()) {
            throw new BeanException("Init function " + this + " has conflicts");
        }

        Method method = getRoot();
        Bean bean = getBean();

        method.setAccessible(true);

        try {
            method.invoke(bean.getRoot());
            log.info("Invoked bean construct-function of §2" + method);

        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new BeanException(exception);
        }
    }
}
