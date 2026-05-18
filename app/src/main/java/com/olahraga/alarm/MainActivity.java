package com.olahraga.alarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView tvAlarmStatus;
    private Button btnSetAlarm;
    private SharedPreferences prefs;

    private BroadcastReceiver uiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateButtonState();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAlarmStatus = findViewById(R.id.tvAlarmStatus);
        btnSetAlarm = findViewById(R.id.btnSetAlarm);
        prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);

        checkNotificationPermission();
        syncAlarmOnStartup(); 
        updateButtonState();

        btnSetAlarm.setOnClickListener(new View.OnClickListener () {
            @Override
            public void onClick(View v){
                if (AlarmReceiver.ringtone != null && AlarmReceiver.ringtone.isPlaying()) {
                    AlarmReceiver.ringtone.stop();

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancel(1);
                    }

                    updateButtonState();
                    Toast.makeText(MainActivity.this, "Alarm Berhasil Dimatikan!", Toast.LENGTH_SHORT).show();
                } else {
                    Calendar currentTime = Calendar.getInstance();
                    int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = currentTime.get(Calendar.MINUTE);
                                                                            TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,                                       new TimePickerDialog.OnTimeSetListener () {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {                                                 setDailyAlarm(hourOfDay, minute);
                        }
                      }, hour, minute, true);
                    timePickerDialog.show();
                }
            }
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void setDailyAlarm(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();             calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            Intent showIntent = new Intent(this, MainActivity.class);
            PendingIntent showPendingIntent = PendingIntent.getActivity(this, 0, showIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), showPendingIntent);
            alarmManager.setAlarmClock(clockInfo, pendingIntent);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("alarmHour", hour);
        editor.putInt("alarmMinute", minute);
        editor.putBoolean("isAlarmActive", true);
        editor.apply();

        updateButtonState();
        Toast.makeText(this, "Alarm Berhasil Diaktifkan & Dikunci Sistem!", Toast.LENGTH_SHORT).show();
    }

    private void syncAlarmOnStartup() {
       
        boolean isActive = prefs.getBoolean("isAlarmActive", false);
        if (isActive) {
            Intent bootIntent = new Intent(this, BootReceiver.class);
            sendBroadcast(bootIntent);
        }
    }
                                                            private void updateButtonState() {
        if (AlarmReceiver.ringtone != null && AlarmReceiver.ringtone.isPlaying()) {
            tvAlarmStatus.setText("WAKTUNYA OLAHRAGA CUY!!!\nAYO BANGUN JANGAN MALES!");
            btnSetAlarm.setText("MATIKAN ALARM");
        } else {
            boolean isActive = prefs.getBoolean("isAlarmActive", false);
            if (isActive) {
                int hour = prefs.getInt("alarmHour", 0);
                int minute = prefs.getInt("alarmMinute", 0);
                tvAlarmStatus.setText(String.format("Alarm Telah Aktif : %02d:%02d", hour, minute));
                btnSetAlarm.setText("Atur Jam Nya Dulu Mas");
            } else {
                tvAlarmStatus.setText("Belum ada alarm yang aktif");
                btnSetAlarm.setText("Atur Jam Nya Dulu Mas");
            }
        }                                                   }                                                   
    @Override                                               protected void onStart() {
        super.onStart();                                        registerReceiver(uiReceiver, new IntentFilter("com.olahraga.alarm.UPDATE_UI"), Context.RECEIVER_EXPORTED);                                                          }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(uiReceiver);                     } catch (Exception e) {
            e.printStackTrace();
        }                                                   }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonState();                                }
}
