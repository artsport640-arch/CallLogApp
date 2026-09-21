package com.example.calllogapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CallLogEntity.class}, version = 1, exportSchema = false)
public abstract class CallLogDatabase extends RoomDatabase {
    
    private static CallLogDatabase instance;

    public abstract CallLogDao callLogDao();

    public static synchronized CallLogDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    CallLogDatabase.class,
                    "call_log_database"
            ).build();
        }
        return instance;
    }
}
