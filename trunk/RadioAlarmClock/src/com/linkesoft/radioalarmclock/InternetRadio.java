package com.linkesoft.radioalarmclock;

import java.util.*;

import android.content.*;
import android.media.*;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Funktionalität rund um das Abspielen von InternetRadio-Streams
 * 
 * @author Andreas Linke
 * 
 */
public class InternetRadio {
	// Interface für callback
	public static interface InternetRadioListener {
		public void onInternetRadioError(String error);
		public void onInternetRadioPrepareFinished();
	}

	private MediaPlayer mediaPlayer;

	private boolean wasStarted; // konnte Stream erfolgreich geladen werden? 
	private Ringtone ringtone; // standard Alarm falls Radio nicht abspielbar

	public InternetRadioListener internetRadioListener; // callback

	public String loadStreamURL(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getString("radioStreamURL", "");
	}

	public void storeStreamURL(Context context, String streamURL) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putString("radioStreamURL", streamURL);
		prefsEditor.commit();
	}

	public int loadVolume(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		int volume=prefs.getInt("radioAudioVolume", 0);
		if(volume!=0)
			return volume;
		// Lautstärke nicht gesetzt: mittlerer Wert
		AudioManager am=(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		return am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2;
	}

	public void storeVolume(Context context, int volume) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putInt("radioAudioVolume", volume);
		prefsEditor.commit();
	}

	public void start(final Context context, final boolean increaseVolume) {
		stop();
		wasStarted=false;
		final String streamURL = loadStreamURL(context);
		if (streamURL.length() == 0)
			return;
		final AudioManager am=(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		final int volume=loadVolume(context);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, volume,0);
		Log.v("Radio", "Starting radio from stream " + streamURL+" volume "+volume);
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(streamURL);
			mediaPlayer.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					if (internetRadioListener != null)
						internetRadioListener
								.onInternetRadioError("Error loading radio stream");
					else
						Log.e("Radio", "Error loading radio stream "
								+ streamURL + ": " + what);
					return false;
				}
			});
			// mediaPlayer.setOnBufferingUpdateListener(this);
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					// Buffer fertig geladen, kann gestartet werden
					mp.setVolume(1, 1); // MediaPlayer-Lautstärke maximal, Gesamtlautstärke ergibt sich durch AudioManager
					if (increaseVolume) {
						scheduleVolumeIncrease(am,volume);
					} 
					mp.start();
					wasStarted=true;
					if (internetRadioListener != null)
						internetRadioListener.onInternetRadioPrepareFinished();
				}
			});
			mediaPlayer.prepareAsync(); // siehe OnPrepared Callback oben
			
		} catch (Exception e) {
			Log.e("Radio", "Error playing " + streamURL, e);
			if (internetRadioListener != null)
				internetRadioListener.onInternetRadioError("Error loading "
						+ streamURL + ":" + e.getLocalizedMessage());
		}
	}
	
	public boolean wasStarted() {
		return wasStarted;
	}

	private void scheduleVolumeIncrease(final AudioManager am,final int targetVolume) {
		// starte mit 0 Lautstärke
		// erhöhe innerhalb 10 sec auf Ziellautstärke
		am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
		final int deltaVolume=targetVolume/10;
		final Timer t = new Timer("RadioVolume");
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				int volume=am.getStreamVolume(AudioManager.STREAM_MUSIC)+deltaVolume;
				if(deltaVolume==0)
					volume++;
				am.setStreamVolume(AudioManager.STREAM_MUSIC,volume, 0);
				if(volume>=targetVolume)
					t.cancel();
			}
		}, 0, 1000); // erhöhe jede Sekunde die Lautstärke um deltaVolume

	}

	public void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		if (ringtone != null)
			ringtone.stop();
	}
	
	public void playDefaultAlarm(final Context context)
	{
		stop();
		ringtone = RingtoneManager.getRingtone(context, 
				RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
		if(ringtone!=null) {
			ringtone.setStreamType(AudioManager.STREAM_ALARM); // mit Alarm-Lautstärke
			ringtone.play();
		}
		else
			Log.e("Radio","No ringtone available");
	}
}
