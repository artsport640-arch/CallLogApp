package com.example.calllogapp;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class BackupScheduler {
    
    private static final String BACKUP_WORK_NAME = "call_log_backup";
    
    public static void scheduleBackup(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        PeriodicWorkRequest backupWork = new PeriodicWorkRequest.Builder(
                BackupWorker.class,
                1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                BACKUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                backupWork
        );
    }
    
    public static void cancelBackup(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(BACKUP_WORK_NAME);
    }
}
