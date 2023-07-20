package me.moonways.bridgenet.mtp.config;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CipherSecurity {

    private final String publicKey;
    private final String privateKey;

    public boolean isFilled() {
        return (publicKey != null && !publicKey.isEmpty()) && (privateKey != null && !privateKey.isEmpty());
    }
}
