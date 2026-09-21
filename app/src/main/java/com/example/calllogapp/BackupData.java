package com.example.calllogapp;

import java.util.List;

public class BackupData {
    private long backupTime;
    private String appVersion;
    private List<CallLogEntity> callLogs;

    public BackupData() {}

    public BackupData(long backupTime, String appVersion, List<CallLogEntity> callLogs) {
        this.backupTime = backupTime;
        this.appVersion = appVersion;
        this.callLogs = callLogs;
    }

    public long getBackupTime() { return backupTime; }
    public void setBackupTime(long backupTime) { this.backupTime = backupTime; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    public List<CallLogEntity> getCallLogs() { return callLogs; }
    public void setCallLogs(List<CallLogEntity> callLogs) { this.callLogs = callLogs; }
}
