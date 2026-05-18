package com.olahraga.alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    public static Ringtone ringtone;
    private static final String CHANNEL_ID = "AlarmOlahragaChannel";
    private static final String ACTION_STOP = "com.olahraga.alarm.ACTION_STOP";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // JIKA TOMBOL "MATIKAN" DI NOTIFIKASI DIKLIK
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
            if (notificationManager != null) {
                notificationManager.cancel(1); // Hapus notifikasi
            }
            // Kirim sinyal ke MainActivity agar tombol di layar juga ikutan reset normal
            Intent updateIntent = new Intent("com.olahraga.alarm.UPDATE_UI");
            context.sendBroadcast(updateIntent);
            return;
        }

        // JIKA JALUR ALARM BUNYI (NORMAL)
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
            
            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(context, alarmUri);
            }
            
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.setLooping(true);
                }
                if (!ringtone.isPlaying()) {
                    ringtone.play();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Bikin Channel Notifikasi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alarm Olahraga Channel", NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Intent untuk tombol "MATIKAN" di dalam notifikasi
        Intent stopIntent = new Intent(context, AlarmReceiver.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 200, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent normal kalau notifikasinya diklik iseng (buka aplikasi)
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Buat Notifikasi lengkap dengan TOMBOL AKSI (addAction)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("WAKTUNYA OLAHRAGA CUY!!!")
                .setContentText("Ayo bangun, jangan males!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true) // Supaya gak bisa di-swipe ilang sebelum dimatikan
                .setContentIntent(mainPendingIntent)
                // INI DIA TOMBOLNYA! ↓↓↓
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "MATIKAN ALARM", stopPendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }

        // Kirim sinyal UI biar layar utama tahu alarm lagi bunyi
        Intent updateIntent = new Intent("com.olahraga.alarm.UPDATE_UI");
        context.sendBroadcast(updateIntent);
    }
}
