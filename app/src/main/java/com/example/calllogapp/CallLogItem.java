package com.example.calllogapp;

public class CallLogItem {
    private String name;
    private String number;
    private String callType;
    private int iconRes;
    private String date;
    private String duration;

    public CallLogItem(String name, String number, String callType, int iconRes, String date, String duration) {
        this.name = name;
        this.number = number;
        this.callType = callType;
        this.iconRes = iconRes;
        this.date = date;
        this.duration = duration;
    }

    public String getName() { return name; }
    public String getNumber() { return number; }
    public String getCallType() { return callType; }
    public int getIconRes() { return iconRes; }
    public String getDate() { return date; }
    public String getDuration() { return duration; }
}
