package javenue.habits;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javenue.habits.model.Checkmark;
import javenue.habits.model.Mark;

class MonthAdapter extends ArrayAdapter<Mark> {
    MonthAdapter(Context context, int resource, List<Mark> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);

        Mark mark = getItem(position);

        int color = 0x00FFFFFF;
        if (mark instanceof Checkmark) {
            view.setTextColor(0xFFFFFFFF);

            color = ContextCompat.getColor(getContext(), R.color.colorGray);

            Checkmark checkmark = (Checkmark) mark;
            if (checkmark.getValue() == Checkmark.Value.CHECKED) {
                color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            }
            if (checkmark.getValue() == Checkmark.Value.CHECKED_IMPLICIT) {
                color = ContextCompat.getColor(getContext(), R.color.colorPrimaryLight);
            }

            if (checkmark.getText().equals(new SimpleDateFormat(Checkmark.DATE_FORMAT, Locale.US).format(new Date())))
                view.setCompoundDrawablesWithIntrinsicBounds(null, null, null, ContextCompat.getDrawable(getContext(), R.drawable.current_day_underline));
        } else {
            view.setClickable(false);
        }

        view.setBackgroundColor(color);
        view.setPadding(0,0,0,0);
        view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        view.setTextSize(18);

        return view;
    }
}
