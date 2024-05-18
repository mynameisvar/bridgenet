package me.moonways.endpoint.players;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.moonways.bridgenet.model.players.Player;
import me.moonways.bridgenet.model.players.PlayersServiceModel;
import me.moonways.bridgenet.rsi.endpoint.persistance.EndpointRemoteContext;
import me.moonways.bridgenet.rsi.endpoint.persistance.EndpointRemoteObject;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

@Getter
@Accessors(fluent = true)
public final class PlayersServiceEndpoint extends EndpointRemoteObject implements PlayersServiceModel {

    private static final long serialVersionUID = 5074638195342022234L;

    private final PlayerLevelingStub leveling = new PlayerLevelingStub();
    private final PlayerStoreStub store = new PlayerStoreStub();

    public PlayersServiceEndpoint() throws RemoteException {
        super();
    }

    @Override
    protected void construct(EndpointRemoteContext context) {
        context.inject(leveling);
        context.inject(store);
    }

    @Override
    public int getTotalOnline() throws RemoteException {
        return getOnlinePlayers().size();
    }

    @Override
    public List<Player> getOnlinePlayers() throws RemoteException {
        return Collections.emptyList();
    }
}
