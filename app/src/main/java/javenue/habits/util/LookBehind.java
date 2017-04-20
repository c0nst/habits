package javenue.habits.util;

import java.util.ArrayList;
import java.util.List;

import javenue.habits.model.Checkmark;

public class LookBehind {
    private int times;
    private int period;

    private int checked;
    private List<Checkmark> list = new ArrayList<>();

    public LookBehind(int times, int period) {
        this.times = times;
        this.period = period;
    }

    public void push(Checkmark c) {
        if (period == 1)
            return;

        if (list.size() >= period - 1) {
            Checkmark removed = list.remove(0);
            if (removed.getValue().getValue() == 1)
                checked--;
        }

        list.add(c);

        if (c.getValue().getValue() == 1)
            checked++;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public boolean shouldBeImplicitlyChecked(Checkmark checkmark) {
        if (checkmark.getValue() != Checkmark.Value.UNCHECKED)
            return false;

//        int potential = period - 1 - list.size(); TODO
//        return checked >= times - potential;
        return checked >= times;
    }
}
