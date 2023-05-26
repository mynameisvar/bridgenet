package me.moonways.bridgenet.system.connection;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.api.connection.server.AddressServerMap;
import me.moonways.bridgenet.api.connection.server.ServerManager;
import me.moonways.bridgenet.api.connection.server.type.GameServer;
import me.moonways.bridgenet.protocol.BridgenetChannel;
import me.moonways.bridgenet.protocol.message.Message;
import me.moonways.bridgenet.protocol.message.MessageHandler;
import me.moonways.bridgenet.protocol.message.MessageTrigger;
import me.moonways.bridgenet.service.game.GameArena;
import me.moonways.bridgenet.service.game.GameService;
import me.moonways.bridgenet.service.game.data.Arena;
import me.moonways.bridgenet.service.game.data.Game;
import me.moonways.bridgenet.service.game.data.Mode;
import me.moonways.bridgenet.service.inject.InitMethod;
import me.moonways.bridgenet.service.inject.Inject;
import me.moonways.bridgenet.services.connection.server.ServerConnectResponseType;
import me.moonways.bridgenet.services.connection.server.protocol.game.arena.ArenaCreateMessage;
import me.moonways.bridgenet.services.connection.server.protocol.game.arena.response.ArenaCreateResponseMessage;

import java.util.UUID;

@MessageHandler
@Log4j2
public class GameArenaHandshakeHandler extends AbstractHandshakeHandler {

    @Inject
    @Getter
    private ServerManager serverManager;

    @Inject
    private GameService gameService;

    @InitMethod
    private void init() {
        super.setServerManager(serverManager);
    }

    @MessageTrigger
    public void handle(ArenaCreateMessage message) {
        AddressServerMap addressServerMap = serverManager.getAddressServerMap();

        BridgenetChannel channel = message.getChannel();
        int clientPort = channel.getInetSocketAddress().getPort();

        GameServer gameServer = addressServerMap.getUncheckedServer(clientPort);

        if (gameServer == null) {
            writeResponseHandshake(message, ServerConnectResponseType.SERVER_NOT_FOUND);
            return;
        }

        GameArena gameArena = createArena(message);
        boolean registered = gameServer.addArena(gameArena);

        if (registered) {
            writeResponseHandshake(message, ServerConnectResponseType.ALREADY_CONNECTED);
            return;
        }

        gameService.registerArena(gameArena);
        writeResponseHandshake(message, ServerConnectResponseType.SUCCESSFUL_CONNECTED);

        log.info("Arena {} by game {} successful registered", gameArena.getArenaUUID(), gameServer.getName());
    }

    private GameArena createArena(ArenaCreateMessage message) {
        UUID arenaUUID = message.getArenaUUID();

        String mapName = message.getMapName();

        int gameId = message.getGameId();
        int modeId = message.getModeId();

        int maxPlayers = message.getMaxPlayers();

        Game game = gameService.getRegisteredGame(gameId);
        Mode mode = gameService.getRegisteredMode(modeId);

        Arena arena = Arena.create(maxPlayers, game, mode, arenaUUID, mapName);
        return new GameArena(arena);
    }

    @Override
    protected Message createResponseMessage(int connectionResponseTypeID) {
        return new ArenaCreateResponseMessage(connectionResponseTypeID);
    }
}