package me.moonways.bridgenet.test.jdbc.entity;

import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.api.inject.Inject;
import me.moonways.bridgenet.jdbc.entity.EntityID;
import me.moonways.bridgenet.jdbc.entity.EntityRepository;
import me.moonways.bridgenet.jdbc.entity.EntityRepositoryFactory;
import me.moonways.bridgenet.jdbc.entity.util.search.SearchMarker;
import me.moonways.bridgenet.test.engine.BridgenetJUnitTestRunner;
import me.moonways.bridgenet.test.engine.jdbc.entity.StatusEntity;
import me.moonways.bridgenet.test.engine.jdbc.entity.UserEntity;
import me.moonways.bridgenet.test.engine.persistance.Order;
import me.moonways.bridgenet.test.engine.persistance.SleepExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Optional;

import static org.junit.Assert.*;

@Log4j2
@RunWith(BridgenetJUnitTestRunner.class)
public class JdbcEntityRepositoryTest {

    private static final StatusEntity STATUS_1 = StatusEntity.builder().name("StandUp").build();
    private static final StatusEntity STATUS_2 = StatusEntity.builder().name("SpiderMan").build();
    private static final StatusEntity STATUS_3 = StatusEntity.builder().name("CEO").build();

    private static final UserEntity USER_1 = UserEntity.builder().firstName("Oleg").lastName("Saburov").age(36).statusEntity(STATUS_1).build();
    private static final UserEntity USER_2 = UserEntity.builder().firstName("Piter").lastName("Parker").age(17).statusEntity(STATUS_2).build();
    private static final UserEntity USER_3 = UserEntity.builder().firstName("Mark").lastName("Zukerberg").age(45).statusEntity(STATUS_3).build();

    private EntityRepository<StatusEntity> statusRepository;
    private EntityRepository<UserEntity> userRepository;

    @Inject
    private EntityRepositoryFactory repositoryFactory;

    @Before
    public void setUp() {
        this.statusRepository = repositoryFactory.fromEntityType(StatusEntity.class);
        this.userRepository = repositoryFactory.fromEntityType(UserEntity.class);
    }

    @Test
    @Order(0)
    public void test_userInsert() {
        EntityID entityID = userRepository.insert(USER_1);
        assertEquals(1, entityID.getId());

        log.debug("Inserted user identify: {}", entityID);

        if (entityID.isIncorrect()) {

            if (entityID.isNotFound()) {
                log.debug("entity id is not found");
            }

            if (entityID.isNotGenerated()) {
                log.debug("entity id is not auto-generated");
            }
        }
    }

    @Test
    @Order(1)
    @SleepExecution(duration = 500)
    public void test_userGet() {
        Optional<UserEntity> userOptional = userRepository.searchIf(
                userRepository.newSearchMarker()
                        .withGet(UserEntity::getId, 1)
                        .withGet(UserEntity::getFirstName, USER_1.getFirstName()));

        assertTrue(userOptional.isPresent());
        log.debug("Founded user: {}", userOptional.orElse(null));
    }

    @Test
    @Order(2)
    @SleepExecution(duration = 500)
    public void test_statusGet() {
        Optional<StatusEntity> statusOptional = statusRepository.searchIf(
                statusRepository.newSearchMarker()
                        .withGet(StatusEntity::getId, 1)
                        .withGet(StatusEntity::getName, STATUS_1.getName()));

        assertTrue(statusOptional.isPresent());
        log.debug("Founded status: {}", statusOptional.orElse(null));
    }

    @Test
    @Order(3)
    @SleepExecution(duration = 750)
    public void test_userDelete() {
        SearchMarker<UserEntity> searchMarker = userRepository.newSearchMarker()
                .withGet(UserEntity::getId, 1)
                .withGet(UserEntity::getFirstName, USER_1.getFirstName());

        userRepository.deleteIf(searchMarker);

        Optional<UserEntity> userOptional = userRepository.searchIf(searchMarker);

        assertFalse(userOptional.isPresent());
        log.debug("User is deleted successful");
    }

    @Test
    @Order(4)
    @SleepExecution(duration = 750)
    public void test_statusDelete() {
        SearchMarker<StatusEntity> searchMarker = statusRepository.newSearchMarker()
                .withGet(StatusEntity::getId, 1)
                .withGet(StatusEntity::getName, STATUS_1.getName());

        statusRepository.deleteIf(searchMarker);

        Optional<StatusEntity> statusOptional = statusRepository.searchIf(searchMarker);

        assertFalse(statusOptional.isPresent());
        log.debug("Status is deleted successful");
    }
}
