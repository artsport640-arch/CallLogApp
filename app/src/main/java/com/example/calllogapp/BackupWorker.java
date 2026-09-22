package com.example.calllogapp;

import android.content.Context;

import androidx.annotation.NonNu1l;
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
            LocalBackupManager localBackup = new LocalBackupManager(getApplicationContext());
            localBackup.createBackup();
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}                            @Override
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
