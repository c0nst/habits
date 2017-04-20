package javenue.habits.model;

import android.support.annotation.NonNull;

public class Mark implements Comparable<Mark> {
    private String text = "";

    public Mark() { }

    Mark(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mark mark = (Mark) o;
        return text.equals(mark.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    public int compareTo(@NonNull Mark checkmark) {
        return text.compareTo(checkmark.getText());
    }

    @Override
    public String toString() {
        return text;
    }

}
