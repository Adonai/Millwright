package com.adonai.millwright.db;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;

/**
 * Helper class for retrieving database instance
 *
 * @author Adonai
 */
public class DbProvider {
    private static PersistManager databaseHelper;

    public static PersistManager getHelper(){
        return databaseHelper;
    }

    public static void setHelper(Context context) {
        databaseHelper = OpenHelperManager.getHelper(context, PersistManager.class);
    }

    public static void releaseHelper() {
        OpenHelperManager.releaseHelper();
        databaseHelper = null;
    }
}
