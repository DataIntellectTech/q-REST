package uk.co.aquaq.kdb.connection;

import com.kx.c;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class KdbConnectionWrapperTest {

    private static final String KDB_INSERT_U_UPD = ".u.upd";
    final String host = "host";
    final int port = 1;
    final String username = "username";
    final String password = "password";
    @Mock
    private c c;




    @InjectMocks
    private KdbConnectionWrapper kdbConnection = new KdbConnectionWrapper();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldCloseConnectionSuccessfully() throws Exception {
        doNothing().when(c).close();
        kdbConnection.preDestroy();
        verify(c, times(1)).close();
    }

    @Test
    public void shouldThrowKdbConnectionExceptionWhenUnabeToCloseConnection() throws Exception {
        kdbConnection.setConnectionToKdb(null);
        thrown.expect(KdbConnectionException.class);
        thrown.expectMessage("Cannot close Connection");
        kdbConnection.preDestroy();
    }

    @Test
    public void shouldThrowKdbConnectionExceptionWhenNoConnection() throws Exception {
        ReflectionTestUtils.setField(kdbConnection, "connectionToKdb", null);
        thrown.expect(KdbConnectionException.class);
        thrown.expectMessage("Cannot close Connection");
        kdbConnection.preDestroy();
    }


}