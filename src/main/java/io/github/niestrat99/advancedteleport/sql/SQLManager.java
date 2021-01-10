package io.github.niestrat99.advancedteleport.sql;

import java.sql.Connection;

public abstract class SQLManager {

    protected static SQLManager instance;
    protected Connection connection;

    public SQLManager() {
        instance = this;
        
        createTable();
        transferOldData();
    }

    public static SQLManager getInstance() {
        return instance;
    }

    public abstract void createTable();

    public abstract void transferOldData();
}
