package com.appgemacht.radioalarmclock;

import java.util.List;

import com.actionbarsherlock.app.*;

//import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.*;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TimePicker.OnTimeChangedListener;

public class SetAlarmFragment extends SherlockDialogFragment {

	private List<Alarm> alarmsGroup; // Gruppe von Alarmen mit gleicher Weckzeit
	private TimePicker timePicker;
	private ViewGroup weekdayButtons;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(getDialog()!=null)
			getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

		final View v = inflater.inflate(R.layout.setalarm, container,
				false);
		
		timePicker = (TimePicker) v.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(DateFormat.is24HourFormat(getActivity()));
		weekdayButtons = (ViewGroup) v.findViewById(R.id.weekdayButtons);
		
		List<Integer> weekdays = Alarm.weekdays();
		for(int i=0;i<weekdayButtons.getChildCount();i++)
		{
			ToggleButton weekdayButton=(ToggleButton)weekdayButtons.getChildAt(i);
			weekdayButton.setTextOn(Alarm.weekdayAsShortString(weekdays.get(i)));
			weekdayButton.setTextOff(weekdayButton.getTextOn());
			weekdayButton.setTag(weekdays.get(i));
		}
		update();

		return v;
	}
	
	private final OnCheckedChangeListener onToggleWeekdayButton=new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton button, boolean isChecked) {
			int weekday=(Integer)button.getTag();
			Alarm alarm=((AlarmsActivity)getActivity()).alarms.alarmsPerDayOfWeek[weekday];
			if(isChecked)
			{
				alarm.active=true;
				alarm.hours=timePicker.getCurrentHour();
				alarm.minutes=timePicker.getCurrentMinute();
				alarmsGroup.add(alarm);
			}
			else
			{
				alarm.active=false;
				alarmsGroup.remove(alarm);
			}
			//activity.refresh(alarmsGroup); 
		}
	};
	private final OnTimeChangedListener onTimeChangedListener=new OnTimeChangedListener() {
		@Override
		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
			for(Alarm alarm:alarmsGroup)
			{
				alarm.hours = hourOfDay;
				alarm.minutes = minute;
			}
			//activity.refresh(alarmsGroup);
		}
	};
	
	public void load(List<Alarm> alarmsGroup)
	{
		this.alarmsGroup=alarmsGroup;
		if(timePicker!=null)
			update();
	}
	@Override
	public void onDismiss(DialogInterface dialog) {
		((AlarmsActivity)getActivity()).refresh();
		super.onDismiss(dialog);
	}
	
	private void update()
	{
		// aktive Wochentage
		for(int i=0;i<weekdayButtons.getChildCount();i++)
		{
			ToggleButton button = (ToggleButton) weekdayButtons.getChildAt(i);
			button.setOnCheckedChangeListener(null); // kein Update
			button.setChecked(false);
			for(Alarm alarm:alarmsGroup)
			{
				if(alarm.dayOfWeek==(Integer)button.getTag())
				{
					button.setChecked(true);
					break;
				}
			}
			button.setOnCheckedChangeListener(onToggleWeekdayButton);
		}
		// Uhrzeit
		timePicker.setOnTimeChangedListener(onTimeChangedListener);
		if (alarmsGroup.size() != 0) {
			Alarm firstAlarm = alarmsGroup.get(0);
			timePicker.setCurrentHour(firstAlarm.hours);
			timePicker.setCurrentMinute(firstAlarm.minutes);
		}
		else {
			timePicker.setCurrentHour(7);
			timePicker.setCurrentMinute(0);
		}
		timePicker.setOnTimeChangedListener(onTimeChangedListener);
	}	
}
