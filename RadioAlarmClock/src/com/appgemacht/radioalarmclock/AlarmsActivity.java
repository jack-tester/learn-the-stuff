package com.appgemacht.radioalarmclock;

import java.util.*;

// http://actionbarsherlock.com/usage.html
//import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
//import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

//import android.app.DialogFragment;

//import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

//import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * This activity is used to control the definition of wake up times per week
 * day.
 * 
 * It facilitates the package internal classes Alarms, Alarm and
 * SetAlarmFragment.
 * 
 * @author Dietmar (derived from c't sample application RadioAlarmClock)
 * 
 */
public class AlarmsActivity extends SherlockFragmentActivity {
    public Alarms alarms;
    private ListView listView;
    private ListView alarmStatus;
    private AlarmArrayAdapter alarmArrayAdapter;
    private final List<List<Alarm>> alarmGroups = new ArrayList<List<Alarm>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarms = new Alarms(this);
        setContentView(R.layout.setalarms);
        setTitle("Alarms");
        getSupportActionBar().setHomeButtonEnabled(true); // tipp auf das
                                                          // App-Icon oben links
                                                          // f�hrt zur�ck
                                                          // (android.R.id.home
                                                          // option menu)

        // Switch in der ActionBar
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        // Switch alarmsActive=new Switch(this); // ab 4.0, CheckBox f�r �ltere
        // Releases
        //alarmsActive.setChecked(alarms.isActive(this));
        // alarmsActive.setOnCheckedChangeListener(new OnCheckedChangeListener()
        // {
        //
        // @Override
        // public void onCheckedChanged(CompoundButton buttonView, boolean
        // isChecked) {
        // alarms.setActive(AlarmsActivity.this, isChecked);
        // }
        // });
        // getSupportActionBar().setCustomView(alarmsActive);
        // alarmsActive.setChecked(true); // beim �ffnen der Activity soll der
        // Switch per default on sein

        // Liste mit Alarm-Gruppen
        listView = (ListView) findViewById(android.R.id.list);
        
        alarmArrayAdapter = new AlarmArrayAdapter(this, alarmGroups);
        
        listView.setAdapter(alarmArrayAdapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View view,
                    int position, long id) {
                selectAlarmItem(position);
                return true;
            }
        });
        
        
//        listView.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapter, View view,
//                    int position, long id) {
//                selectAlarmItem(position);
//
//            }
//        });
        

        alarmStatus = (ListView) findViewById(R.id.alarmStatus);

        
    }

    private void selectAlarmItem(int position) {
        List<Alarm> alarmGroup = alarmGroups.get(position);
        listView.setItemChecked(position, true);

        SetAlarmFragment alarmFragment = new SetAlarmFragment();
        alarmFragment.load(alarmGroup);
        alarmFragment.show(getSupportFragmentManager(), null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home: // Tipp auf Icon oben links in actionbar
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        // Alarme haben sich ge�ndert, frische Liste auf
        alarmGroups.clear();
        alarmGroups.addAll(alarms.alarmGroups());
        if (alarmGroups.size() != 7)
            alarmGroups.add(new ArrayList<Alarm>()); // "Add" button

        alarmArrayAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        alarms.storeAlarms(this);
        super.onPause();
    }

    // ListAdapter, der die Darstellung der Alarme in der Liste festlegt
    private class AlarmArrayAdapter extends ArrayAdapter<List<Alarm>> {
        private List<List<Alarm>> alarmGroups;

        public AlarmArrayAdapter(Context context, List<List<Alarm>> alarmGroups) {
            super(context, android.R.layout.simple_list_item_2,
                    android.R.id.text1, alarmGroups);
            this.alarmGroups = alarmGroups;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            List<Alarm> alarmGroup = alarmGroups.get(position);
            View row = super.getView(position, convertView, parent);

            TextView text1 = (TextView) row.findViewById(android.R.id.text1);
            TextView text2 = (TextView) row.findViewById(android.R.id.text2);
            if (alarmGroup.size() == 0) {
                text1.setText(R.string.Add);
                text2.setText("");
                return row;
            }
            // Wochentage
            StringBuffer weekdays = new StringBuffer();
            for (Alarm alarm : alarmGroup) {
                if (weekdays.length() != 0)
                    weekdays.append(", ");
                weekdays.append(Alarm.weekdayAsShortString(alarm.dayOfWeek));
            }
            text1.setText(weekdays);

            // Uhrzeit
            Alarm alarm = alarmGroup.get(0);
            text2.setText(Alarm.timeAsString(alarm.hours, alarm.minutes));

            if (listView.isItemChecked(position))
                row.setBackgroundColor(Color.argb(0x80, 0xA0, 0xA0, 0xA0)); // mark
                                                                            // active
                                                                            // cell
            else
                row.setBackgroundColor(Color.TRANSPARENT);
            return row;
        }
    }
}
