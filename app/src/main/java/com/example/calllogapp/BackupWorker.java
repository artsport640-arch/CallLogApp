package com.example.calllogapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BackupWorker extends Worker {
    
    public BackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            SharedPreferences prefs = getApplicationContext()
                    .getSharedPreferences("CallLogPrefs", Context.MODE_PRIVATE);
            boolean isHidden = prefs.getBoolean("isHidden", false);
            
            LocalBackupManager localBackup = new LocalBackupManager(getApplicationContext());
            boolean localSuccess = localBackup.createBackup();
            
            if (localSuccess) {
                DriveBackupManager driveBackup = new DriveBackupManager(
                        getApplicationContext(), 
                        new DriveBackupManager.OnBackupListener() {
                            @Override
                            public void onBackupSuccess() {}
                            
                            @Override
                            public void onBackupError(String error) {}
                            
                            @Override
                            public void onRestoreSuccess(int count) {}
                            
                            @Override
                            public void onRestoreError(String error) {}
                        }
                );
                
                if (driveBackup.isSignedIn()) {
                    driveBackup.backupToDrive();
                }
            }
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
