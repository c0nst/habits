package javenue.habits;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javenue.habits.model.Checkmark;
import javenue.habits.model.Goal;
import javenue.habits.sql.DbHelper;
import javenue.habits.util.Alarms;

import static javenue.habits.model.Checkmark.DATE_FORMAT;

public class AlarmReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1;

    private static final String SNOOZE = "snooze";
    private static final String DONE = "done";
    public static final String ALARM = "alarm";

    private Context context;
    private Intent intent;
    private DbHelper dbHelper;

    private NotificationManager notificationManager;
    private Goal goal;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        dbHelper = new DbHelper(context);

        long goalId = intent.getLongExtra("goal", 0);
        goal = dbHelper.getGoal(goalId);

        if (goal != null) {
            String action = intent.getAction();

            if (ALARM.equals(action)) {
                SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.US);
                Checkmark checkmark = dbHelper.getCheckmark(goal, format.format(new Date()));
                Checkmark.Value value = CheckmarkAdapter.clarifyCheckmarkValue(context, checkmark);

                if (value == Checkmark.Value.UNCHECKED || value == Checkmark.Value.UNCHECKED_IMPLICIT)
                    displayNotification();
            }

            if (SNOOZE.equals(action)) {
                snoozeNotification();
                return;
            }

            if (DONE.equals(action))
                markAsDone();
        }

        Alarms.adjust(context);
    }

    private void markAsDone() {
        notificationManager.cancel(NOTIFICATION_ID);

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        Checkmark checkmark = dbHelper.getCheckmark(goal, format.format(new Date()));
        checkmark.setValue(Checkmark.Value.CHECKED);

        dbHelper.store(checkmark);
    }

    private void snoozeNotification() {
        notificationManager.cancel(NOTIFICATION_ID);

        Intent clone = (Intent) intent.clone();
        clone.setAction(ALARM);

        PendingIntent snoozeIntent = PendingIntent.getBroadcast(context, 0, clone, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 15 * 60 * 1000, snoozeIntent);
    }

    private void displayNotification() {
        Intent clone = (Intent) intent.clone();
        clone.setAction(SNOOZE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clone, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action snooze = new NotificationCompat.Action.Builder(R.drawable.ic_alarm_add_white_24dp, "SNOOZE", pendingIntent).build();

        clone = (Intent) intent.clone();
        clone.setAction(DONE);
        pendingIntent = PendingIntent.getBroadcast(context, 0, clone, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action markAsDone = new NotificationCompat.Action.Builder(R.drawable.ic_check_white_24dp, "MARK AS DONE", pendingIntent).build();

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(goal.getTitle())
                .setContentText(context.getResources().getString(R.string.notification_question))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(PendingIntent.getActivity(context, 1, new Intent(context, MainActivity.class), 0))
                .addAction(markAsDone)
                .addAction(snooze)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}