package com.example.calllogapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "call_logs")
public class CallLogEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String number;
    private String callType;
    private int iconRes;
    private String date;
    private String duration;
    private long timestamp;

    public CallLogEntity(String name, String number, String callType, int iconRes, 
                         String date, String duration, long timestamp) {
        this.name = name;
        this.number = number;
        this.callType = callType;
        this.iconRes = iconRes;
        this.date = date;
        this.duration = duration;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }
    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
