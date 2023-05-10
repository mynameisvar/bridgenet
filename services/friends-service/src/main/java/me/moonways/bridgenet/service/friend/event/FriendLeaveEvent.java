package me.moonways.bridgenet.service.friend.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class FriendLeaveEvent {

    private final String playerName;
    private final String leavedFriendName;
}
