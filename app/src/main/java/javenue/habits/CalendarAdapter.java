package javenue.habits;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.List;

import javenue.habits.model.Checkmark;
import javenue.habits.model.Mark;
import javenue.habits.model.Month;
import javenue.habits.sql.DbHelper;
import javenue.habits.util.ViewHolder;

class CalendarAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
    private List<Month> months;
    private boolean isMondayFirstDayOfWeek;

    CalendarAdapter(Context context, List<Month> months, boolean isMondayFirstDayOfWeek) {
        this.context = context;
        this.months = months;
        this.isMondayFirstDayOfWeek = isMondayFirstDayOfWeek;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_month, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Month month = months.get(position);
        final View view = holder.view;

        TextView monthName = (TextView) view.findViewById(R.id.month_name);
        monthName.setText(month.getName());

        GridView headerView = (GridView) view.findViewById(R.id.month_header);
        int daysOfWeekArrayId = isMondayFirstDayOfWeek ? R.array.days_of_week_array : R.array.days_of_week_sunday_array;
        final ArrayAdapter<String> headerAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, context.getResources().getStringArray(daysOfWeekArrayId));
        headerView.setAdapter(headerAdapter);

        GridView gridView = (GridView) view.findViewById(R.id.month_grid);
        final MonthAdapter adapter = new MonthAdapter(context, android.R.layout.simple_list_item_1, month.getMarks());
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int markPosition, long id) {
                Mark item = adapter.getItem(markPosition);
                if (!(item instanceof Checkmark))
                    return;

                Checkmark checkmark = (Checkmark) item;
                checkmark.toggle();

                Checkmark.Value value = CheckmarkAdapter.clarifyCheckmarkValue(context, checkmark);
                checkmark.setValue(value);

                DbHelper dbHelper = new DbHelper(context);
                dbHelper.store(checkmark);

                int max = markPosition + 7 > adapter.getCount() ? adapter.getCount() : markPosition + 7;
                for (int i = markPosition; i < max; i++) {
                    Mark mark = adapter.getItem(i);
                    if (!(mark instanceof Checkmark))
                        continue;

                    Checkmark ch = (Checkmark) mark;

                    Checkmark.Value v = CheckmarkAdapter.clarifyCheckmarkValue(context, ch);
                    ch.setValue(v);
                }

                int p = holder.getAdapterPosition();
                if (p - 1 >= 0) {
                    Month nextMonth = months.get(p - 1);
                    max = 13 > nextMonth.getMarks().size() ? nextMonth.getMarks().size() : 13;

                    for (int i = 0; i < max; i++) {
                        Mark mark = nextMonth.getMarks().get(i);
                        if (!(mark instanceof Checkmark))
                            continue;

                        Checkmark ch = (Checkmark) mark;

                        Checkmark.Value v = CheckmarkAdapter.clarifyCheckmarkValue(context, ch);
                        ch.setValue(v);
                    }

                    notifyDataSetChanged();
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return months.size();
    }
}
