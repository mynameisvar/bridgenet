package me.moonways.bridgenet.connector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import me.moonways.bridgenet.connector.reconnect.BridgenetReconnectHandler;
import me.moonways.bridgenet.mtp.*;
import me.moonways.bridgenet.mtp.MTPClient;
import me.moonways.bridgenet.mtp.config.MTPConfiguration;
import me.moonways.bridgenet.mtp.message.MessageWrapper;
import me.moonways.bridgenet.mtp.message.MessageRegistry;
import me.moonways.bridgenet.mtp.pipeline.NettyPipelineInitializer;
import me.moonways.bridgenet.api.inject.DependencyInjection;
import net.conveno.jdbc.ConvenoRouter;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class BaseBridgenetConnector {

    private MTPConnectionFactory connectionFactory;

    private MTPDriver driver;
    private MessageRegistry messageRegistry;

    @Setter
    @Getter
    private MTPChannel channel;

    protected void initializeConnectorData(MTPDriver driver,
                                           MessageRegistry messageRegistry) {

        this.driver = driver;
        this.messageRegistry = messageRegistry;
    }

    protected void registerBridgenetMessages(@NotNull MessageRegistry messageRegistry) {
        // override me.
    }

    public void setProperties() {
        // jdbc settings.
        System.setProperty("system.jdbc.username", "username");
        System.setProperty("system.jdbc.password", "password");
    }

    private void applyDependencyInjection(BaseBridgenetConnector currentConnector) {
        DependencyInjection injector = new DependencyInjection(); // TODO - Забрать из модуля bootstrap

        injector.searchByProject();

        injector.bind(ConvenoRouter.create());
        injector.bind(currentConnector);
        injector.bind(new Properties());
        injector.bind(connectionFactory);
    }

    public void enableBridgenetServicesSync(DependencyInjection injector, BaseBridgenetConnector currentConnector) {
        setProperties();
        connectionFactory = MTPConnectionFactory.createConnectionFactory(injector);

        applyDependencyInjection(currentConnector);

        registerBridgenetMessages(messageRegistry);
        connectToMTPServer(connectionFactory);
    }

    public void connectToMTPServer(@NotNull MTPConnectionFactory connectionFactory) {
        ChannelFactory<? extends Channel> clientChannelFactory = NettyFactory.createClientChannelFactory();

        MTPConfiguration configuration = connectionFactory.getConfiguration();
        NettyPipelineInitializer channelInitializer = NettyPipelineInitializer.create(driver, configuration);

        EventLoopGroup parentWorker = NettyFactory.createEventLoopGroup(configuration.getSettings().getWorkers().getBossThreads());

        MTPClient client = MTPConnectionFactory.newClientBuilder(connectionFactory)
                .setGroup(parentWorker)
                .setChannelFactory(clientChannelFactory)
                .setChannelInitializer(channelInitializer)
                .build();

        channelInitializer.addChannelHandler(new BridgenetReconnectHandler(client, this));
        channel = client.connectSync();
    }

    @Synchronized
    public void sendMessage(MessageWrapper message) {
        channel.sendMessage(message);
    }
}
