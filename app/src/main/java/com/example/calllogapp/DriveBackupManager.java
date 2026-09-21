package com.example.calllogapp;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;

public class DriveBackupManager {
    
    private static final String BACKUP_FILE_NAME = "call_log_backup.json";
    private static final String APP_FOLDER_NAME = "CallLogApp_Backups";
    
    private Context context;
    private Drive driveService;
    private OnBackupListener listener;
    
    public interface OnBackupListener {
        void onBackupSuccess();
        void onBackupError(String error);
        void onRestoreSuccess(int count);
        void onRestoreError(String error);
    }
    
    public DriveBackupManager(Context context, OnBackupListener listener) {
        this.context = context;
        this.listener = listener;
        initializeDriveService();
    }
    
    private void initializeDriveService() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account == null) {
            return;
        }
        
        GoogleAccountCredential credential = GoogleAccountCredential
                .usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());
        
        driveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("CallLogApp")
                .build();
    }
    
    public boolean isSignedIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        return account != null;
    }
    
    public void backupToDrive() {
        if (!isSignedIn()) {
            if (listener != null) {
                listener.onBackupError("يجب تسجيل الدخول بحساب Google أولاً");
            }
            return;
        }
        
        new AsyncTask<Void, Void, Boolean>() {
            String errorMessage = null;
            
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    LocalBackupManager localBackup = new LocalBackupManager(context);
                    if (!localBackup.createBackup()) {
                        errorMessage = "فشل في إنشاء النسخة الاحتياطية المحلية";
                        return false;
                    }
                    
                    String folderId = getOrCreateAppFolder();
                    String fileId = getBackupFileId(folderId);
                    
                    java.io.File backupFile = localBackup.getBackupFile();
                    FileContent mediaContent = new FileContent("application/json", backupFile);
                    
                    if (fileId != null) {
                        File fileMetadata = new File();
                        fileMetadata.setName(BACKUP_FILE_NAME);
                        driveService.files().update(fileId, fileMetadata, mediaContent).execute();
                    } else {
                        File fileMetadata = new File();
                        fileMetadata.setName(BACKUP_FILE_NAME);
                        fileMetadata.setParents(Collections.singletonList(folderId));
                        driveService.files().create(fileMetadata, mediaContent).execute();
                    }
                    
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    errorMessage = e.getMessage();
                    return false;
                }
            }
            
            @Override
            protected void onPostExecute(Boolean success) {
                if (listener != null) {
                    if (success) {
                        listener.onBackupSuccess();
                    } else {
                        listener.onBackupError(errorMessage != null ? errorMessage : "حدث خطأ غير معروف");
                    }
                }
            }
        }.execute();
    }
    
    public void restoreFromDrive() {
        if (!isSignedIn()) {
            if (listener != null) {
                listener.onRestoreError("يجب تسجيل الدخول بحساب Google أولاً");
            }
            return;
        }
        
        new AsyncTask<Void, Void, Integer>() {
            String errorMessage = null;
            
            @Override
            protected Integer doInBackground(Void... voids) {
                try {
                    String folderId = getOrCreateAppFolder();
                    String fileId = getBackupFileId(folderId);
                    
                    if (fileId == null) {
                        errorMessage = "لا يوجد نسخ احتياطي على Google Drive";
                        return -1;
                    }
                    
                    java.io.File localFile = new java.io.File(context.getFilesDir(), "temp_restore.json");
                    OutputStream outputStream = new FileOutputStream(localFile);
                    driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                    outputStream.close();
                    
                    LocalBackupManager localBackup = new LocalBackupManager(context);
                    BackupData backupData = localBackup.loadBackup();
                    
                    if (backupData == null || backupData.getCallLogs() == null) {
                        errorMessage = "فشل في قراءة ملف النسخ الاحتياطي";
                        return -1;
                    }
                    
                    CallLogDatabase database = CallLogDatabase.getInstance(context);
                    database.callLogDao().deleteAll();
                    
                    for (CallLogEntity entity : backupData.getCallLogs()) {
                        database.callLogDao().insert(entity);
                    }
                    
                    localFile.delete();
                    
                    return backupData.getCallLogs().size();
                } catch (Exception e) {
                    e.printStackTrace();
                    errorMessage = e.getMessage();
                    return -1;
                }
            }
            
            @Override
            protected void onPostExecute(Integer count) {
                if (listener != null) {
                    if (count >= 0) {
                        listener.onRestoreSuccess(count);
                    } else {
                        listener.onRestoreError(errorMessage != null ? errorMessage : "حدث خطأ غير معروف");
                    }
                }
            }
        }.execute();
    }
    
    private String getOrCreateAppFolder() throws Exception {
        FileList result = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and name='" + APP_FOLDER_NAME + "' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();
        
        if (result.getFiles().size() > 0) {
            return result.getFiles().get(0).getId();
        }
        
        File fileMetadata = new File();
        fileMetadata.setName(APP_FOLDER_NAME);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        
        File folder = driveService.files().create(fileMetadata)
                .setFields("id")
                .execute();
        
        return folder.getId();
    }
    
    private String getBackupFileId(String folderId) throws Exception {
        FileList result = driveService.files().list()
                .setQ("'" + folderId + "' in parents and name='" + BACKUP_FILE_NAME + "' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();
        
        if (result.getFiles().size() > 0) {
            return result.getFiles().get(0).getId();
        }
        
        return null;
    }
}
