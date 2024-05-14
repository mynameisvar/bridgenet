package me.moonways.bridgenet.test.jdbc.sql;

import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.api.inject.Inject;
import me.moonways.bridgenet.jdbc.core.DatabaseConnection;
import me.moonways.bridgenet.jdbc.core.ResponseStream;
import me.moonways.bridgenet.jdbc.core.TransactionIsolation;
import me.moonways.bridgenet.jdbc.core.util.result.Result;
import me.moonways.bridgenet.test.engine.BridgenetJUnitTestRunner;
import me.moonways.bridgenet.test.jdbc.sql.subj.DatabaseConnectionEventTestObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@Log4j2
@RunWith(BridgenetJUnitTestRunner.class)
public class NativeQueryCallTransactionalTest {

    @Inject
    private DatabaseConnection connection;

    @Before
    public void setUp() {
        connection.addObserver(new DatabaseConnectionEventTestObserver());
    }

    private Result<ResponseStream> getPlayersResponse() {
        connection.call("create table PLAYERS (id int not null unique auto_increment, name varchar(32) not null unique)");
        connection.call("insert into PLAYERS (NAME) values ('lyx')")
                .map(response -> response.find(1))
                .whenCompleted(log::debug);

        return connection.call("select * from PLAYERS");
    }

    @Test
    public void test_serializableTransactionGet() {
        connection.ofTransactionalGet(this::getPlayersResponse)
                .whenCompleted(log::debug);
    }

    @Test
    public void test_repeatableReadTransactionGet() {
        connection.ofTransactionalGet(TransactionIsolation.REPEATABLE_READ, this::getPlayersResponse)
                .whenCompleted(log::debug);
    }
}
