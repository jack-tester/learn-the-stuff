package com.appgemacht.radioalarmclock;

import com.appgemacht.radioalarmclock.InternetRadio.InternetRadioListener;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.appgemacht.radioalarmclock.InternetRadio.InternetRadioListener;

public class MainActivity extends Activity implements InternetRadioListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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

}
