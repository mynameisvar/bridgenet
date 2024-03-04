package me.moonways.bridgenet.test.services;

import me.moonways.bridgenet.api.inject.Inject;
import me.moonways.bridgenet.test.engine.BridgenetJUnitTestRunner;
import me.moonways.bridgenet.model.friends.FriendsList;
import me.moonways.bridgenet.model.friends.FriendsServiceModel;
import me.moonways.bridgenet.test.engine.persistance.Order;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.rmi.RemoteException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(BridgenetJUnitTestRunner.class)
public class FriendsServiceEndpointTest {

    private static final UUID PLAYER_ID = UUID.randomUUID();
    private static final UUID FRIEND_ID = UUID.randomUUID();

    @Inject
    private FriendsServiceModel friendsServiceModel;

    @Test
    @Order(0)
    public void test_friendAdd() throws RemoteException {
        FriendsList friendsList = friendsServiceModel.getFriends(PLAYER_ID);
        friendsList.addFriend(FRIEND_ID);

        assertEquals(1, friendsList.getFriendsIDs().size());
    }

    @Test
    @Order(1)
    public void test_friendRemove() throws RemoteException {
        FriendsList friendsList = friendsServiceModel.getFriends(PLAYER_ID);
        friendsList.removeFriend(FRIEND_ID);

        assertEquals(0, friendsList.getFriendsIDs().size());
    }
}
