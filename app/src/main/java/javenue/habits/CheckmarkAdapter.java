package javenue.habits;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javenue.habits.model.Checkmark;
import javenue.habits.model.Goal;
import javenue.habits.sql.DbHelper;
import javenue.habits.util.Images;
import javenue.habits.util.Settings;
import javenue.habits.util.ViewHolder;

class CheckmarkAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
    private List<Checkmark> checkmarks;

    CheckmarkAdapter(Context context, List<Checkmark> checkmarks) {
        this.context = context;
        this.checkmarks = checkmarks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_checkmark, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Checkmark checkmark = checkmarks.get(position);

        ImageView image = (ImageView) holder.view.findViewById(R.id.checkmark_image);

        int resource = R.drawable.ic_check_black_24dp;
        int colorId = R.color.colorPrimary;
        boolean scale = false;
        switch (checkmark.getValue()) {
            case CHECKED_IMPLICIT:
                scale = true;
                break;
            case UNCHECKED:
                resource = R.drawable.ic_clear_black_24dp;
                colorId = R.color.colorAccent;
                break;
            case UNCHECKED_IMPLICIT:
                resource = R.drawable.ic_clear_black_24dp;
                colorId = R.color.colorAccent;
                scale = true;
                break;
        }

        Drawable drawable = ContextCompat.getDrawable(context, resource);
        if (scale)
            drawable = Images.scaleImage(context, drawable, 0.5f);
        Images.setTintColor(context, drawable, colorId);
        image.setImageDrawable(drawable);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean pressAndHoldToCheck = Settings.pressAndHoldToCheck(context);

                if (pressAndHoldToCheck)
                    Toast.makeText(context, R.string.hint_press_and_hold, Toast.LENGTH_SHORT).show();
                else {
                    toggleCheckmark(checkmark);
                    updatePrevious(holder.getAdapterPosition());
                    notifyDataSetChanged();
                }
            }
        });

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                toggleCheckmark(checkmark);
                updatePrevious(holder.getAdapterPosition());
                notifyDataSetChanged();

                return true;
            }
        });
    }

    private void updatePrevious(int adapterPosition) {
        DbHelper dbHelper = new DbHelper(context);

        for (int i = 0; i < adapterPosition; i++) {
            Checkmark checkmark = checkmarks.get(i);
            checkmark.setValue(clarifyCheckmarkValue(context, checkmark));
            dbHelper.store(checkmark);
        }
    }

    private void toggleCheckmark(Checkmark checkmark) {
        DbHelper dbHelper = new DbHelper(context);

        checkmark.toggle();

        Checkmark.Value value = clarifyCheckmarkValue(context, checkmark);
        checkmark.setValue(value);

        dbHelper.store(checkmark);
    }

    static Checkmark.Value clarifyCheckmarkValue(Context context, Checkmark checkmark) {
        if (checkmark.getValue() == Checkmark.Value.CHECKED)
            return checkmark.getValue();

        SimpleDateFormat format = new SimpleDateFormat(Checkmark.DATE_FORMAT, Locale.US);

        DbHelper dbHelper = new DbHelper(context);
        Goal goal = dbHelper.getGoal(checkmark.getGoalId());

        Date date;
        try {
            date = format.parse(checkmark.getText());
        } catch (ParseException e) {
            throw new RuntimeException();
        }
        List<Checkmark> recent = GoalsAdapter.getRecentCheckmarks(context, goal, date, goal.getPeriodConsideringDefault());
        int checked = GoalsAdapter.countChecked(recent, 1, recent.size());

        if (checked >= goal.getTimesConsideringDefault())
            return Checkmark.Value.CHECKED_IMPLICIT;
        else {
            String today = format.format(new Date());
            if (checkmark.getText().equals(today))
                return Checkmark.Value.UNCHECKED_IMPLICIT;
        }

        return Checkmark.Value.UNCHECKED;
    }

    @Override
    public int getItemCount() {
        return checkmarks.size();
    }
}
