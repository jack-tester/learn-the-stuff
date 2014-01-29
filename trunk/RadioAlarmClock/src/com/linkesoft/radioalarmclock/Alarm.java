package com.linkesoft.radioalarmclock;

import java.text.*;
import java.util.*;

import org.json.*;

import android.annotation.SuppressLint;
import android.util.Log;

/**
 * Ein einzelner Alarm, bestehend aus Stunden, Minuten und Wochentag. 
 *  
 * @author Andreas Linke
 *
 */
public class Alarm {
	
	public int hours;
	public int minutes;
	public boolean active;
	final public int dayOfWeek;
	
	public Alarm(int dayOfWeek)
	{
		this.dayOfWeek = dayOfWeek;
		hours=0;
		minutes=0;
		active=false;
	}
	
	// nächstmöglicher Alarmzeitpunkt in der Zukunft 
	public Calendar calendar()
	{
		Calendar c=Calendar.getInstance(); // heute
		c.set(Calendar.HOUR_OF_DAY, hours);
		c.set(Calendar.MINUTE, minutes);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		if(c.after(Calendar.getInstance())) // später als jetzt?
			return c;
		// c liegt in der Vergangenheit, addiere 1 Woche
		c.add(Calendar.WEEK_OF_MONTH,1);
		return c;
	}
	
	// could use GSON lib, but that's overkill here
	public Alarm(JSONObject json,int dayOfWeek)
	{
		this.dayOfWeek=dayOfWeek;
		try {
			hours=json.getInt("hours");
			minutes=json.getInt("minutes");
			active=json.getBoolean("active");
		} catch (JSONException e) {
			Log.e("Alarm", "Invalid json object "+json);
		}
	}
	
	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();
		try {
			json.put("hours", hours);
			json.put("minutes", minutes);
			json.put("active", active);
		} catch (JSONException e) {
			Log.e("Alarm", "Cannot serialize to json object");
		}
		return json;
	}
	
	// Wochentag + Uhrzeit, Datum egal
	@Override
	public String toString() {
		return weekdayAsShortString(dayOfWeek)+" "+timeAsString(hours, minutes);
	}

	// Hilfsfunktionen
	
	// sortierte Liste von Wochentagen, beginnend mit dem ersten Tag der Woche (Mo für Deutschland, So für USA etc)
	public static List<Integer> weekdays()
	{
		List<Integer> weekdays=new ArrayList<Integer>(7);
		Calendar c=Calendar.getInstance();
		int day=c.getFirstDayOfWeek();
		do			
		{
			weekdays.add(day);
			day++;
			if(day>Calendar.SATURDAY)
				day=Calendar.SUNDAY;
		}
		while(day!=c.getFirstDayOfWeek());
		return weekdays;
	}
	
	@SuppressLint("SimpleDateFormat") // Spezialfall: nur Wochentag (ausgeschrieben)
	public static CharSequence weekdayAsLongString(int dayOfWeek) {
		Calendar c=Calendar.getInstance(); 
		c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		return  sdf.format(c.getTime());
	}
	@SuppressLint("SimpleDateFormat") // Spezialfall: nur Wochentag (kurz)
	public static CharSequence weekdayAsShortString(int dayOfWeek) {
		Calendar c=Calendar.getInstance(); 
		c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		SimpleDateFormat sdf = new SimpleDateFormat("E");
		return  sdf.format(c.getTime());
	}
	public static CharSequence timeAsString(int hours,int minutes) {
		Calendar c=Calendar.getInstance(); 
		c.set(Calendar.HOUR_OF_DAY, hours);
		c.set(Calendar.MINUTE, minutes);
		DateFormat sdf = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
		return  sdf.format(c.getTime());
	}
}
