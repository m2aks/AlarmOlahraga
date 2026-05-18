package com.olahraga.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        boolean isActive = prefs.getBoolean("isAlarmActive", false);

        if (isActive) {
            int hour = prefs.getInt("alarmHour", -1);
            int minute = prefs.getInt("alarmMinute", -1);

            if (hour != -1 && minute != -1) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1);
                }

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 100, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                if (alarmManager != null) {
                    Intent showIntent = new Intent(context, MainActivity.class);
                    PendingIntent showPendingIntent = PendingIntent.getActivity(context, 0, showIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), showPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
            }
        }
    }
}
