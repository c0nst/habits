package javenue.habits;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import javenue.habits.model.Goal;
import javenue.habits.sql.DbHelper;
import javenue.habits.util.Alarms;
import javenue.habits.util.Images;

public class EditGoalActivity extends AppCompatActivity implements TimePickerDialog.GoalDialogListener{
    private Goal goal;

    private EditText goalTitle;

    private LinearLayout customRepeat;

    private Spinner repeatSpinner;
    private ArrayAdapter<Repeat> repeatAdapter;

    private Spinner alarmSpinner;
    private ArrayAdapter<AlarmTime> alarmAdapter;

    private Spinner timesSpinner;
    private Spinner periodSpinner;
    private ImageView repeatWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_goal);

        initToolbar();

        extractViews();
        extractGoal();

        goalTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                goalTitle.getBackground().setColorFilter(
                        ContextCompat.getColor(getBaseContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                goalTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        customRepeat.setVisibility(View.GONE);
        repeatWarning.setVisibility(View.GONE);

        repeatAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, Repeat.values());
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(repeatAdapter);
        repeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Repeat repeat = repeatAdapter.getItem(position);
                 if (repeat == Repeat.CUSTOM) {
                     populateCustomRepeat();
                 } else {
                     customRepeat.setVisibility(View.GONE);
                 }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        alarmAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item);
        alarmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmSpinner.setAdapter(alarmAdapter);
        alarmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AlarmTime selectedTime = alarmAdapter.getItem(position);

                if (selectedTime == PredefinedTime.CUSTOM) {
                    TimePickerDialog dialog = new TimePickerDialog();

                    Bundle arguments = new Bundle();
                    arguments.putInt("alarmTime", getAlarmTime(goal));
                    dialog.setArguments(arguments);

                    dialog.show(getSupportFragmentManager(), "Fragment");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        timesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                repeatWarning.setVisibility(View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adjustTimesSpinner();

                repeatWarning.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (goal != null) {
            goalTitle.setText(goal.getTitle());

            Repeat repeat = Repeat.byRepeats(goal.getTimesConsideringDefault(), goal.getPeriodConsideringDefault());
            int position = repeatAdapter.getPosition(repeat);
            repeatSpinner.setSelection(position);

            if (repeat == Repeat.CUSTOM) {
                populateCustomRepeat();
            }

            populateAlarmTime();
        } else {
            goal = new Goal();
            alarmAdapter.addAll(PredefinedTime.values());
        }
    }

    private void adjustTimesSpinner() {
        int position;
        int times = extractSpinnerValue(timesSpinner);
        int period = extractSpinnerValue(periodSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 1; i < period; i++) {
            adapter.add(i + "");
        }
        timesSpinner.setAdapter(adapter);

        if (times >= period)
            times = period - 1;

        position = adapter.getPosition(times + "");
        timesSpinner.setSelection(position);
    }

    private void populateAlarmTime() {
        int position;AlarmTime alarmTime = PredefinedTime.byMinutes(goal.getAlarm());
        if (alarmTime == PredefinedTime.CUSTOM) {
            CustomTime customTime = new CustomTime(goal.getAlarm());
            alarmAdapter.clear();
            alarmAdapter.add(customTime);
            alarmAdapter.addAll(PredefinedTime.values());
        } else {
            alarmAdapter.clear();
            alarmAdapter.addAll(PredefinedTime.values());
            position = alarmAdapter.getPosition(alarmTime);
            alarmSpinner.setSelection(position);
        }
    }

    private int getAlarmTime(Goal goal) {
        if (goal == null || goal.getAlarm() == -1)
            return 18 * 60;

        return goal.getAlarm();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.title_edit_goal));
        }
    }

    private void extractGoal() {
        Intent intent = getIntent();
        long goalId = intent.getLongExtra(MainActivity.GOAL_ID, 0);

        DbHelper dbHelper = new DbHelper(getBaseContext());
        goal = dbHelper.getGoal(goalId);
    }

    private void extractViews() {
        goalTitle = (EditText) findViewById(R.id.goalTitle);

        customRepeat = (LinearLayout) findViewById(R.id.customRepeat);

        timesSpinner = (Spinner) findViewById(R.id.spinner_repeat_times);
        periodSpinner = (Spinner) findViewById(R.id.spinner_repeat_period);

        repeatWarning = (ImageView) findViewById(R.id.repeat_warning);

        repeatSpinner = (Spinner) findViewById(R.id.repeatSpinner);
        alarmSpinner = (Spinner) findViewById(R.id.alarmSpinner);
    }

    private void populateCustomRepeat() {
        customRepeat.setVisibility(View.VISIBLE);

        timesSpinner.setSelection(goal.getTimesConsideringDefault() - 1);
        periodSpinner.setSelection(goal.getPeriodConsideringDefault() - 2);

        adjustTimesSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            if (item.getItemId() == R.id.action_archive) item.setVisible(false);
            if (item.getItemId() == R.id.action_unarchive) item.setVisible(false);
            if (item.getItemId() == R.id.action_add) item.setVisible(false);
            if (item.getItemId() == R.id.action_delete) item.setVisible(false);

            if (item.getItemId() == R.id.action_edit) item.setVisible(false);
            if (item.getItemId() == R.id.action_save) item.setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final Context context = getBaseContext();

        if (id == R.id.action_save) {
            if (!isFormDataValid()) {
                displayErrors(context);

                return true;
            }

            storeGoal();
            Alarms.adjust(context);
            finish();

            return true;
        }

        if (id == android.R.id.home) {
            if (hasUnsavedChanges()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(R.string.warning_edit_goal))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.action_exit, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }})
                        .setNegativeButton(android.R.string.cancel, null);

                if (isFormDataValid())
                    builder.setNeutralButton(R.string.action_save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            storeGoal();
                            Alarms.adjust(context);
                            finish();
                        }
                    });

                builder.show();
            } else {
                finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayErrors(Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_alert);
        drawable = Images.scaleImage(context, drawable, 0.5f);
        Images.setTintColor(context, drawable, R.color.colorAccent);

        if (this.goalTitle.getText().toString().isEmpty()) {
            int colorAccent = ContextCompat.getColor(context, R.color.colorAccent);
            goalTitle.getBackground().setColorFilter(colorAccent, PorterDuff.Mode.SRC_ATOP);
            goalTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }

        int repeatTimes = extractSpinnerValue(timesSpinner);
        int repeatPeriod = extractSpinnerValue(periodSpinner);
        if (repeatTimes > repeatPeriod) {
            repeatWarning.setImageDrawable(drawable);
            repeatWarning.setVisibility(View.VISIBLE);
        }
    }

    private boolean isFormDataValid() {
        String goalTitle = this.goalTitle.getText().toString();
        int repeatTimes = extractSpinnerValue(timesSpinner);
        int repeatPeriod = extractSpinnerValue(periodSpinner);

        return !goalTitle.isEmpty() && repeatTimes <= repeatPeriod;
    }

    private boolean hasUnsavedChanges() {
        if (goal.getId() == 0)
            return true;

        AlarmTime alarmTime = (AlarmTime) alarmSpinner.getSelectedItem();

        int repeatTimes, repeatPeriod;
        Repeat repeat = (Repeat) repeatSpinner.getSelectedItem();
        if (repeat == Repeat.CUSTOM) {
            repeatTimes = extractSpinnerValue(timesSpinner);
            repeatPeriod = extractSpinnerValue(periodSpinner);
        } else {
            repeatTimes = repeat.getTimes();
            repeatPeriod = repeat.getPeriod();
        }

        return !goal.getTitle().equals(goalTitle.getText().toString())
                || goal.getAlarm() != alarmTime.getMinutes()
                || goal.getTimesConsideringDefault() != repeatTimes
                || goal.getPeriodConsideringDefault() != repeatPeriod;
    }

    private void storeGoal() {
        goal.setTitle(goalTitle.getText().toString());

        Repeat repeat = (Repeat) repeatSpinner.getSelectedItem();
        if (repeat == Repeat.CUSTOM) {
            goal.setTimes(extractSpinnerValue(timesSpinner));
            goal.setPeriod(extractSpinnerValue(periodSpinner));
        } else {
            goal.setTimes(repeat.getTimes());
            goal.setPeriod(repeat.getPeriod());
        }

        AlarmTime alarmTime = (AlarmTime) alarmSpinner.getSelectedItem();
        goal.setAlarm(alarmTime.getMinutes());

        new DbHelper(getBaseContext()).store(goal);
    }

    private int extractSpinnerValue(Spinner spinner) {
        try {
            return Integer.valueOf((String) spinner.getSelectedItem());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onDialogPositive(DialogFragment dialog) {
        TimePicker timePicker = (TimePicker) dialog.getDialog().findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        int hour, minute;

        if (Build.VERSION.SDK_INT >= 23) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        int minutes = hour * 60 + minute;
        alarmAdapter.clear();
        alarmAdapter.add(new CustomTime(minutes));
        alarmAdapter.addAll(PredefinedTime.values());
        alarmAdapter.notifyDataSetChanged();
        alarmSpinner.setSelection(0);
    }

    @Override
    public void onDialogNegative(DialogFragment dialog) {
        populateAlarmTime();
    }

    private enum Repeat {
        EVERY_DAY("Every day", 1, 1),
        EVERY_WEEK("Every week", 1, 7),
        TWO_TIMES("2 times a week", 2, 7),
        THREE_TIMES("3 times a week", 3, 7),
        FIVE_TIMES("5 times a week", 5, 7),
        CUSTOM("Custom…", 0, 0);

        private String title;
        private int times;
        private int period;

        Repeat(String title, int times, int period) {
            this.title = title;
            this.times = times;
            this.period = period;
        }

        public String getTitle() {
            return title;
        }

        public int getTimes() {
            return times;
        }

        public int getPeriod() {
            return period;
        }

        public static Repeat byRepeats(int times, int period) {
            for (Repeat r : values())
                if (r.times == times && r.period == period)
                    return r;
            return CUSTOM;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    interface AlarmTime {
        String getTitle();
        int getMinutes();
    }

    private class CustomTime implements AlarmTime {
        private String title;
        private int minutes;

        CustomTime(int minutes) {
            this.title = Alarms.minutesToAlarmString(getBaseContext(), minutes);
            this.minutes = minutes;
        }

        public String getTitle() {
            return title;
        }

        public int getMinutes() {
            return minutes;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private enum PredefinedTime implements AlarmTime {
        EVERY_DAY("Off", -1),
        EVERY_WEEK("Morning", 9 * 60),
        TWO_TIMES("Afternoon", 13 * 60),
        THREE_TIMES("Evening", 18 * 60),
        CUSTOM("Custom…", 0);

        private String title;
        private int minutes;

        PredefinedTime(String title, int minutes) {
            this.title = title;
            this.minutes = minutes;
        }

        public String getTitle() {
            return title;
        }

        public int getMinutes() {
            return minutes;
        }

        public static PredefinedTime byMinutes(int minutes) {
            for (PredefinedTime r : values())
                if (r.minutes == minutes)
                    return r;
            return CUSTOM;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
