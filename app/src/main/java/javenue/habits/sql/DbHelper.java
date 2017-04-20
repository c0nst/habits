package javenue.habits.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javenue.habits.model.Checkmark;
import javenue.habits.model.Goal;

public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 10;
    private static final String DATABASE_NAME = "habits.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbContract.SQL_CREATE_GOAL);
        db.execSQL(DbContract.SQL_CREATE_CHECKMARK);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 7 && newVersion > 7) {
            db.execSQL("alter table " + DbContract.GoalEntry.TABLE
                    + " add column " + DbContract.GoalEntry.COLUMN_ALARM + " integer");
        }

        if (oldVersion <= 9 && newVersion > 9) {
            db.execSQL("alter table " + DbContract.GoalEntry.TABLE
                    + " add column " + DbContract.GoalEntry.COLUMN_TIMES + " integer");
            db.execSQL("alter table " + DbContract.GoalEntry.TABLE
                    + " add column " + DbContract.GoalEntry.COLUMN_PERIOD + " integer");
        }
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbContract.SQL_DROP_GOAL);
        db.execSQL(DbContract.SQL_DROP_CHECKMARK);

        onCreate(db);
    }

    public List<Goal> getGoals(boolean archived) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                DbContract.GoalEntry._ID,
                DbContract.GoalEntry.COLUMN_CREATED,
                DbContract.GoalEntry.COLUMN_TITLE,
                DbContract.GoalEntry.COLUMN_TIMES,
                DbContract.GoalEntry.COLUMN_PERIOD,
                DbContract.GoalEntry.COLUMN_ALARM,
                DbContract.GoalEntry.COLUMN_ARCHIVED
        };

        String whereClause = DbContract.GoalEntry.COLUMN_ARCHIVED + " = ?";

        String sortOrder = DbContract.GoalEntry._ID + " DESC";

        Cursor cursor = db.query(
                DbContract.GoalEntry.TABLE,                     // The table to query
                projection,                                     // The columns to return
                whereClause,                                    // The columns for the WHERE clause
                new String[] { archived ? "1" : "0" },          // The values for the WHERE clause
                null,                                           // don't group the rows
                null,                                           // don't filter by row groups
                sortOrder                                       // The sort order
        );

        List<Goal> goals = new ArrayList<>();
        while (cursor.moveToNext()){
            Goal goal = materializeGoal(cursor);
            goals.add(goal);
        }

        cursor.close();

        db.close();

        return goals;
    }

    public Goal getGoal(long goalId) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                DbContract.GoalEntry._ID,
                DbContract.GoalEntry.COLUMN_CREATED,
                DbContract.GoalEntry.COLUMN_TITLE,
                DbContract.GoalEntry.COLUMN_TIMES,
                DbContract.GoalEntry.COLUMN_PERIOD,
                DbContract.GoalEntry.COLUMN_ALARM,
                DbContract.GoalEntry.COLUMN_ARCHIVED
        };

        Cursor cursor = db.query(
                DbContract.GoalEntry.TABLE,                 // The table to query
                projection,                                 // The columns to return
                DbContract.GoalEntry._ID + " = ?",          // The columns for the WHERE clause
                new String[] { goalId + "" },               // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                null                                        // The sort order
        );

        if (!cursor.moveToNext())
            return null;

        Goal goal = materializeGoal(cursor);

        cursor.close();
        db.close();

        return goal;
    }

    public Goal getGoalWithNearestAlarm(int timeInMinutes) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();

            cursor = db.rawQuery("select * " +
                    " from " + DbContract.GoalEntry.TABLE +
                    " where " + DbContract.GoalEntry.COLUMN_ALARM + " > " + timeInMinutes +
                    " and " + DbContract.GoalEntry.COLUMN_ARCHIVED + " = 0" +
                    " and " + DbContract.GoalEntry.COLUMN_ALARM + " > -1" +
                    " order by " + DbContract.GoalEntry.COLUMN_ALARM + " asc limit 1", null);

            if (cursor.moveToNext())
                return materializeGoal(cursor);

            cursor.close();

            cursor = db.rawQuery("select * " +
                    " from " + DbContract.GoalEntry.TABLE +
                    " where " + DbContract.GoalEntry.COLUMN_ARCHIVED + " = 0" +
                    " and " + DbContract.GoalEntry.COLUMN_ALARM + " > -1" +
                    " order by " + DbContract.GoalEntry.COLUMN_ALARM + " asc limit 1", null);

            if (cursor.moveToNext())
                return materializeGoal(cursor);

            return null;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private Goal materializeGoal(Cursor cursor) {
        Goal goal = new Goal();
        goal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DbContract.GoalEntry._ID)));
        goal.setCreated(cursor.getString(cursor.getColumnIndexOrThrow(DbContract.GoalEntry.COLUMN_CREATED)));
        goal.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DbContract.GoalEntry.COLUMN_TITLE)));
        goal.setTimes(cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.GoalEntry.COLUMN_TIMES)));
        goal.setPeriod(cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.GoalEntry.COLUMN_PERIOD)));
        goal.setAlarm(cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.GoalEntry.COLUMN_ALARM)));
        goal.setArchived(cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.GoalEntry.COLUMN_ARCHIVED)) > 0);

        return goal;
    }

    public void store(Goal goal) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.GoalEntry.COLUMN_CREATED, goal.getCreated());
        values.put(DbContract.GoalEntry.COLUMN_TITLE, goal.getTitle());
        values.put(DbContract.GoalEntry.COLUMN_TIMES, goal.getTimesConsideringDefault());
        values.put(DbContract.GoalEntry.COLUMN_PERIOD, goal.getPeriodConsideringDefault());
        values.put(DbContract.GoalEntry.COLUMN_ALARM, goal.getAlarm());
        values.put(DbContract.GoalEntry.COLUMN_ARCHIVED, goal.isArchived());

        if (goal.getId() > 0) {
            db.update(DbContract.GoalEntry.TABLE,
                    values,
                    DbContract.GoalEntry._ID + " = ?",
                    new String[] { goal.getId() + "" });
        } else {
            long id = db.insert(DbContract.GoalEntry.TABLE, null, values);
            goal.setId(id);
        }

        db.close();
    }

    public boolean delete(long goalId) {
        SQLiteDatabase db = getWritableDatabase();

        String whereClause = DbContract.GoalEntry._ID + " = ?";
        String[] args = { goalId + "" };

        int deletedCount = db.delete(DbContract.GoalEntry.TABLE, whereClause, args);
        if (deletedCount > 0) {
            whereClause = DbContract.CheckmarkEntry.COLUMN_GOAL_ID + " = ?";
            db.delete(DbContract.CheckmarkEntry.TABLE, whereClause, args);
        }
        db.close();

        return deletedCount > 0;
    }

    public List<Checkmark> getCheckmarks(Goal goal) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                DbContract.CheckmarkEntry._ID,
                DbContract.CheckmarkEntry.COLUMN_GOAL_ID,
                DbContract.CheckmarkEntry.COLUMN_CHECKMARK_DATE,
                DbContract.CheckmarkEntry.COLUMN_VALUE
        };

        String where = null;
        String[] values = null;
        String sortOrder = null;
        if (goal != null) {
            where = DbContract.CheckmarkEntry.COLUMN_GOAL_ID + " = ?";
            values = new String[] { goal.getId() + "" };
            sortOrder = DbContract.CheckmarkEntry.COLUMN_CHECKMARK_DATE;
        }

        Cursor cursor = db.query(
                DbContract.CheckmarkEntry.TABLE,
                projection,
                where,
                values,
                null,
                null,
                sortOrder
        );

        List<Checkmark> checkmarks = new ArrayList<>();
        while (cursor.moveToNext()){
            Checkmark checkmark = materializeCheckmark(cursor);
            checkmarks.add(checkmark);
        }

        cursor.close();

        db.close();

        return checkmarks;
    }

    public Map<String, Checkmark> getCheckmarksMap(Goal goal) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                DbContract.CheckmarkEntry._ID,
                DbContract.CheckmarkEntry.COLUMN_GOAL_ID,
                DbContract.CheckmarkEntry.COLUMN_CHECKMARK_DATE,
                DbContract.CheckmarkEntry.COLUMN_VALUE
        };

        String whereClause = DbContract.CheckmarkEntry.COLUMN_GOAL_ID + " = ? and " + DbContract.CheckmarkEntry.COLUMN_VALUE + " = ?";
        String[] params = new String[] { goal.getId() + "", "1"};
        String sortOrder = DbContract.GoalEntry._ID + " DESC";

        Cursor cursor = db.query(
                DbContract.CheckmarkEntry.TABLE,
                projection,
                whereClause,
                params,
                null,
                null,
                sortOrder
        );

        Map<String, Checkmark> checkmarks = new TreeMap<>();
        while (cursor.moveToNext()){
            Checkmark checkmark = materializeCheckmark(cursor);
            checkmarks.put(checkmark.getText(), checkmark);
        }

        cursor.close();

        db.close();

        return checkmarks;
    }

    public Checkmark getCheckmark(Goal goal, String date) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                DbContract.CheckmarkEntry._ID,
                DbContract.CheckmarkEntry.COLUMN_GOAL_ID,
                DbContract.CheckmarkEntry.COLUMN_CHECKMARK_DATE,
                DbContract.CheckmarkEntry.COLUMN_VALUE
        };

        String sortOrder = DbContract.GoalEntry._ID + " DESC";

        Cursor cursor = db.query(
                DbContract.CheckmarkEntry.TABLE,
                projection,
                DbContract.CheckmarkEntry.COLUMN_GOAL_ID + " = ? and " + DbContract.CheckmarkEntry.COLUMN_CHECKMARK_DATE + " = ?",
                new String[] { goal.getId() + "", date },
                null,
                null,
                sortOrder
        );

        Checkmark checkmark;
        if (cursor.moveToNext()){
            checkmark = materializeCheckmark(cursor);
        } else {
            checkmark = new Checkmark();
            checkmark.setGoalId(goal.getId());
            checkmark.setText(date);
        }

        cursor.close();
        db.close();

        return checkmark;
    }

    @NonNull
    private Checkmark materializeCheckmark(Cursor cursor) {
        Checkmark checkmark = new Checkmark();
        checkmark.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DbContract.CheckmarkEntry._ID)));
        checkmark.setGoalId(cursor.getLong(cursor.getColumnIndexOrThrow(DbContract.CheckmarkEntry.COLUMN_GOAL_ID)));
        checkmark.setText(cursor.getString(cursor.getColumnIndexOrThrow(DbContract.CheckmarkEntry.COLUMN_CHECKMARK_DATE)));
        checkmark.setValue(Checkmark.Value.byValue(cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.CheckmarkEntry.COLUMN_VALUE))));
        return checkmark;
    }

    public void store(Checkmark checkmark) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.CheckmarkEntry.COLUMN_GOAL_ID, checkmark.getGoalId());
        values.put(DbContract.CheckmarkEntry.COLUMN_CHECKMARK_DATE, checkmark.getText());
        values.put(DbContract.CheckmarkEntry.COLUMN_VALUE, checkmark.getValue().getValue());

        if (checkmark.getId() > 0) {
            db.update(DbContract.CheckmarkEntry.TABLE,
                    values,
                    DbContract.CheckmarkEntry._ID + " = ?",
                    new String[] { checkmark.getId() + "" });
        } else {
            long id = db.insert(DbContract.CheckmarkEntry.TABLE, null, values);
            checkmark.setId(id);
        }

        db.close();
    }
}
