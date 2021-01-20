package io.github.niestrat99.advancedteleport.sql;

import io.github.niestrat99.advancedteleport.CoreClass;

import java.sql.Connection;

public abstract class SQLManager {

    protected Connection connection;

    public SQLManager() {



        createTable();
        transferOldData();
    }


    public abstract void createTable();

    public abstract void transferOldData();

    public interface SQLCallback<D> {
        void onSuccess(D data);

        default void onFail() {}
    }


}
