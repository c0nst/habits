package javenue.habits;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javenue.habits.model.Goal;
import javenue.habits.sql.DbHelper;
import javenue.habits.util.RecyclerViewEmptySupport;
import javenue.habits.util.Settings;

public class ArchiveFragment extends Fragment {
    private View view;

    private List<Goal> goals = new ArrayList<>();
    private RecyclerViewEmptySupport goalsListView;
    private GoalsAdapter goalsAdapter;

    public List<Goal> getGoals() {
        return goals;
    }

    public RecyclerViewEmptySupport getGoalsListView() {
        return goalsListView;
    }

    public GoalsAdapter getGoalsAdapter() {
        return goalsAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(getLayoutId(), container, false);

        setDaysOfWeekHeader();

        goalsListView = (RecyclerViewEmptySupport) view.findViewById(R.id.goals_list);
        goalsListView.setEmptyView(view.findViewById(R.id.goals_empty_list));

        LinearLayoutManager layout = new LinearLayoutManager(getActivity());
        goalsListView.setLayoutManager(layout);

        goalsAdapter = new GoalsAdapter(getActivity(), goals, this instanceof GoalsFragment);
        goalsListView.setAdapter(goalsAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        reloadGoals();
    }

    protected int getLayoutId() {
        return R.layout.fragment_archive;
    }

    protected void reloadGoals() {
        new ReloadGoalsTask(getActivity()).execute();
    }

    private class ReloadGoalsTask extends AsyncTask<Void, Void, Void> {
        Context context;

        ReloadGoalsTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            boolean archived = isArchive();

            DbHelper dbHelper = new DbHelper(context);
            goals.clear();
            goals.addAll(dbHelper.getGoals(archived));

            Settings.restoreGoalsPosition(context, goals);

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            goalsAdapter.notifyDataSetChanged();
        }
    }

    private boolean isArchive() {
        return !GoalsFragment.class.equals(getClass());
    }

    private void setDaysOfWeekHeader() {
        Activity context = getActivity();
        boolean isLeftToRight = Settings.isLeftToRightOrientation(context);

        TextView today = (TextView) view.findViewById(R.id.today);
        today.setText(getDayOfWeek(isLeftToRight ? 0 : 2));
        if (isLeftToRight)
            today.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                    ContextCompat.getDrawable(context, R.drawable.current_day_dot));

        TextView yesterday = (TextView) view.findViewById(R.id.yesterday);
        yesterday.setText(getDayOfWeek(1));

        TextView twoDaysBefore = (TextView) view.findViewById(R.id.twoDaysBefore);
        twoDaysBefore.setText(getDayOfWeek(isLeftToRight ? 2 : 0));
        if (!isLeftToRight)
            twoDaysBefore.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                    ContextCompat.getDrawable(context, R.drawable.current_day_dot));
    }

    protected String getDayOfWeek(int daysBefore) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysBefore);

        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY: return "Mo";
            case Calendar.TUESDAY: return "Tu";
            case Calendar.WEDNESDAY: return "We";
            case Calendar.THURSDAY: return "Th";
            case Calendar.FRIDAY: return "Fr";
            case Calendar.SATURDAY: return "Sa";
            case Calendar.SUNDAY: return "Su";
            default: return "";
        }
    }
}
