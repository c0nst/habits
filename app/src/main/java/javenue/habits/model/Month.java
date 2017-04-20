package javenue.habits.model;

import java.util.List;

public class Month {
    private String name;
    private List<Mark> marks;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<Mark> getMarks() {
        return marks;
    }

    public void setMarks(List<Mark> marks) {
        this.marks = marks;
    }
}
