package com.example.clipboardmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ClipBoardService extends Service implements ClipboardManager.OnPrimaryClipChangedListener {
    private static final String ENABLE_SERVICE = "ENABLE_SERVICE";
    private static final String DISABLE_SERVICE = "DISABLE_SERVICE";
    private static final String TAG = "CBM:ClipBoardService";
    private ClipboardManager clipboard;
    private String channelId = "bed076a8-3500-460a-8af6-dde57687e4ea";
    private String channelName = "clipboard-service-manager";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if(ENABLE_SERVICE.equals(action)){
            enableClipBoardListener();
        }else if(DISABLE_SERVICE.equals(action)){
            disableClipBoardListner();
        }else{
            startForeground(1, makeNotification());
            initializeClipBoard();
        }
        return START_STICKY;
    }

    private void initializeClipBoard() {
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        enableClipBoardListener();
    }

    private void enableClipBoardListener() {
        clipboard.addPrimaryClipChangedListener(this);
    }

    private void disableClipBoardListner() {
        clipboard.removePrimaryClipChangedListener(this);
    }

    private Notification makeNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle("Clipboard Manager is running in background");
        bigTextStyle.bigText("You can hide this notification via settings.");
        // Set big text style.
        builder.setStyle(bigTextStyle);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        Bitmap largeIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clipboard);
        builder.setLargeIcon(largeIconBitmap);

        // Add Play button intent in notification.
        Intent playIntent = new Intent(this, ClipBoardService.class);
        playIntent.setAction(ENABLE_SERVICE);
        PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);
        NotificationCompat.Action playAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "Enable", pendingPlayIntent);
        builder.addAction(playAction);

        // Add Pause button intent in notification.
        Intent pauseIntent = new Intent(this, ClipBoardService.class);
        pauseIntent.setAction(DISABLE_SERVICE);
        PendingIntent pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0);
        NotificationCompat.Action prevAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Disable", pendingPrevIntent);
        builder.addAction(prevAction);
        makeNotificationChannel(channelId, channelName);
        return builder.build();
    }

    private void makeNotificationChannel(String channelId, String channelName) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            service.createNotificationChannel(chan);
        }
    }

    @Override
    public void onPrimaryClipChanged() {
        if (clipboard.getPrimaryClip() != null && clipboard.getPrimaryClip().getItemCount() > 0) {
            String clipboardText = String.valueOf(clipboard.getPrimaryClip().getItemAt(0).getText());
            Log.d(TAG, clipboardText);
        }
    }
}