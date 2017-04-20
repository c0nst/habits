package javenue.habits;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Map;

import javenue.habits.model.Checkmark;
import javenue.habits.model.Goal;
import javenue.habits.sql.DbHelper;

public class ViewGoalActivity extends AppCompatActivity {
    Goal goal;
    Map<String, Checkmark> checkmarksMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_goal);

        extractGoal();
        initToolbar();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Calendar"));
        tabLayout.addTab(tabLayout.newTab().setText("Statistics"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void extractGoal() {
        Intent intent = getIntent();
        long goalId = intent.getLongExtra(MainActivity.GOAL_ID, 0);

        DbHelper dbHelper = new DbHelper(getBaseContext());
        goal = dbHelper.getGoal(goalId);

        checkmarksMap = dbHelper.getCheckmarksMap(goal);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(goal.getTitle());
        }
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

            if (item.getItemId() == R.id.action_unarchive) item.setVisible(goal.isArchived());
            if (item.getItemId() == R.id.action_edit) item.setVisible(true);
            if (item.getItemId() == R.id.action_archive) item.setVisible(!goal.isArchived());
            if (item.getItemId() == R.id.action_delete) item.setVisible(true);

            if (item.getItemId() == R.id.action_add) item.setVisible(false);
            if (item.getItemId() == R.id.action_save) item.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Context context = getBaseContext();
        int id = item.getItemId();

        if (id == R.id.action_unarchive) {

            goal.setArchived(false);
            new DbHelper(getBaseContext()).store(goal);

            finish();

            return true;
        }

        if (id == R.id.action_edit) {
            Intent intent = new Intent(context, EditGoalActivity.class);
            intent.putExtra(MainActivity.GOAL_ID, goal.getId());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            return true;
        }

        if (id == R.id.action_archive) {

            goal.setArchived(true);
            new DbHelper(context).store(goal);

            finish();

            return true;
        }

        if (id == R.id.action_delete) {

            new AlertDialog.Builder(this)
                    .setMessage("This will also delete all goal stats. Consider archiving the goal. Proceed anyway?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            boolean deleted = new DbHelper(context).delete(goal.getId());
                            finish();

                            if (deleted)
                                Toast.makeText(context, "Successfully deleted", Toast.LENGTH_SHORT).show();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
