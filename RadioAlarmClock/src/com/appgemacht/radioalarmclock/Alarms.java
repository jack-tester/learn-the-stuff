package com.appgemacht.radioalarmclock;

import java.text.SimpleDateFormat;
import java.util.*;

import org.json.*;

import android.app.*;
import android.content.*;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Verwalte Alarme, ein Alarm-Objekt pro Wochentag Da dayOfWeek von 1..7 läuft
 * sind einige Umrechnungen notwendig
 * 
 */
public class Alarms {

    public Alarm[] alarmsPerDayOfWeek = new Alarm[8]; // 0
                                                      // ignoriert,1=So,2=Mo,...,7=Sa

    public Alarms(Context context) {
        for (int i = 0; i < alarmsPerDayOfWeek.length; i++)
            alarmsPerDayOfWeek[i] = new Alarm(i); //
        loadAlarms(context);
    }

    // Liste von Alarms gruppiert nach gleicher Weckzeit
    // z.B. Mo,Di,Mi,Fr 7:00, Do 6:00, Sa,So 10:00
    public List<List<Alarm>> alarmGroups() {
        List<List<Alarm>> groups = new ArrayList<List<Alarm>>();
        weekdayLoop: for (int day : Alarm.weekdays()) {
            Alarm alarm = alarmsPerDayOfWeek[day];
            if (!alarm.active)
                continue;
            CharSequence alarmstr = Alarm.timeAsString(alarm.hours,
                    alarm.minutes);
            // haben wir schon einen Eintrag mit dieser Alarmzeit?
            for (List<Alarm> group : groups) {
                Alarm firstAlarmInGroup = group.get(0);
                if (alarmstr.equals(Alarm.timeAsString(firstAlarmInGroup.hours,
                        firstAlarmInGroup.minutes))) {
                    group.add(alarm);
                    continue weekdayLoop;
                }
            }
            // neuer Eintrag
            List<Alarm> group = new ArrayList<Alarm>();
            group.add(alarm);
            groups.add(group);
        }
        return groups;
    }

    public boolean isActive(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getBoolean("active", false);
    }

    public void setActive(Context context, boolean active) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean("active", active);
        prefsEditor.commit();
    }

    public Alarm nextAlarm() {
        Alarm nextAlarm = null;
        // finde minimum
        for (int i = 0; i < alarmsPerDayOfWeek.length; i++) {
            Alarm a = alarmsPerDayOfWeek[i];
            if (a.active) {
                if (nextAlarm == null
                        || nextAlarm.calendar().after(a.calendar()))
                    nextAlarm = a;
            }
        }
        return nextAlarm;
    }

    private void loadAlarms(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String jsonstr = prefs.getString("alarms", null);
        if (jsonstr != null)
            loadFromJSON(jsonstr);
        else
            initDefaultAlarms();
    }

    public void storeAlarms(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        JSONObject json = new JSONObject();
        storeToJSON(json);
        prefsEditor.putString("alarms", json.toString());
        prefsEditor.commit();
    }

    private void loadFromJSON(String jsonstr) {
        try {
            JSONObject json = new JSONObject(jsonstr);
            JSONArray jsonAlarms = json.getJSONArray("alarms");
            for (int i = 0; i < jsonAlarms.length()
                    && i < alarmsPerDayOfWeek.length; i++) {
                alarmsPerDayOfWeek[i] = new Alarm(jsonAlarms.getJSONObject(i),
                        i);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void storeToJSON(JSONObject json) {
        JSONArray jsonAlarms = new JSONArray();
        for (int i = 0; i < alarmsPerDayOfWeek.length; i++)
            jsonAlarms.put(alarmsPerDayOfWeek[i].toJSON());
        try {
            json.put("alarms", jsonAlarms);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void scheduleNextAlarm(Context context) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmReceiver.class), 0);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Alarm alarm = nextAlarm();
        // TODO check why 'isActive' does not work properly ...
        if (alarm == null)// ||!isActive(context))
            return; // kein aktiver Alarm
        Log.v("Alarm",
                "Schedule next alarm for "
                        + SimpleDateFormat.getDateTimeInstance().format(
                                alarm.calendar().getTime()));
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.calendar()
                .getTimeInMillis(), pendingIntent);
    }

    // Alarm an allen Arbeitstagen
    public void initDefaultAlarms() {
        for (int i = 0; i < alarmsPerDayOfWeek.length; i++) {
            Alarm alarm = alarmsPerDayOfWeek[i];
            if (i != Calendar.SATURDAY && i != Calendar.SUNDAY) {
                alarm.hours = 7;
                alarm.minutes = 0;
                alarm.active = true;
            } else {
                // Wochenende
                alarm.hours = 10;
                alarm.minutes = 0;
                alarm.active = false;
            }
        }

    }

}
