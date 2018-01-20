package com.rnsound;

import android.widget.Toast;


import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import android.content.ComponentName;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;

public class SoundModule extends ReactContextBaseJavaModule {
    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";

    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    private ArrayList<Audio> playList = new ArrayList<>();

    private int mCurrentState;

    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;

    private ReactApplicationContext context;
    private Class<?> clsActivity;


    public SoundModule(ReactApplicationContext reactContext) {
        super(reactContext);

        this.context = reactContext;
    }


    public ReactApplicationContext getReactApplicationContextModule() {
        return this.context;
    }

    public Class<?> getClassActivity() {
        if (this.clsActivity == null) {
            this.clsActivity = getCurrentActivity().getClass();
        }
        return this.clsActivity;
    }

    @Override
    public String getName() {
        return "SoundExample";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
        constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
        return constants;
    }

    @ReactMethod
    public void play(ReadableMap jsonObj) { //pass as json object
        //Toast.makeText(getReactApplicationContext(), message, duration).show();
        Log.d("asdfasdf", jsonObj.getString("url"));
        Log.d("asasdf", jsonObj.toString());
        Bundle x = new Bundle();
        x.putSerializable("soloAudio", new Audio(jsonObj.getString("url"), jsonObj.getString("title"), jsonObj.getString("subtitle")));
        mMediaControllerCompat.sendCommand(BackgroundSoundService.COMMAND_SOLO_PLAY, x, null);
        Toast.makeText(getReactApplicationContext(), "sent", Toast.LENGTH_LONG).show();
    }

    @ReactMethod
    public void pause() {
        mMediaControllerCompat.sendCommand(BackgroundSoundService.COMMAND_PAUSE, null, null);
    }

    @ReactMethod
    public void resume() {
        mMediaControllerCompat.sendCommand(BackgroundSoundService.COMMAND_RESUME, null, null);
    }

    @ReactMethod
    public void setPlaylist(ReadableArray jsonArray) {
        Log.d("asdf", jsonArray.toString());
        int size = jsonArray.size();
        ArrayList<Audio> playlist = new ArrayList<>();

        for (int i = 0; i < size; i++) { //every iteration an audio Element
            ReadableMap rm = jsonArray.getMap(i);
            Log.d("asdf", rm.toString());
            playlist.add(new Audio(rm.getString("url"), rm.getString("title"), rm.getString("subtitle")));
        }

        Bundle x = new Bundle();
        x.putSerializable("playList", playlist);
        x.putSerializable("currentItem", playlist.get(0));

        mMediaControllerCompat.sendCommand(BackgroundSoundService.COMMAND_NEW_PLAYLIST, x, null);
    }

    @ReactMethod
    public void startService() {
        mMediaBrowserCompat = new MediaBrowserCompat(this.context , new ComponentName(this.context, BackgroundSoundService.class),
                mMediaBrowserCompatConnectionCallback,  null);
        mMediaBrowserCompat.connect();
    }

    @ReactMethod
    public void stopService() {
        mMediaBrowserCompat.disconnect();
    }

    @ReactMethod
    public void setPlaylistIndex(int newIndex) {
        Bundle x = new Bundle();
        x.putInt("newID", newIndex);
        mMediaControllerCompat.sendCommand(BackgroundSoundService.COMMAND_CHANGE_PLAYLIST_INDEX, x, null);
    }

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mMediaControllerCompat = new MediaControllerCompat(context, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                MediaControllerCompat.setMediaController(getCurrentActivity(), mMediaControllerCompat);
                Log.d("asdf", "CONNECTED!");

            } catch( RemoteException e ) {
                Log.d("asdf", e.getMessage());
            }
        }
    };

    private MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if( state == null ) {
                return;
            }

            switch( state.getState() ) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    mCurrentState = STATE_PLAYING;
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    mCurrentState = STATE_PAUSED;
                    break;
                }
            }
        }
    };
}
