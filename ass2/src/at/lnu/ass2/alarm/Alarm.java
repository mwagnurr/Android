package at.lnu.ass2.alarm;

import java.io.Serializable;
import java.util.Calendar;

public class Alarm implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private final int alarmID;
	private Calendar calendar;

	public Alarm(long id, int alarmID, Calendar calendar) {
		super();
		this.calendar = calendar;
		this.alarmID = alarmID;
		this.id = id;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	/**
	 * id for alarm manager
	 * 
	 * @return
	 */
	public int getAlarmID() {
		return alarmID;
	}

	public String getTimeAsString() {
		return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
	}

	/**
	 * database id
	 * 
	 * @return
	 */
	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Alarm [id=" + id + ", alarmID=" + alarmID + " [" + getTimeAsString() + "]]";
	}

}
