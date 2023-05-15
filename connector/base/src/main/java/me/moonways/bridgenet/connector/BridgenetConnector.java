package me.moonways.bridgenet.connector;

import me.moonways.bridgenet.connector.reconnect.BridgenetReconnectHandler;
import me.moonways.bridgenet.protocol.*;
import me.moonways.bridgenet.protocol.pipeline.BridgenetPipeline;
import me.moonways.bridgenet.service.inject.Component;
import me.moonways.bridgenet.service.inject.Inject;
import org.jetbrains.annotations.NotNull;

@Component
public class BridgenetConnector {

    @Inject
    private ProtocolControl protocolControl;

    public void connect(@NotNull Bridgenet bridgenet) {
        BridgenetPipeline bridgenetPipeline = BridgenetPipeline.
                newBuilder(protocolControl).build();

        BridgenetClient client = Bridgenet.newClientBuilder(bridgenet, protocolControl)
                .setGroup(BridgenetNetty.createEventLoopGroup(2))
                .setChannelFactory(BridgenetNetty.createClientChannelFactory())
                .setChannelInitializer(bridgenetPipeline)
                .build();

        bridgenetPipeline.addChannelHandler(new BridgenetReconnectHandler(client));

        client.connectSync();
    }
}