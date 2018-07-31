package com.yitong.servicedemo;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Create by Daniel Zhang on 2018/7/8
 * ==========================================================================
 * 手动调用bindService()后，自动调用内部方法：{onCreate()、onBind()}
 * 手动调用unbindService()后，自动调用内部方法：{onUnbind()、onDestory()}
 * =========================================================================
 * 手动调用startService()后，自动调用内部方法：{onCreate()、onStartCommand()}
 * 手动调用stopService()后，自动调用内部方法：{onDestory()}
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer;
    private Timer timer;
    private String status = "stop";//播放状态

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("TAGG", "onCreate");
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("TAGG", "onBind");
        return new MyBind();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TAGG", "onStartCommand");
        switch (intent.getIntExtra("type", -1)) {

            //播放音乐
            case MainActivity.PLAY_MUSIC:
                if (status.equals("stop")) {
                    mediaPlayer.reset();
                    mediaPlayer = MediaPlayer.create(this, R.raw.birds);
                    mediaPlayer.start();
                    mediaPlayer.setLooping(false);//是否重复播放

                    //定时器 实时发送播放进度
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Intent inten = new Intent();
                            inten.putExtra("isStop", false);
                            inten.putExtra("total", mediaPlayer.getDuration());
                            inten.putExtra("current", mediaPlayer.getCurrentPosition());
                            inten.setAction("com.complete");
                            sendBroadcast(inten);
                        }
                    }, 0, 100);

                    //播放完成监听
                    mediaPlayer.setOnCompletionListener(this);

                } else if (status.equals("pause")) {
                    mediaPlayer.start();
                    //定时器 实时发送播放进度

                }

                break;

            //暂停音乐
            case MainActivity.PAUSE_MUSIC:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    status = "pause";
                }
                break;

            //停止音乐
            case MainActivity.STOP_MUSIC:
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    timer.cancel();
                    status = "stop";
                }
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer.purge();
        Log.e("TAGG", "onDestroy");
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.stop();
        status = "stop";
        timer.cancel();
        Log.e("TAGG", "发送结束信号");
        Intent intent = new Intent();
        intent.putExtra("isStop", true);
        intent.setAction("com.complete");
        sendBroadcast(intent);
    }

    class MyBind extends Binder {
        void toastInfo(String msg) {
            Log.e("TAGG", msg);
        }
    }

}
