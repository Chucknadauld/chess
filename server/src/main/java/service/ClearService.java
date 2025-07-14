package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;

public class ClearService {

    private final DataAccess dataAccess;

    public ClearService() {
        this.dataAccess = new MemoryDataAccess(); // In the future, inject this insteadx
    }

    public void clearApplication() throws DataAccessException {
        dataAccess.clear();
    }
}
