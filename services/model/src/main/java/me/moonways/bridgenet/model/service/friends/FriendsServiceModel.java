package me.moonways.bridgenet.model.service.friends;

import me.moonways.bridgenet.rmi.service.RemoteService;

import java.rmi.RemoteException;
import java.util.UUID;

public interface FriendsServiceModel extends RemoteService {

    FriendsList getFriends(UUID playerUUID) throws RemoteException;

    FriendsList getFriends(String playerName) throws RemoteException;
}
