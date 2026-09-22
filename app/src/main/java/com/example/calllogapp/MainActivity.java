package com.example.calllogapp;        
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private RecyclerView recyclerView;
    private CallLogAdapter adapter;
    private List<CallLogItem> callLogList;
    private ProgressBar progressBar;
    private TextView emptyView;
    private CallLogDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        database = CallLogDatabase.getInstance(this);

        callLogList = new ArrayList<>();
        adapter = new CallLogAdapter(callLogList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        BackupScheduler.scheduleBackup(this);

        checkPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_backup_now) {
            performManualBackup();
            return true;
        } else if (id == R.id.action_restore) {
            showRestoreOptions();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performManualBackup() {
        LocalBackupManager localBackup = new LocalBackupManager(this);
        boolean success = localBackup.createBackup();
        
        if (success) {
            Toast.makeText(this, "تم إنشاء النسخة الاحتياطية بنجاح", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "فشل في إنشاء النسخة الاحتياطية", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRestoreOptions() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("showRestore", true);
        startActivity(intent);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_CALL_LOG}, 
                    PERMISSION_REQUEST_CODE);
        } else {
            syncCallLogs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                syncCallLogs();
            } else {
                Toast.makeText(this, "يجب منح إذن قراءة سجل المكالمات", Toast.LENGTH_LONG).show();
                loadFromDatabase();
            }
        }
    }

    private void syncCallLogs() {
        progressBar.setVisibility(View.VISIBLE);
        
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Cursor cursor = getContentResolver().query(
                        CallLog.Calls.CONTENT_URI,
                        null,
                        null,
                        null,
                        CallLog.Calls.DATE + " DESC"
                );

                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                        String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                        int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                        long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                        int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));

                        String callType;
                        int iconRes;
                        switch (type) {
                            case CallLog.Calls.INCOMING_TYPE:
                                callType = "واردة";
                                iconRes = R.drawable.ic_incoming;
                                break;
                            case CallLog.Calls.OUTGOING_TYPE:
                                callType = "صادرة";
                                iconRes = R.drawable.ic_outgoing;
                                break;
                            case CallLog.Calls.MISSED_TYPE:
                                callType = "فائتة";
                                iconRes = R.drawable.ic_missed;
                                break;
                            default:
                                callType = "غير معروف";
                                iconRes = R.drawable.ic_missed;
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        String dateString = sdf.format(new Date(date));
                        String durationString = formatDuration(duration);

                        CallLogEntity existingCall = database.callLogDao().getCallLogByTimestamp(date);
                        if (existingCall == null) {
                            CallLogEntity newCall = new CallLogEntity(
                                    name != null ? name : number,
                                    number,
                                    callType,
                                    iconRes,
                                    dateString,
                                    durationString,
                                    date
                            );
                            database.callLogDao().insert(newCall);
                        }
                    }
                    cursor.close();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                loadFromDatabase();
            }
        }.execute();
    }

    private void loadFromDatabase() {
        new AsyncTask<Void, Void, List<CallLogEntity>>() {
            @Override
            protected List<CallLogEntity> doInBackground(Void... voids) {
                return database.callLogDao().getAllCallLogs();
            }

            @Override
            protected void onPostExecute(List<CallLogEntity> callLogs) {
                callLogList.clear();
                
                for (CallLogEntity entity : callLogs) {
                    CallLogItem item = new CallLogItem(
                            entity.getName(),
                            entity.getNumber(),
                            entity.getCallType(),
                            entity.getIconRes(),
                            entity.getDate(),
                            entity.getDuration()
                    );
                    callLogList.add(item);
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (callLogList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("لا توجد مكالمات");
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    private String formatDuration(int duration) {
        if (duration == 0) {
            return "0 ث";
        }
        int minutes = duration / 60;
        int seconds = duration % 60;
        if (minutes > 0) {
            return String.format("%d د %d ث", minutes, seconds);
        } else {
            return String.format("%d ث", seconds);
        }
    }
}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        database = CallLogDatabase.getInstance(this);

        callLogList = new ArrayList<>();
        adapter = new CallLogAdapter(callLogList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        BackupScheduler.scheduleBackup(this);

        checkPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_backup_now) {
            performManualBackup();
            return true;
        } else if (id == R.id.action_restore) {
            showRestoreOptions();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performManualBackup() {
        LocalBackupManager localBackup = new LocalBackupManager(this);
        boolean success = localBackup.createBackup();
        
        if (success) {
            Toast.makeText(this, "تم إنشاء النسخة الاحتياطية بنجاح", Toast.LENGTH_SHORT).show();
            
            DriveBackupManager driveBackup = new DriveBackupManager(this, 
                    new DriveBackupManager.OnBackupListener() {
                        @Override
                        public void onBackupSuccess() {
                            runOnUiThread(() -> 
                                Toast.makeText(MainActivity.this, 
                                    "تم النسخ إلى Google Drive بنجاح", 
                                    Toast.LENGTH_SHORT).show()
                            );
                        }
                        
                        @Override
                        public void onBackupError(String error) {}
                        
                        @Override
                        public void onRestoreSuccess(int count) {}
                        
                        @Override
                        public void onRestoreError(String error) {}
                    });
            
            if (driveBackup.isSignedIn()) {
                driveBackup.backupToDrive();
            }
        } else {
            Toast.makeText(this, "فشل في إنشاء النسخة الاحتياطية", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRestoreOptions() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("showRestore", true);
        startActivity(intent);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_CALL_LOG}, 
                    PERMISSION_REQUEST_CODE);
        } else {
            syncCallLogs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                syncCallLogs();
            } else {
                Toast.makeText(this, "يجب منح إذن قراءة سجل المكالمات", Toast.LENGTH_LONG).show();
                loadFromDatabase();
            }
        }
    }

    private void syncCallLogs() {
        progressBar.setVisibility(View.VISIBLE);
        
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Cursor cursor = getContentResolver().query(
                        CallLog.Calls.CONTENT_URI,
                        null,
                        null,
                        null,
                        CallLog.Calls.DATE + " DESC"
                );

                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                        String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                        int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                        long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                        int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));

                        String callType;
                        int iconRes;
                        switch (type) {
                            case CallLog.Calls.INCOMING_TYPE:
                                callType = "واردة";
                                iconRes = R.drawable.ic_incoming;
                                break;
                            case CallLog.Calls.OUTGOING_TYPE:
                                callType = "صادرة";
                                iconRes = R.drawable.ic_outgoing;
                                break;
                            case CallLog.Calls.MISSED_TYPE:
                                callType = "فائتة";
                                iconRes = R.drawable.ic_missed;
                                break;
                            default:
                                callType = "غير معروف";
                                iconRes = R.drawable.ic_missed;
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        String dateString = sdf.format(new Date(date));
                        String durationString = formatDuration(duration);

                        CallLogEntity existingCall = database.callLogDao().getCallLogByTimestamp(date);
                        if (existingCall == null) {
                            CallLogEntity newCall = new CallLogEntity(
                                    name != null ? name : number,
                                    number,
                                    callType,
                                    iconRes,
                                    dateString,
                                    durationString,
                                    date
                            );
                            database.callLogDao().insert(newCall);
                        }
                    }
                    cursor.close();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                loadFromDatabase();
            }
        }.execute();
    }

    private void loadFromDatabase() {
        new AsyncTask<Void, Void, List<CallLogEntity>>() {
            @Override
            protected List<CallLogEntity> doInBackground(Void... voids) {
                return database.callLogDao().getAllCallLogs();
            }

            @Override
            protected void onPostExecute(List<CallLogEntity> callLogs) {
                callLogList.clear();
                
                for (CallLogEntity entity : callLogs) {
                    CallLogItem item = new CallLogItem(
                            entity.getName(),
                            entity.getNumber(),
                            entity.getCallType(),
                            entity.getIconRes(),
                            entity.getDate(),
                            entity.getDuration()
                    );
                    callLogList.add(item);
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (callLogList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("لا توجد مكالمات");
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    private String formatDuration(int duration) {
        if (duration == 0) {
            return "0 ث";
        }
        int minutes = duration / 60;
        int seconds = duration % 60;
        if (minutes > 0) {
            return String.format("%d د %d ث", minutes, seconds);
        } else {
            return String.format("%d ث", seconds);
        }
    }
}
