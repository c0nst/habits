package javenue.habits.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javenue.habits.model.Mark;

public class Checkmark extends Mark {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private long id;
    private long goalId;
    private Value value = Value.UNCHECKED;

    public Checkmark() { }

    public Checkmark(String text, long goalId) {
        super(text);
        this.goalId = goalId;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getGoalId() {
        return goalId;
    }
    public void setGoalId(long goalId) {
        this.goalId = goalId;
    }

    private String getDayNumber() {
        if ("".equals(getText())) return "";

        DateFormat format = new SimpleDateFormat(Checkmark.DATE_FORMAT, Locale.US);
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(format.parse(getText()));
            return calendar.get(Calendar.DAY_OF_MONTH) + "";
        } catch (ParseException e) {
            return "";
        }
    }

    public Value getValue() {
        return value;
    }
    public void setValue(Value value) {
        this.value = value;
    }

    public void toggle() {
        switch (value) {
            case UNCHECKED:
            case UNCHECKED_IMPLICIT:
            case CHECKED_IMPLICIT:
                value = Checkmark.Value.CHECKED;
                break;
            case CHECKED:
                value = Checkmark.Value.UNCHECKED;
                break;
        }
    }

    @Override
    public String toString() {
        return getDayNumber();
    }

    public enum Value {
        UNCHECKED(0),
        CHECKED(1),
        UNCHECKED_IMPLICIT(0),
        CHECKED_IMPLICIT(0);

        int value;

        public int getValue() {
            return value;
        }

        Value(int value) {
            this.value = value;
        }

        public static Value byValue(int value) {
            for (Value v : values())
                if (v.getValue() == value)
                    return v;
            return null;
        }
    }
}
