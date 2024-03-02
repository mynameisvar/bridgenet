package me.moonways.bridgenet.api.inject;

import lombok.*;
import me.moonways.bridgenet.api.inject.bean.BeanComponent;
import me.moonways.bridgenet.api.inject.bean.BeanException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BeanPropertyWrapper {

    private static final Function<String, Class<?>> TO_CLASS_MAPPER = ((value) -> {
        try {
            return Class.forName(value);
        } catch (ClassNotFoundException exception) {
            throw new BeanException(exception);
        }
    });

    /**
     * Создать проперти бина исходя из вложенных
     * параметров в компонент бина, из которого
     * и создаем данные о проперти.
     *
     * @param component - компонент бина.
     */
    public static BeanPropertyWrapper from(BeanComponent component) {
        Optional<String> propertyKey = component.getPropertyKey();
        if (!propertyKey.isPresent()) {
            throw new BeanException("System property isn`t found for " + component.getRoot());
        }
        return new BeanPropertyWrapper(propertyKey.orElse(null));
    }

    @Getter
    private final String property;

    /**
     * Установить новое значение в проперти
     * @param value - новое значение
     */
    public void set(@NotNull Object value) {
        System.setProperty(property, value.toString());
    }

    /**
     * Получить значение проперти по ключу
     * в виде строки.
     */
    public Optional<String> getAsString() {
        return Optional.ofNullable(System.getProperty(property));
    }

    /**
     * Получить значение проперти по ключу
     * в виде целочисленного числа.
     */
    public Optional<Integer> getAsInt() {
        return getAsString().map(Integer::parseInt);
    }

    /**
     * Получить значение проперти по ключу
     * в виде числа с плавающей точкой.
     */
    public Optional<Double> getAsDouble() {
        return getAsString().map(Double::parseDouble);
    }

    /**
     * Получить значение проперти по ключу
     * в виде Float.
     */
    public Optional<Float> getAsFloat() {
        return getAsString().map(Float::parseFloat);
    }

    /**
     * Получить значение проперти по ключу
     * в виде булеанового выражения.
     */
    public Optional<Boolean> getAsBoolean() {
        return getAsString().map(Boolean::parseBoolean);
    }

    /**
     * Получить значение проперти по ключу
     * в виде класса
     */
    public Optional<Class<?>> getAsClass() {
        return getAsString().map(TO_CLASS_MAPPER);
    }
}