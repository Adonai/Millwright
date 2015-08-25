package com.adonai.millwright.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.adonai.millwright.db.entities.Request;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Helper class for managing OrmLite database and DAOs
 *
 * @author Adonai
 */
public class PersistManager extends OrmLiteSqliteOpenHelper {
    private static final String TAG = PersistManager.class.getSimpleName();

    private static final String DATABASE_NAME ="millwright.db";

    private static final int DATABASE_VERSION = 1;

    //Dao fast access links
    private RuntimeExceptionDao<Request, Long> requestDao;

    public PersistManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Request.class);
        } catch (SQLException e) {
            Log.e(TAG, "error creating DB " + DATABASE_NAME);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVer, int newVer) {

    }

    @NonNull
    public RuntimeExceptionDao<Request, Long> getRequestDao() {
        if (requestDao == null) {
            requestDao = getRuntimeExceptionDao(Request.class);
        }
        return requestDao;
    }
}
