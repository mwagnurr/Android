package at.lnu.ass2.alarm;

import java.io.Serializable;
import java.util.Calendar;

import android.app.PendingIntent;

public class Alarm implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private final int alarmID;
	private Calendar calendar;
	
	public Alarm(int alarmID, Calendar calendar) {
		super();
		this.calendar = calendar;
		this.alarmID = alarmID;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	
	public int getAlarmID() {
		return alarmID;
	}

	public String getTimeAsString(){
		return calendar.get(Calendar.HOUR_OF_DAY) +  ":" + calendar.get(Calendar.MINUTE);
	}

	@Override
	public String toString() {
		return "Alarm " +alarmID + " [" + getTimeAsString() + "]";
	}
	
	
	
	
	
	

}
