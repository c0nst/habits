package javenue.habits;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import javenue.habits.model.Checkmark;
import javenue.habits.model.Goal;
import javenue.habits.sql.DbHelper;
import javenue.habits.util.Images;
import javenue.habits.util.LookBehind;

public class StatisticsFragment extends Fragment {
    public static final String USER_DATE_FORMAT = "dd MMM yyyy";

    View view;

    Goal goal;
    Map<String, Checkmark> checkmarksMap;

    int bestStrike;
    int currentStrike;

    int daysTotal;
    int daysCompleted;
    int daysMaintained;
    int percentCompleted;

    String startDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getContext();

        view = inflater.inflate(R.layout.fragment_statistics, container, false);

        TextView textView = (TextView) view.findViewById(R.id.title_strikes);
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_thumb_up_black_24dp);
        Images.setTintColor(context, drawable, R.color.colorPrimaryDark);
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        textView.setCompoundDrawablePadding(25);

        textView = (TextView) view.findViewById(R.id.title_stats);
        drawable = ContextCompat.getDrawable(context, R.drawable.ic_assessment_black_24dp);
        Images.setTintColor(context, drawable, R.color.colorPrimaryDark);
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        textView.setCompoundDrawablePadding(25);

        textView = (TextView) view.findViewById(R.id.title_info);
        drawable = ContextCompat.getDrawable(context, R.drawable.ic_info_black_24dp);
        Images.setTintColor(context, drawable, R.color.colorPrimaryDark);
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        textView.setCompoundDrawablePadding(25);

        extractGoal();

        refreshStats();

        return view;
    }

    public void refreshStats() {
        readCheckmarks();
        clearStats();
        calculateStats();
        populateStats();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            refreshStats();
        }
    }

    private void extractGoal() {
        Intent intent = getActivity().getIntent();
        long goalId = intent.getLongExtra(MainActivity.GOAL_ID, 0);

        DbHelper dbHelper = new DbHelper(getContext());
        goal = dbHelper.getGoal(goalId);
    }

    private void readCheckmarks() {
        DbHelper dbHelper = new DbHelper(getContext());

        checkmarksMap = dbHelper.getCheckmarksMap(goal);
    }

    private void clearStats() {
        bestStrike = 0;
        currentStrike = 0;

        daysTotal = 0;
        daysCompleted = 0;
        daysMaintained = 0;
        percentCompleted = 0;

        startDate = "";
    }

    private void calculateStats() {
        if (checkmarksMap.isEmpty())
            return;

        DateFormat format = new SimpleDateFormat(Checkmark.DATE_FORMAT, Locale.US);
        DateFormat userFormat = new SimpleDateFormat(USER_DATE_FORMAT, Locale.US);

        final Calendar current = Calendar.getInstance();
        String currentDate = format.format(current.getTime());

        String oldest = checkmarksMap.keySet().iterator().next();
        Calendar date = Calendar.getInstance();
        try {
            date.setTime(format.parse(oldest));
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
        } catch (ParseException e) {
            return;
        }

        startDate = userFormat.format(date.getTime());

        LookBehind lookBehind = new LookBehind(goal.getTimesConsideringDefault(), goal.getPeriodConsideringDefault());

        while (true) {
            if (date.after(current))
                break;

            String checkmarkDate = format.format(date.getTime());

            Checkmark checkmark = checkmarksMap.get(checkmarkDate);
            if (checkmark == null) {
                checkmark = new Checkmark();
                checkmark.setText(checkmarkDate);
                checkmark.setGoalId(goal.getId());
            }

            if (lookBehind.shouldBeImplicitlyChecked(checkmark))
                checkmark.setValue(Checkmark.Value.CHECKED_IMPLICIT);

            if (checkmark.getValue() == Checkmark.Value.CHECKED) {
                daysCompleted++;
                daysMaintained++;
                currentStrike++;
            }

            if (checkmark.getValue() == Checkmark.Value.CHECKED_IMPLICIT) {
                daysMaintained++;
                currentStrike++;
            }

            if (checkmark.getValue() == Checkmark.Value.UNCHECKED) {
                if (currentStrike > bestStrike)
                    bestStrike = currentStrike;

                if (!currentDate.equals(format.format(date.getTime())))
                    currentStrike = 0;
            }

            daysTotal++;

            lookBehind.push(checkmark);

            date.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (currentStrike > bestStrike)
            bestStrike = currentStrike;

        percentCompleted = (int) ((100.0 * daysMaintained) / daysTotal);
    }

    private void populateStats() {
        TextView currentStrikeView = (TextView) view.findViewById(R.id.stats_current_strike);
        currentStrikeView.setText("Current strike: " + currentStrike);

        TextView bestStrikeView = (TextView) view.findViewById(R.id.stats_best_strike);
        bestStrikeView.setText("Best strike: " + bestStrike);

        TextView daysCompletedView = (TextView) view.findViewById(R.id.stats_days_completed);
        daysCompletedView.setText("Days completed: " + daysCompleted);

        TextView percentCompletedView = (TextView) view.findViewById(R.id.stats_percent_completed);
        percentCompletedView.setText("Percent completed: " + percentCompleted + "%");

        TextView startDateView = (TextView) view.findViewById(R.id.stats_start_date);
        startDateView.setText("Start date: " + startDate);
    }
}
