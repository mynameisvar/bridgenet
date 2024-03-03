package me.moonways.bridgenet.api.modern_command.depend.cooldown.info;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Getter
public class CooldownInfoImpl implements CooldownInfo {

    private final long duration;
    private final TimeUnit time;
}