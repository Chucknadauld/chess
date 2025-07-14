package dataaccess;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {

    private final HashMap<String, Object> dummyStore = new HashMap<>(); //placeholderx

    @Override
    public void clear() throws DataAccessException {
        dummyStore.clear();
    }
}
