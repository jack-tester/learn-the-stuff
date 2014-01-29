package com.linkesoft.radioalarmclock;

import android.content.*;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction()!=null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
			// Ger‰t wurde gebootet, Alarm muss neu gesetzt werden
			Alarms alarms=new Alarms(context);
			alarms.scheduleNextAlarm(context);
			}
		else
		{
			// ein Alarm ist f‰llig, schalte Ger‰t ein und starte Radio
			Log.v("Alarm","Alarm up");
			MainActivity.accquireWakeLock(context);
			
			Intent i=new Intent(context,MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // wir starten die Activity auﬂerhalb einer anderen Activity
			i.putExtra(MainActivity.ALARM_UP, true);
			context.startActivity(i);
		}
	}

}
