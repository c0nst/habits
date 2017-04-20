package javenue.habits.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Collections;
import java.util.List;

import javenue.habits.R;
import javenue.habits.model.Goal;

public class Settings {
    public static boolean pressAndHoldToCheck(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pref_press_and_hold_to_check", false);
    }

    public static boolean isLeftToRightOrientation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String leftToRight = context.getString(R.string.left_to_right_orientation);
        String checkmarkOrientation = prefs.getString("pref_checkmark_orientation", leftToRight);
        return leftToRight.equals(checkmarkOrientation);
    }

    public static boolean isMondayFirstDay(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String monday = context.getString(R.string.day_of_week_monday);
        String firstDayOfWeek = prefs.getString("pref_first_day_of_week", monday);
        return monday.equals(firstDayOfWeek);
    }

    public static boolean regularDaysOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("pref_calendar_regular_days_order", false);
    }

    public static void storeGoalsPosition(Context context, List<Goal> goals) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < goals.size(); i++)
            builder.append(goals.get(i).getId()).append(" ");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("pref_goals_position", builder.toString());
        editor.apply();
    }

    public static void restoreGoalsPosition(Context context, List<Goal> goals) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String string = preferences.getString("pref_goals_position", "");
        String[] goalIds = string.split(" ");
        for (int i = 0; i < goalIds.length; i++) {
            String s = goalIds[i];
            for (Goal g : goals) {
                if (s.equals(g.getId() + ""))
                    g.setWeight(goalIds.length - i);
            }
        }
        Collections.sort(goals);
    }

    public static boolean isCheckmarksMigrated(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_checkmarks_migrated", false);
    }

    public static void setCheckmarksMigrated(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("pref_checkmarks_migrated", true);
        editor.apply();
    }
}
