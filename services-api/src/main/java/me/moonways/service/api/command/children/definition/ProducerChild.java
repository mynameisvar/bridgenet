package me.moonways.service.api.command.children.definition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.moonways.service.api.command.children.CommandChild;

import java.lang.reflect.Method;

@Getter
@RequiredArgsConstructor
public class ProducerChild implements CommandChild {

    private final Object parent;
    private final Method method;

    private final String name;
    private final String permission;
}
