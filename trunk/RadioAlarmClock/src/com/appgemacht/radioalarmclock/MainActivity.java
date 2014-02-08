package com.appgemacht.radioalarmclock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.appgemacht.radioalarmclock.InternetRadio.InternetRadioListener;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.appgemacht.radioalarmclock.InternetRadio.InternetRadioListener;
import com.appgemacht.radioalarmclock.Alarms;
import com.appgemacht.radioalarmclock.R;
import com.appgemacht.radioalarmclock.InternetRadio;
import com.appgemacht.radioalarmclock.Alarm;
import com.appgemacht.radioalarmclock.MainActivity;
//import com.appgemacht.radioalarmclock.AlarmsActivity;
import com.appgemacht.radioalarmclock.InternetRadioActivity;

public class MainActivity extends Activity implements InternetRadioListener {

	
	public static final String ALARM_UP = "ALARM_UP"; // vom AlarmReceiver:
	                                                  // Alarm geht los
	
	private TextView clockView;
	private Button stopAlarmButton;
	private MenuItem alarmMenu;

	private Timer timer; // zum Update der Uhrzeit
	private static PowerManager.WakeLock wakeLock;

	private Alarms alarms;
	private InternetRadio radio = new InternetRadio();
	private int originalVolume;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.clock);//activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // kein Statusbar ganz oben
		alarms = new Alarms(this);
		clockView = (TextView) findViewById(R.id.clockTextView);
		stopAlarmButton = (Button) findViewById(R.id.stopAlarmButton);
		stopAlarmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopAlarm();
			}
		});

		radio.internetRadioListener = this;		
	}

	// Hilfsfunktion um Text in einem TextView so groß wie möglich darzustellen
	// Im Layout ggf. android:singleLine="true" 
	// und android:includeFontPadding="false" setzen
	private void maximize(TextView textView) {
		// gehe von maximaler Höhe aus und verkleinere bis nicht mehr abgeschnitten wird
		// Achtung: textView.getTextSize() gibt Pixel zurück
		// während textView.setTextSize() dips erwartet
		final CharSequence text = textView.getText();
		final float scale = getResources().getDisplayMetrics().density;
		final float width=textView.getWidth()-textView.getPaddingLeft()-textView.getPaddingRight();
		final float height=textView.getHeight()-textView.getCompoundPaddingTop()-textView.getCompoundPaddingBottom();
		for (float textSize = height/scale; textSize > 10; textSize *= 0.95) {
			textView.setTextSize(textSize); // dip
			if (text.equals(TextUtils.ellipsize(text, textView.getPaint(),
					width, TextUtils.TruncateAt.END))) // Text passt 
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		AudioManager audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
		originalVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		// Timer zur Aktualisierung der Uhrzeit
		timer = new Timer(getString(R.string.app_name));
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				// TimerTask läuft in einem Background-Thread, 
				// UI-Änderungen (z.B. Text setzen)
				// dürfen aber nur vom UI-Thread aus gemacht werden
				runOnUiThread(new Runnable() {
					public void run() {
						updateClock();
					}
				});
			}
		}, 0, 1000); // wiederhole alle 1000 ms
		
		// Warnung falls keine Radio-URL vorhanden
		if(radio.loadStreamURL(this).length()==0)
			Toast.makeText(this, R.string.NoRadioStream, Toast.LENGTH_LONG).show();

		// Intent wird vom BroadcastReceiver gesetzt, wenn ein Alarm fällig ist
		if (getIntent().getBooleanExtra(ALARM_UP, false)) {
			getIntent().removeExtra(ALARM_UP); // vermeide Mehrfach-Alarm
			onAlarmUp();
		}
	}
	
	@Override
	protected void onPause() {
		stopAlarm();
		timer.cancel();		
		AudioManager audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
		super.onPause();
	}
	
	private void onAlarmUp() {
		Log.v("Alarm","onAlarmUp");
		accquireWakeLock(this);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED						
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		flashClock();
		// starte Radio mit geringer Verzögerung, 
		// damit das System aufwachen und eine Internet-Verbindung herstellen kann 
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				alarms.scheduleNextAlarm(MainActivity.this);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						radio.start(MainActivity.this, true);
						stopAlarmButton.setVisibility(View.VISIBLE);
					}
				});
			}
		}, 1000);
		// Notfall-Fallback, falls Radio nicht abgespielt werden kann
		// (z.B. keine Internet-Verbindung)
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(!radio.wasStarted()) {
							radio.playDefaultAlarm(MainActivity.this);
							stopAlarmButton.setVisibility(View.VISIBLE);
						}
					}
				});
			}
		}, 20000);
	}

	// kleine Animation zum Blinken der Uhrzeit
	private void flashClock() {
		Animation anim = new AlphaAnimation(0.0f, 1.0f);
		anim.setDuration(200); // in ms
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(30);
		clockView.startAnimation(anim);
	}

	private void stopAlarm() {
		radio.stop();
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
		}
		wakeLock = null;
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Bildschirm soll nicht mehr anbleiben
		stopAlarmButton.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.alarm)
		{
			showSetAlarm();
		}
		else if (item.getItemId() == R.id.radio)
		{
			showSetRadio();
		}
		else
		{
		}
		return super.onOptionsItemSelected(item);
	}

	private void showSetRadio() {
		startActivity(new Intent(this, InternetRadioActivity.class));
	}

	private void showSetAlarm() {
		startActivityForResult(new Intent(this, AlarmsActivity.class), 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		alarms = new Alarms(this); // re-load alarms
		alarms.scheduleNextAlarm(this);
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void updateClock() {
		Calendar c = Calendar.getInstance();
		DateFormat sdf = SimpleDateFormat
				.getTimeInstance(SimpleDateFormat.SHORT);
		clockView.setText(sdf.format(c.getTime()));
		maximize(clockView);
		// update alarm entry
		if (alarmMenu != null) {
			Alarm nextAlarm = alarms.nextAlarm();
			if (nextAlarm != null && alarms.isActive(this))
				alarmMenu.setTitle("Alarm: " + nextAlarm.toString());
			else
				alarmMenu.setTitle("Alarm: Aus");
		}
		if (stopAlarmButton.getVisibility() == View.VISIBLE)
			maximize(stopAlarmButton);
	}

	@SuppressWarnings("deprecation")
	public static void accquireWakeLock(Context context) {
		PowerManager powermgr = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = powermgr.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP,
				"RadioAlarmClock");
		wakeLock.acquire(3600 * 1000);
	}

	/*
	 * The following methods need to be defined here
	 * because of the inclusion of the package 'com.appgemacht.internetradio'
	 * @see com.appgemacht.radioalarmclock.InternetRadio.InternetRadioListener#onInternetRadioError(java.lang.String)
	 */
	
	@Override
	public void onInternetRadioError(String error) {
		Log.e("Alarm", "Cannot play radio: " + error);
		Toast.makeText(this, error, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onInternetRadioPrepareFinished() {
		Log.v("Alarm", "Radio prepare finished");
	}

}
