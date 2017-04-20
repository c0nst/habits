package javenue.habits;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javenue.habits.model.Checkmark;
import javenue.habits.model.Goal;
import javenue.habits.sql.DbHelper;
import javenue.habits.util.Alarms;
import javenue.habits.util.Images;
import javenue.habits.util.Settings;
import javenue.habits.util.ViewHolder;

class GoalsAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
    private List<Goal> goals;
    private boolean selectable;

    private int selectedItemPosition = -1;

    GoalsAdapter(Context context, List<Goal> items, boolean selectable) {
        this.context = context;
        this.goals = items;
        this.selectable = selectable;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_goal, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Goal goal = goals.get(position);

        TextView goalName = (TextView) holder.view.findViewById(R.id.goal_name);
        goalName.setText(goal.getTitle());

        TextView goalInfo = (TextView) holder.view.findViewById(R.id.goal_info);
        if (goal.getAlarm() == -1)
            goalInfo.setVisibility(View.GONE);
        else {
            goalInfo.setVisibility(View.VISIBLE);
            goalInfo.setText(Alarms.minutesToAlarmString(context, goal.getAlarm()));

            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_alarm_black_24dp);
            drawable = Images.scaleImage(context, drawable, 0.7f);
            Images.setTintColor(context, drawable, R.color.colorGray);

            goalInfo.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            goalInfo.setCompoundDrawablePadding(10);
        }

        if (selectable)
            if (selectedItemPosition == holder.getAdapterPosition()){
                holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.cardview_border));
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewGoalActivity.class);
                intent.putExtra(MainActivity.GOAL_ID, goal.getId());
                context.startActivity(intent);
            }
        });
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                notifyItemChanged(selectedItemPosition);
                selectedItemPosition = holder.getAdapterPosition();
                notifyItemChanged(selectedItemPosition);
                return true;
            }
        });

        List<Checkmark> values = getCheckmarks(goal);

        RecyclerView recycler = (RecyclerView) holder.view.findViewById(R.id.checkmark_list);

        LinearLayoutManager layout = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recycler.setLayoutManager(layout);

        CheckmarkAdapter checkmarkAdapter = new CheckmarkAdapter(context, values);
        recycler.setAdapter(checkmarkAdapter);

        checkmarkAdapter.notifyDataSetChanged();
    }

    private List<Checkmark> getCheckmarks(Goal goal) {
        int days = goal.getPeriodConsideringDefault() + 2;

        List<Checkmark> checkmarks = getRecentCheckmarks(context, goal, new Date(), days);

        clarifyCheckmarkDaysAgo(goal, checkmarks, 0);
        clarifyCheckmarkDaysAgo(goal, checkmarks, 1);
        clarifyCheckmarkDaysAgo(goal, checkmarks, 2);

        checkmarks = checkmarks.subList(0, 3);

        boolean leftToRight = Settings.isLeftToRightOrientation(context);
        if (!leftToRight)
            Collections.reverse(checkmarks);

        return checkmarks;
    }

    static List<Checkmark> getRecentCheckmarks(Context context, Goal goal, Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        DbHelper dbHelper = new DbHelper(context);

        List<Checkmark> checkmarks = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat(Checkmark.DATE_FORMAT, Locale.US);
        for (int i = 0; i < days; i++) {
            String day = format.format(calendar.getTime());
            Checkmark checkmark = dbHelper.getCheckmark(goal, day);
            checkmarks.add(checkmark);

            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        return checkmarks;
    }

    private static void clarifyCheckmarkDaysAgo(Goal goal, List<Checkmark> checkmarks, int daysAgo) {
        String today = new SimpleDateFormat(Checkmark.DATE_FORMAT, Locale.US).format(new Date());

        Checkmark day = checkmarks.get(daysAgo);
        if (day.getValue() == Checkmark.Value.UNCHECKED) {
            int checked = countChecked(checkmarks, daysAgo + 1, daysAgo + goal.getPeriodConsideringDefault());
            if (checked >= goal.getTimesConsideringDefault())
                day.setValue(Checkmark.Value.CHECKED_IMPLICIT);
            else if (day.getText().equals(today))
                day.setValue(Checkmark.Value.UNCHECKED_IMPLICIT);
        }
    }

    static int countChecked(List<Checkmark> checkmarks, int from, int to) {
        int checked = 0;
        for (int i = from; i < to; i++) {
            Checkmark c = checkmarks.get(i);
            if (c.getValue() == Checkmark.Value.CHECKED)
                checked++;
        }
        return checked;
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }
}
