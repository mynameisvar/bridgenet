package me.moonways.bridgenet.api.command.children.definition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.moonways.bridgenet.api.command.children.CommandChild;

import java.lang.reflect.Method;

@Getter
@RequiredArgsConstructor
public final class MentorChild implements CommandChild { //недодрочилдрен

    private final Object parent;
    private final Method method;
}