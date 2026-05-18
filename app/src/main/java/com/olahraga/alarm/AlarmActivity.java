package com.olahraga.alarm;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AlarmActivity extends Activity {
    private static Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        TextView tvStatus = findViewById(R.id.tvAlarmStatus);
        Button btnStop = findViewById(R.id.btnSetAlarm);

        tvStatus.setText("WAKTUNYA OLAHRAGA CUY!!!\nAYO BANGUN JANGAN MALES!");
        btnStop.setText("MATIKAN ALARM");

        if (ringtone == null) {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
            ringtone = RingtoneManager.getRingtone(this, alarmUri);
        }
        
        if (ringtone != null && !ringtone.isPlaying()) {
            ringtone.play();
        }

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ringtone != null && ringtone.isPlaying()) {
                    ringtone.stop();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
}
