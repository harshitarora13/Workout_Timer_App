package com.example.workouttimerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class ActivityWorkOut extends AppCompatActivity {

    TextView txt_remaining_time, tvSets, tvCompleted;
    Button BtnPause;
    Button BtnReset, btnStart;
    private Vibrator vibrator;
    private boolean isDownloadStopped = false,TimerRunningRest=false;
    CountDownTimer countDownTimer;
    MediaPlayer mediaPlayer;
    int i = 0;
    private static final String STOP_ACTION = "com.example.workouttimerapp.ACTION_CUSTOM_ACTION";
    private static final String Start_ACTION = "com.example.workouttimerapp.ACTION_CUSTOM_ACTION1";
    boolean TimerRunning;
    ProgressBar progressBar;
    int progress, TotalSets, ReamingSets, DefaultTotalSets;
    long LOOK, look;

    private NotificationManagerCompat notificationManager;
    long TimeLeftInMillis, RestTimeLeftInMillis, TimeLeftInMillisDefault, RestTimeLeftInMillisDefault;
    String timeLeftFormatted;
    long EndTime;
    private static final String CHANNEL_ID = "MY CHANNEL";
    private static final int NOTIFICATION_ID = 100;
    private static final int REQ_INTENT = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_out);
        txt_remaining_time = findViewById(R.id.txt_remaining_time);
        tvCompleted = findViewById(R.id.tvCompleted);
        tvSets = findViewById(R.id.tvSets);
        progressBar = findViewById(R.id.pro_bar);
        BtnPause = findViewById(R.id.btnStartPause);


        IntentFilter filter = new IntentFilter(STOP_ACTION);
        registerReceiver(customActionReceiver, filter);

        IntentFilter filter1 = new IntentFilter(Start_ACTION);
        registerReceiver(customActionReceiver, filter1);

        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        BtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseTimer();
                SharedPreferences preferences = getSharedPreferences("shared1", 0);
                preferences.edit().clear().commit();
                preferences.edit().clear().commit();
                finish();
            }
        });
    }

    public void startTimer() {

        isDownloadStopped = false;
        progressBar.setMax((int) TimeLeftInMillis);
        EndTime = System.currentTimeMillis() + TimeLeftInMillis;
        countDownTimer = new CountDownTimer(TimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeLeftInMillis = millisUntilFinished;
                progressBar.setProgress((int) millisUntilFinished);
                notficationFun((int) TimeLeftInMillis, "set" + String.valueOf(DefaultTotalSets - ReamingSets + 1));
                ReamingSets = TotalSets - 1;
                int sets = DefaultTotalSets - ReamingSets;
                tvSets.setText("set " + sets);
                SaveData();
                updateCountDownText();

            }

            @Override
            public void onFinish() {
                TimerRunning = false;
                notficationFun(progress, "");

                TotalSets = ReamingSets;
                if (ReamingSets > 0) {
                    RestTimeLeftInMillis = RestTimeLeftInMillisDefault;
                    StartRestTime();
                    tvSets.setText("Rest");

                } else {
                    tvCompleted.setVisibility(View.VISIBLE);
                    BtnPause.setText("Done");
                    pauseTimer();
                    countDownTimer.cancel();
                    notficationFun(progress, "");
                }
            }
        }.start();
        TimerRunning = true;
    }

    private void StartRestTime() {

        EndTime = System.currentTimeMillis() + RestTimeLeftInMillis;
        countDownTimer = new CountDownTimer(RestTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                RestTimeLeftInMillis = millisUntilFinished;
                progress = (int) (millisUntilFinished);
                progressBar.setProgress((int) millisUntilFinished);
                tvSets.setText("Rest ");
                SaveData();
                notficationFun((int) RestTimeLeftInMillis, "Rest");

                updateCountDownTextReaming();

            }

            @Override
            public void onFinish() {
                TimerRunning = false;
                TimeLeftInMillis = TimeLeftInMillisDefault;
                startTimer();
                notficationFun(progress, "Rest");

            }
        }.start();
        TimerRunning = true;
        TimerRunningRest=true;
    }

    private void updateCountDownTextReaming() {
        int minutes = (int) (RestTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (RestTimeLeftInMillis / 1000) % 60;
        timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        txt_remaining_time.setText(timeLeftFormatted);

    }

    private void updateCountDownText() {
        int minutes = (int) (TimeLeftInMillis / 1000) / 60;
        int seconds = (int) (TimeLeftInMillis / 1000) % 60;
        timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        txt_remaining_time.setText(timeLeftFormatted);


    }

    void pauseTimer() {
        countDownTimer.cancel();
        isDownloadStopped = true;
        TimerRunning = false;

    }

    private void DataLoad() {

        SharedPreferences preferences = getSharedPreferences("shared1", MODE_PRIVATE);
        TotalSets = preferences.getInt("TotalSets", 0);
        ReamingSets = preferences.getInt("ReamingSets", -1);
        RestTimeLeftInMillis = preferences.getLong("RestTimeLeftInMillis", 0);
        RestTimeLeftInMillisDefault = preferences.getLong("RestTimeLeftInMillisDefault", 0);
        TimeLeftInMillis = preferences.getLong("TimeLeftInMillis", 0);
        DefaultTotalSets = preferences.getInt("DefaultTotalSets", 0);
        TimeLeftInMillisDefault = preferences.getLong("TimeLeftInMillisDefault", 0);
        LOOK = preferences.getLong("look", -1);

        int sets = DefaultTotalSets - ReamingSets + 1;
        tvSets.setText("Set " + sets);
        updateCountDownText();
        if (TimerRunning) {
            EndTime = preferences.getLong("endTime", 0);
            TimeLeftInMillis = EndTime - System.currentTimeMillis();
            if (TimeLeftInMillis < 0) {
                TimeLeftInMillis = 0;
                TimerRunning = false;
                updateCountDownText();
            }
            else {
                startTimer();
            }

        }
    }

    private void SaveData() {
        SharedPreferences preferences = getSharedPreferences("shared1", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("TotalSets", TotalSets);
        editor.putInt("ReamingSets", ReamingSets);
        editor.putLong("RestTimeLeftInMillis", RestTimeLeftInMillis);
        editor.putLong("TimeLeftInMillis", TimeLeftInMillis);
        editor.putBoolean("timerRunning", TimerRunning);
        editor.putBoolean("timerRunningRest", TimerRunningRest);
        editor.putLong("endTime", EndTime);
        editor.apply();
    }

    @Override
    protected void onStart() {

        super.onStart();
        DataLoad();
        startTimer();
    }

    @Override
    protected void onPause() {

        super.onPause();
        SaveData();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void notficationFun(int progress, String message) {

        NotificationManager mNotificationManager;
        final int progressMax = progress / 1000;

        if (progressMax == 3) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(100);
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
            mediaPlayer.start();
        }
        Intent customActionIntent = new Intent(STOP_ACTION);
        PendingIntent customActionPendingIntent = PendingIntent.getBroadcast(this, 0, customActionIntent, PendingIntent.FLAG_MUTABLE);

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.download)
                .setContentTitle(timeLeftFormatted)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .addAction(R.drawable.download, "Stop", customActionPendingIntent)
                .addAction(R.drawable.download, "cancel", customActionPendingIntent)
                .setOnlyAlertOnce(true)
                .setProgress(progressMax, 0, true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            return;
        }

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            notification.setChannelId(channelId);
        }
        notificationManager.notify(2, notification.build());

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                for (int progress = 0; progress <= progressMax; progress += 1) {
                    if (isDownloadStopped) {
                        notification.setProgress(progressMax, 0, false)
                                .setContentText("Timer stopped")
                                .setOngoing(false);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions

                            return;
                        }
                        notificationManager.notify(2, notification.build());
                        return;
                    }
                    notification.setProgress(progressMax, progress, false);
                    notification.setContentTitle(timeLeftFormatted);

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions

                        return;
                    }

                    notificationManager.notify(2, notification.build());
                    SystemClock.sleep(1000);
                }
                notification.setContentText("Counter finished")
                        .setProgress(0, 0, false)
                        .setOngoing(false);

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    return;
                }
                notificationManager.notify(2, notification.build());
            }

        }).start();


    }

    private BroadcastReceiver customActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(STOP_ACTION)) {
                pauseTimer();
                pauseTimer();
                NotificationManagerCompat.from(getApplicationContext()).cancelAll();
                NotificationManagerCompat.from(getApplicationContext()).cancelAll();
                SharedPreferences preferences = getSharedPreferences("shared1", 0);
                preferences.edit().clear().commit();
                finish();
            }
            if (intent.getAction().equals(Start_ACTION)) {
                startTimer();
            }

        }
    };

}
