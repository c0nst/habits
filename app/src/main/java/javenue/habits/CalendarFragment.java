package javenue.habits;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javenue.habits.model.Checkmark;
import javenue.habits.model.Goal;
import javenue.habits.model.Mark;
import javenue.habits.model.Month;
import javenue.habits.sql.DbHelper;
import javenue.habits.util.LookBehind;
import javenue.habits.util.Settings;

public class CalendarFragment extends Fragment {
    Goal goal;
    Map<String, Checkmark> checkmarksMap;

    boolean mondayFirstDay;
    CalendarAdapter calendarAdapter;

    private final static Mark EMPTY = new Mark();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getContext();
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        extractGoal();

        final RecyclerView recycler = (RecyclerView) view.findViewById(R.id.calendar);
        mondayFirstDay = Settings.isMondayFirstDay(context);
        calendarAdapter = new CalendarAdapter(context, getMonths(checkmarksMap), mondayFirstDay);

        LinearLayoutManager layout = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(layout);
        layout.setStackFromEnd(true);

        recycler.setAdapter(calendarAdapter);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recycler.scrollToPosition(calendarAdapter.getItemCount() - 1);
            }
        }, 50);

        return view;
    }

    private void extractGoal() {
        Intent intent = getActivity().getIntent();
        long goalId = intent.getLongExtra(MainActivity.GOAL_ID, 0);

        DbHelper dbHelper = new DbHelper(getContext());
        goal = dbHelper.getGoal(goalId);

        checkmarksMap = dbHelper.getCheckmarksMap(goal);
    }

    private List<Month> getMonths(Map<String, Checkmark> checkmarksMap) {
        final Calendar currentDate = Calendar.getInstance();
        DateFormat format = new SimpleDateFormat(Checkmark.DATE_FORMAT, Locale.US);
        DateFormat monthNameFormat = new SimpleDateFormat("MMM yyyy", Locale.US);

        if (checkmarksMap.isEmpty()) {
            String current = format.format(currentDate.getTime());
            checkmarksMap.put(current, new Checkmark(current, goal.getId()));
        }

        String oldest = checkmarksMap.keySet().iterator().next();
        Calendar oldestDate = Calendar.getInstance();
        try {
            oldestDate.setTime(format.parse(oldest));
            oldestDate.set(Calendar.DAY_OF_MONTH, 1);
            oldestDate.set(Calendar.HOUR_OF_DAY, 0);
            oldestDate.set(Calendar.MINUTE, 0);
            oldestDate.set(Calendar.SECOND, 0);
            oldestDate.set(Calendar.MILLISECOND, 0);
        } catch (ParseException e) {
            oldestDate.setTime(currentDate.getTime());
        }
        oldestDate.add(Calendar.MONTH, -1);

        lookBehind = new LookBehind(goal.getTimesConsideringDefault(), goal.getPeriodConsideringDefault());

        List<Month> months = new ArrayList<>();
        while (true) {
            if (oldestDate.getTimeInMillis() >= currentDate.getTimeInMillis())
                break;

            Month month = new Month();
            month.setName(monthNameFormat.format(oldestDate.getTime()));

            month.setMarks(getCheckmarksForMonth(checkmarksMap, oldestDate, currentDate));
            months.add(month);

            oldestDate.add(Calendar.MONTH, 1);
        }

        return months;
    }


    LookBehind lookBehind;
    boolean trackingStarted;

    List<Mark> getCheckmarksForMonth(Map<String, Checkmark> checkmarkMap, Calendar month, Calendar currentDate) {
        boolean regularDaysOrder = Settings.regularDaysOrder(getContext()); // TODO

        Calendar monthCopy = (Calendar) month.clone();

        int max = monthCopy.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (monthCopy.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH))
            max = currentDate.get(Calendar.DAY_OF_MONTH);

        DateFormat format = new SimpleDateFormat(Checkmark.DATE_FORMAT, Locale.US);
        monthCopy.set(Calendar.DAY_OF_MONTH, 1);
        int offset = monthCopy.get(Calendar.DAY_OF_WEEK) - 1 - (mondayFirstDay ? 1 : 0);
        if (offset == -1) offset = 0;

        List<Mark> checkmarks = new ArrayList<>();

        for (int i = 0; i < offset; i++)
            checkmarks.add(EMPTY);

        for (int i = 0; i < max; i++) {
            String checkmarkDate = format.format(monthCopy.getTime());

            Checkmark checkmark = checkmarkMap.get(checkmarkDate);
            if (checkmark == null) {
                checkmark = new Checkmark();
                checkmark.setText(checkmarkDate);
                checkmark.setGoalId(goal.getId());
            }
            checkmarks.add(checkmark);

            if (trackingStarted && lookBehind.shouldBeImplicitlyChecked(checkmark))
                checkmark.setValue(Checkmark.Value.CHECKED_IMPLICIT);

            if (checkmark.getValue() == Checkmark.Value.CHECKED) {
                trackingStarted = true;
            }

            if (trackingStarted)
                lookBehind.push(checkmark);

            monthCopy.add(Calendar.DAY_OF_MONTH, 1);
        }

        return checkmarks;
    }
}
