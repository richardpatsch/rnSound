package com.rnsound;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.drm.DrmStore;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;

import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
//import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by iDontCare on 15.12.2017.
 */

public class BackgroundSoundService extends MediaBrowserServiceCompat implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {
    public static final String COMMAND_EXAMPLE = "command_example";
    public static final String COMMAND_NEW_PLAYLIST = "command_new_playlist";
    public static final String COMMAND_CHANGE_PLAYLIST_INDEX = "command_change_playlist_index";
    public static final String COMMAND_SOLO_PLAY = "command_solo_play";
    public static final String COMMAND_PAUSE = "command_pause";
    public static final String COMMAND_RESUME = "command_resume";

    public final String TAG = "BackgroundSoundService";

    private MediaPlayer mMediaPlayer;
    private MediaSessionCompat mMediaSessionCompat;
    private ArrayList<Audio> playlist =  new ArrayList<>();
    private Audio currentAudio;
    private int CURRENT_STATE;

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
                mMediaPlayer.pause();
            }
        }
    };

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();
            if (!successfullyRetrievedAudioFocus()) {
                return;
            }

            mMediaSessionCompat.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            updatePlayingNotification();
            mMediaPlayer.start();

        }

        @Override
        public void onPause() {
            super.onPause();

            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                updatePlayingNotification();
            }
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "next");
            next();
        }

        public void onSkipToPrevious() {
            Log.d(TAG, "last");
            previous();
        }

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
            Log.d("JKLSJDLKFJLDKJFLK", "onCommand");

            if (COMMAND_EXAMPLE.equalsIgnoreCase(command)) {

            } else if (COMMAND_NEW_PLAYLIST.equalsIgnoreCase(command)){
                Log.d("JKLSJDLKFJLDKJFLK", "EAT SHIT BOB");
                ArrayList<Audio> newPlayList = (ArrayList<Audio>)extras.getSerializable("playList");
                handleNewPlaylist(newPlayList);
            } else if (COMMAND_CHANGE_PLAYLIST_INDEX.equalsIgnoreCase(command)) {
                int newID = extras.getInt("newID");
                switchToIndexOfPlaylist(newID);
            } else if (COMMAND_SOLO_PLAY.equalsIgnoreCase(command)) {
                Audio newItem = (Audio)extras.getSerializable("soloAudio");
                if (newItem != null) {
                    play(newItem);
                }
            } else if (COMMAND_PAUSE.equalsIgnoreCase(command)) {
                mMediaPlayer.pause();
            } else if (COMMAND_RESUME.equalsIgnoreCase(command)) {
                mMediaPlayer.start();
            }
        }



        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }
    };

    private void handleNewPlaylist(ArrayList<Audio> newPlaylist) {
        Log.d("KJLKJKL", "GOT NEW PLAYLIST");
        this.playlist = newPlaylist;

        if (this.playlist.size() > 0 ) {
            this.currentAudio = this.playlist.get(0);
            play(this.currentAudio); //play first title
            Log.d(TAG, "play new list (index0)");
        }
    }

    private void play(Audio song) {
        this.currentAudio = song;
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(song.getDataSource());

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Prepare async for..." + song.getTitle());
        mMediaPlayer.prepareAsync();
    }

    private void next() {
        if (this.playlist != null && this.playlist.size() > 0) {
            int currentIndex = this.playlist.indexOf(this.currentAudio);
            Log.d(TAG, String.valueOf(currentIndex));
            if (currentIndex < this.playlist.size()-1) {
                Log.d(TAG, "set ca (next) to " + this.currentAudio.getTitle());
                this.currentAudio = this.playlist.get(currentIndex+1);
                play(this.currentAudio);
            }
        }
    }

    private void previous() {
        if (this.playlist != null && this.playlist.size() > 0) {
            int currentIndex = this.playlist.indexOf(this.currentAudio);
            Log.d(TAG, String.valueOf(currentIndex));
            Log.d(TAG, "pre if (prev)");
            Log.d(TAG, String.valueOf(this.playlist.size()));
            if (currentIndex > 0) {
                Log.d(TAG, "set ca (previous) to " + this.currentAudio.getTitle());
                this.currentAudio = this.playlist.get(currentIndex-1);
                play(this.currentAudio);
            }
        }
    }

    private void switchToIndexOfPlaylist(int newIndex) {
        if (newIndex <= this.playlist.size()-1) {
            this.currentAudio = this.playlist.get(newIndex);
            play(this.currentAudio);
        } else {
            Log.d(TAG, "Playlist index is ouf range");
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "start playing: " + this.currentAudio.getTitle());

        mMediaSessionCompat.setActive(true);
        initMediaSessionMetadata(this.currentAudio);
        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
        updatePlayingNotification();

        mMediaPlayer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();
        initMediaSessionMetadata(new Audio("emtpy", "null", "null"));
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        AudioAttributes attr = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        mMediaPlayer.setAudioAttributes(attr);

        mMediaPlayer.setVolume(1.0f, 1.0f);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        this.CURRENT_STATE = PlaybackStateCompat.STATE_PAUSED;
    }

    private void updatePlayingNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(BackgroundSoundService.this, mMediaSessionCompat);
        if( builder == null ) {
            return;
        }

        if (this.playlist.size() > 1) {
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous, "Previous", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        }

        if (this.CURRENT_STATE ==  PlaybackStateCompat.STATE_PAUSED) {
            Log.d("asdf", String.valueOf(this.CURRENT_STATE));
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        } else if (this.CURRENT_STATE == PlaybackStateCompat.STATE_PLAYING){
            Log.d("asdf", String.valueOf(this.CURRENT_STATE));
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        }

        if (this.playlist.size() > 1) {
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
        }

        if (this.playlist.size() > 1) {
            builder.setStyle(new android.support.v7.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2).setMediaSession(mMediaSessionCompat.getSessionToken()));
        } else {
            builder.setStyle(new android.support.v7.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
        }
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationManagerCompat.from(BackgroundSoundService.this).notify(1, builder.build());
    }

    private void initMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(getApplication(), MediaButtonReceiver.class);
        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mMediaSessionCompat.setCallback(mMediaSessionCallback);
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        setSessionToken(mMediaSessionCompat.getSessionToken());
    }

    private void setMediaPlaybackState(int state) {
        Log.d("asdf", "CALL setMediaPlaybackState");
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();

        if( state == PlaybackStateCompat.STATE_PLAYING ) {
            Log.d("asdf", "set to playing");

            this.CURRENT_STATE = PlaybackStateCompat.STATE_PLAYING;
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        } else {
            Log.d("asdf", "set to paused");

            this.CURRENT_STATE = PlaybackStateCompat.STATE_PAUSED;
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    private void initMediaSessionMetadata(Audio track) {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, track.getTitle());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, track.getSubtitle());
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1);

        mMediaSessionCompat.setMetadata(metadataBuilder.build());
    }

    private void initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
    }

    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        //MediaBrowserCompat.MediaItem mi = new MediaBrowserCompat.MediaItem()
        result.sendResult(mediaItems);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch( focusChange ) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if( mMediaPlayer.isPlaying() ) {
                    mMediaPlayer.stop();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                mMediaPlayer.pause();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if( mMediaPlayer != null ) {
                    mMediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if( mMediaPlayer != null ) {
                    if( !mMediaPlayer.isPlaying() ) {
                        mMediaPlayer.start();
                    }
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mMediaPlayer != null) {
            Log.d(TAG, "complete");

            int currentIndex = this.playlist.indexOf(this.currentAudio);

            if (currentIndex > -1 && currentIndex < this.playlist.size()-1) {
                this.currentAudio = this.playlist.get(currentIndex +1);
                play(this.currentAudio);
            } else if (currentIndex == this.playlist.size()-1) { //last record of playlist => start again at 0
                this.currentAudio = this.playlist.get(0);
                play(this.currentAudio);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
