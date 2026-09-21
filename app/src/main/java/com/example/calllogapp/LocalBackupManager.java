package com.example.calllogapp;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.List;

public class LocalBackupManager {
    
    private static final String BACKUP_FILE = "call_log_backup.json";
    private Context context;
    private CallLogDatabase database;
    
    public LocalBackupManager(Context context) {
        this.context = context;
        this.database = CallLogDatabase.getInstance(context);
    }
    
    public boolean createBackup() {
        try {
            List<CallLogEntity> callLogs = database.callLogDao().getAllCallLogs();
            
            BackupData backupData = new BackupData(
                    System.currentTimeMillis(),
                    "1.0",
                    callLogs
            );
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(backupData);
            
            FileOutputStream fos = context.openFileOutput(BACKUP_FILE, Context.MODE_PRIVATE);
            fos.write(json.getBytes("UTF-8"));
            fos.close();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public BackupData loadBackup() {
        try {
            File file = new File(context.getFilesDir(), BACKUP_FILE);
            if (!file.exists()) {
                return null;
            }
            
            FileInputStream fis = context.openFileInput(BACKUP_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            
            Gson gson = new Gson();
            return gson.fromJson(sb.toString(), BackupData.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean restoreFromBackup() {
        try {
            BackupData backupData = loadBackup();
            if (backupData == null || backupData.getCallLogs() == null) {
                return false;
            }
            
            database.callLogDao().deleteAll();
            
            for (CallLogEntity entity : backupData.getCallLogs()) {
                database.callLogDao().insert(entity);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public File getBackupFile() {
        return new File(context.getFilesDir(), BACKUP_FILE);
    }
}
