package service;

import dataaccess.DataAccessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ClearServiceTest {

    @Test
    public void positiveClearTest() {
        ClearService service = new ClearService();
        assertDoesNotThrow(service::clearApplication);
    }
}
