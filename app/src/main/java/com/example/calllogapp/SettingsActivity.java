package com.example.calllogapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch hideSwitch;
    private TextView statusText;
    private Button restoreButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        hideSwitch = findViewById(R.id.hideSwitch);
        statusText = findViewById(R.id.statusText);
        restoreButton = findViewById(R.id.restoreButton);
        Button backButton = findViewById(R.id.backButton);
        Button driveButton = findViewById(R.id.driveButton);
        TextView driveStatusText = findViewById(R.id.driveStatusText);

        driveButton.setVisibility(View.GONE);
        driveStatusText.setVisibility(View.GONE);

        prefs = getSharedPreferences("CallLogPrefs", Context.MODE_PRIVATE);
        boolean isHidden = prefs.getBoolean("isHidden", false);

        hideSwitch.setChecked(isHidden);
        updateStatusText(isHidden);

        hideSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                hideApp();
            } else {
                showApp();
            }
        });

        restoreButton.setOnClickListener(v -> {
            showRestoreOptions();
        });

        backButton.setOnClickListener(v -> finish());

        if (getIntent().getBooleanExtra("showRestore", false)) {
            showRestoreOptions();
        }
    }

    private void showRestoreOptions() {
        new AlertDialog.Builder(this)
                .setTitle("استعادة النسخة الاحتياطية")
                .setMessage("سيتم استبدال جميع البيانات الحالية بالبيانات من النسخة الاحتياطية. هل أنت متأكد؟")
                .setPositiveButton("استعادة", (dialog, which) -> {
                    LocalBackupManager localBackup = new LocalBackupManager(this);
                    boolean success = localBackup.restoreFromBackup();
                    
                    if (success) {
                        Toast.makeText(this, "تمت الاستعادة بنجاح", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "فشل في الاستعادة", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void hideApp() {
        PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isHidden", true);
        editor.apply();

        updateStatusText(true);
        Toast.makeText(this, "تم إخفاء التطبيق", Toast.LENGTH_LONG).show();
    }

    private void showApp() {
        PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isHidden", false);
        editor.apply();

        updateStatusText(false);
        Toast.makeText(this, "تم إظهار التطبيق", Toast.LENGTH_SHORT).show();
    }

    private void updateStatusText(boolean isHidden) {
        if (isHidden) {
            statusText.setText("التطبيق مخفي");
            statusText.setTextColor(0xFFE53935);
        } else {
            statusText.setText("التطبيق ظاهر");
            statusText.setTextColor(0xFF43A047);
        }
    }
}
