package com.yitong.servicedemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PLAY_MUSIC = 0;
    public static final int STOP_MUSIC = 1;
    public static final int PAUSE_MUSIC = 2;

    private Button play;
    private Button stop;
    private MyBroadCastReceiver receiver;
    private ServiceConnection mServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        play = findViewById(R.id.play);
        stop = findViewById(R.id.stop);
        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        initServiceConnect();

        //注册广播
        receiver = new MyBroadCastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.complete");
        registerReceiver(receiver, filter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:
                if (play.getText().toString().equals("播放")) {
                    playingmusic(PLAY_MUSIC);
                    play.setText("暂停");
                } else {
                    playingmusic(PAUSE_MUSIC);
                    play.setText("播放");
                }
                break;
            case R.id.stop:
                playingmusic(STOP_MUSIC);
                play.setText("播放");
                break;
        }
    }

    //初始化服务连接
    private void initServiceConnect() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MyBind bind = (MusicService.MyBind) service;
                bind.toastInfo("调用service方法");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    //启动服务
    private void playingmusic(int type) {
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("type", type);
        startService(intent);
    }

    //绑定服务
    private void bindService() {
        bindService(new Intent(this, MusicService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    //解绑服务
    private void unbindService() {
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //广播接收停止信号
    public class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("isStop", false)) {
                Toast.makeText(MainActivity.this, "播放结束", Toast.LENGTH_SHORT).show();
                play.setText("播放");
            } else {
                int total = intent.getIntExtra("total", 0);
                int current = intent.getIntExtra("current", 0);
                Log.e("TAGG", current + "/" + total);
            }
        }
    }

}
