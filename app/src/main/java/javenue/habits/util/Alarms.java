package javenue.habits.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.DecimalFormat;
import java.util.Calendar;

import javenue.habits.AlarmReceiver;
import javenue.habits.R;
import javenue.habits.model.Goal;
import javenue.habits.sql.DbHelper;

public class Alarms {
    private static PendingIntent alarmIntent;

    public static void adjust(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar now = Calendar.getInstance();
        int currentTime = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        Goal goal = new DbHelper(context).getGoalWithNearestAlarm(currentTime);

        if (goal == null) {
            if (alarmIntent != null)
                alarmManager.cancel(alarmIntent);
        } else {
            int minutesTillAlarm = goal.getAlarm() > currentTime ?
                    (goal.getAlarm() - currentTime) :
                    (goal.getAlarm() + 24 * 60 - currentTime);

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("goal", goal.getId());
            intent.setAction(AlarmReceiver.ALARM);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            alarmManager.set(AlarmManager.RTC, now.getTimeInMillis() + minutesTillAlarm * 60 * 1000, alarmIntent);
        }
    }

    public static int alarmToMinutes(String alarm) {
        int index = alarm.indexOf(":");
        int hours = Integer.valueOf(alarm.substring(0, index));
        int minutes = Integer.valueOf(alarm.substring(index + 1));
        return hours * 60 + minutes;
    }

    public static String minutesToAlarmString(Context context, int minutes) {
        if (minutes == -1)
            return context.getString(R.string.alarm_item_off);

        return minutes / 60 + ":" + new DecimalFormat("00").format(minutes % 60);
    }
}
