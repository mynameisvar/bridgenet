package me.moonways.bridgenet.model.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.moonways.bridgenet.api.inject.Inject;
import me.moonways.bridgenet.mtp.message.persistence.ClientMessage;
import me.moonways.bridgenet.mtp.transfer.ByteTransfer;
import me.moonways.bridgenet.mtp.transfer.provider.TransferUuidProvider;

import java.util.UUID;

@Getter
@ToString
@ClientMessage
@NoArgsConstructor(onConstructor_ = @Inject)
@AllArgsConstructor
public class DeleteGame {

    @ByteTransfer(provider = TransferUuidProvider.class)
    private UUID gameId;

    @ByteTransfer(provider = TransferUuidProvider.class)
    private UUID activeId;
}