package javenue.habits.sql;

import android.provider.BaseColumns;

public final class DbContract {
    private DbContract() {}

    public static class GoalEntry implements BaseColumns {
        public static final String TABLE = "goal";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_TIMES = "times";
        public static final String COLUMN_PERIOD = "preiod";
        public static final String COLUMN_ALARM = "alarm";
        public static final String COLUMN_ARCHIVED = "archived";
    }

    public static class CheckmarkEntry implements BaseColumns {
        public static final String TABLE = "checkmark";
        public static final String COLUMN_GOAL_ID = "goalId";
        public static final String COLUMN_CHECKMARK_DATE = "checkmarkDate";
        public static final String COLUMN_VALUE = "value";
    }

    static final String SQL_CREATE_GOAL =
            "CREATE TABLE " + GoalEntry.TABLE + " (" +
                    GoalEntry._ID + " INTEGER PRIMARY KEY," +
                    GoalEntry.COLUMN_CREATED + " TEXT," +
                    GoalEntry.COLUMN_TITLE + " TEXT," +
                    GoalEntry.COLUMN_TIMES + " INTEGER," +
                    GoalEntry.COLUMN_PERIOD + " INTEGER," +
                    GoalEntry.COLUMN_ALARM + " INTEGER," +
                    GoalEntry.COLUMN_ARCHIVED + " BOOLEAN)";

    static final String SQL_DROP_GOAL =
            "DROP TABLE IF EXISTS " + GoalEntry.TABLE;

    static final String SQL_CREATE_CHECKMARK =
            "CREATE TABLE " + CheckmarkEntry.TABLE + " (" +
                    CheckmarkEntry._ID + " INTEGER PRIMARY KEY," +
                    CheckmarkEntry.COLUMN_GOAL_ID + " INTEGER," +
                    CheckmarkEntry.COLUMN_CHECKMARK_DATE + " TEXT," +
                    CheckmarkEntry.COLUMN_VALUE + " INTEGER)";

    static final String SQL_DROP_CHECKMARK =
            "DROP TABLE IF EXISTS " + CheckmarkEntry.TABLE;
}
