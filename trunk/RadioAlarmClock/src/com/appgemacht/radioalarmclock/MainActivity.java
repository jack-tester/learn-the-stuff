package com.appgemacht.radioalarmclock;

import java.util.Timer;

import com.appgemacht.radioalarmclock.InternetRadio.InternetRadioListener;

import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.appgemacht.radioalarmclock.InternetRadio.InternetRadioListener;
import com.appgemacht.radioalarmclock.Alarms;
import com.appgemacht.radioalarmclock.R;
import com.appgemacht.radioalarmclock.InternetRadio;

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


	
	@SuppressWarnings("deprecation")
	public static void accquireWakeLock(Context context) {
		PowerManager powermgr = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
//		wakeLock = powermgr.newWakeLock(PowerManager.FULL_WAKE_LOCK
//				| PowerManager.ACQUIRE_CAUSES_WAKEUP,
//				"RadioAlarmClock");
//		wakeLock.acquire(3600 * 1000);
	}
	
}
