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
import me.moonways.bridgenet.mtp.message.inject.ClientMessage;
import me.moonways.bridgenet.mtp.message.MessageWrapper;
import me.moonways.bridgenet.mtp.message.inject.MessageHandler;
import me.moonways.bridgenet.mtp.message.MessageRegistry;
import me.moonways.bridgenet.mtp.pipeline.NettyPipeline;
import me.moonways.bridgenet.injection.DependencyInjection;
import net.conveno.jdbc.ConvenoRouter;
import org.jetbrains.annotations.NotNull;

public class BaseBridgenetConnector {

    private MTPConnectionFactory connectionProperties;

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
        // netty connection settings.
        System.setProperty(MTPConnectionFactory.HOST_PROPERTY_KEY, "localhost");
        System.setProperty(MTPConnectionFactory.PORT_PROPERTY_KEY, "8080");

        // jdbc settings.
        System.setProperty("system.jdbc.username", "username");
        System.setProperty("system.jdbc.password", "password");
    }

    private void applyDependencyInjection(BaseBridgenetConnector currentConnector) {
        DependencyInjection dependencyInjection = new DependencyInjection(); // TODO - Забрать из модуля bootstrap

        // local system services.
        dependencyInjection.bind(dependencyInjection);

        // dependencies services.
        dependencyInjection.bind(ConvenoRouter.create());

        // inject
        dependencyInjection.findComponentsIntoBasePackage();
        dependencyInjection.bind(currentConnector);

        dependencyInjection.findComponentsIntoBasePackage(ClientMessage.class);
        dependencyInjection.findComponentsIntoBasePackage(MessageHandler.class);

        // bridgenet system
        dependencyInjection.bind(connectionProperties);
    }

    public void enableBridgenetServicesSync(BaseBridgenetConnector currentConnector) {
        setProperties();
        connectionProperties = MTPConnectionFactory.createFromSystemProperties();

        applyDependencyInjection(currentConnector);

        registerBridgenetMessages(messageRegistry);
        connectToMTPServer(connectionProperties);
    }

    public void connectToMTPServer(@NotNull MTPConnectionFactory connectionProperties) {
        ChannelFactory<? extends Channel> clientChannelFactory = NettyFactory.createClientChannelFactory();

        NettyPipeline channelInitializer = NettyPipeline.create(driver);
        EventLoopGroup parentWorker = NettyFactory.createEventLoopGroup(2);

        MTPClient client = MTPConnectionFactory.newClientBuilder(connectionProperties)
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
