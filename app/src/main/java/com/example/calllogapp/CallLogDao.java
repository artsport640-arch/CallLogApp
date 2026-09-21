package com.example.calllogapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    List<CallLogEntity> getAllCallLogs();

    @Query("SELECT * FROM call_logs WHERE timestamp = :timestamp LIMIT 1")
    CallLogEntity getCallLogByTimestamp(long timestamp);

    @Insert
    void insert(CallLogEntity callLog);

    @Update
    void update(CallLogEntity callLog);

    @Delete
    void delete(CallLogEntity callLog);

    @Query("DELETE FROM call_logs")
    void deleteAll();
}
