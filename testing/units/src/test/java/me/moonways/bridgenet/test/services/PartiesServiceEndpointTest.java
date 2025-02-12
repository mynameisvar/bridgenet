package me.moonways.bridgenet.test.services;

import me.moonways.bridgenet.api.inject.Inject;
import me.moonways.bridgenet.model.service.parties.PartiesServiceModel;
import me.moonways.bridgenet.model.service.parties.Party;
import me.moonways.bridgenet.test.data.TestConst;
import me.moonways.bridgenet.test.engine.ModernTestEngineRunner;
import me.moonways.bridgenet.test.engine.component.module.impl.RmiServicesModule;
import me.moonways.bridgenet.test.engine.persistance.TestModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.rmi.RemoteException;

import static org.junit.Assert.assertEquals;

@RunWith(ModernTestEngineRunner.class)
@TestModules(RmiServicesModule.class)
public class PartiesServiceEndpointTest {

    @Inject
    private PartiesServiceModel serviceModel;

    @Test
    public void test_partyCreate() throws RemoteException {
        Party party = serviceModel.createParty(TestConst.Player.NICKNAME);

        assertEquals(party.getOwner().getName(), TestConst.Player.NICKNAME);
        assertEquals(party.getTotalMembersCount(), 0);
    }
}
