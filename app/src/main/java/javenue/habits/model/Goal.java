package javenue.habits.model;

import android.support.annotation.NonNull;

public class Goal implements Comparable<Goal> {
    private long id;
    private String created;
    private String title;

    private int times;
    private int period;

    private int alarm = -1;

    private boolean archived;

    private int weight;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }
    public void setCreated(String created) {
        this.created = created;
    }

    public int getTimesConsideringDefault() {
        return times < 1 ? 1 : times;
    }
    public void setTimes(int times) {
        this.times = times;
    }

    public int getPeriodConsideringDefault() {
        return period < 1 ? 1 : period;
    }
    public void setPeriod(int period) {
        this.period = period;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public int getAlarm() {
        return alarm;
    }
    public void setAlarm(int alarm) {
        this.alarm = alarm;
    }

    public boolean isArchived() {
        return archived;
    }
    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    private int getWeight() {
        return weight;
    }
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int compareTo(@NonNull Goal goal) {
        return -Integer.valueOf(weight).compareTo(goal.getWeight());
    }
}
