package javenue.habits;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javenue.habits.model.Checkmark;
import javenue.habits.model.Goal;
import javenue.habits.sql.DbHelper;
import javenue.habits.util.Alarms;
import javenue.habits.util.Settings;

public class MainActivity extends AppCompatActivity {
    public static final String GOAL_ID = "goalId";

    private String[] tips;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView drawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(0,0,23,0);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        drawer = (NavigationView) findViewById(R.id.navigation_drawer);
        updateDrawerMaterialDimensions();
        drawer.setNavigationItemSelectedListener(new DrawerListener());
        drawer.getHeaderView(0).setBackgroundResource(R.drawable.sidenav_bg);

        tips = getBaseContext().getResources().getStringArray(R.array.tips_array);
        setTipsText();

        migrateCheckmarks();

        Alarms.adjust(this);

        Fragment currentFragment = switchToGoalsFragment();
        updateDrawerState(currentFragment instanceof GoalsFragment);
        updateToolbarTitle(currentFragment);
        updateDrawerSelectedItem(currentFragment);
        drawerToggle.syncState();
    }

    private void migrateCheckmarks() {
        if (Settings.isCheckmarksMigrated(this)) return;

        DbHelper db = new DbHelper(getBaseContext());
        List<Checkmark> checkmarks = db.getCheckmarks(null);
        try {
            for (Checkmark c : checkmarks) {
                if (c.getText() == null) continue;

                Date parsed = SimpleDateFormat.getDateInstance().parse(c.getText());
                c.setText(new SimpleDateFormat(Checkmark.DATE_FORMAT, Locale.US).format(parsed));
                db.store(c);
            }
        } catch (ParseException ignore) { }

        Settings.setCheckmarksMigrated(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }

        Fragment currentFragment = getCurrentFragment();
        updateDrawerState(currentFragment instanceof GoalsFragment);
        updateToolbarTitle(currentFragment);
        updateDrawerSelectedItem(currentFragment);
        drawerToggle.syncState();
    }

    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentByTag("tag");
    }

    private void updateDrawerMaterialDimensions() {
        final Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        ViewGroup.LayoutParams params = drawer.getLayoutParams();
        params.width = size.x - getResources().getDimensionPixelSize(R.dimen.toolbar_material_height);
        drawer.setLayoutParams(params);

        View headerView = drawer.getHeaderView(0);
        params = headerView.getLayoutParams();
        params.height = getResources().getDimensionPixelSize(R.dimen.drawer_material_height);
        headerView.setLayoutParams(params);
    }

    private void updateToolbarTitle(Fragment fragment) {
        if (fragment instanceof GoalsFragment) {
            setTitle(getResources().getString(R.string.action_my_goals));
            return;
        }
        if (fragment instanceof ArchiveFragment)
            setTitle(getResources().getString(R.string.action_archive));
        if (fragment instanceof SettingsFragment)
            setTitle(getResources().getString(R.string.action_settings));
        if (fragment instanceof HelpFragment)
            setTitle(getResources().getString(R.string.action_help));
        if (fragment instanceof AboutFragment)
            setTitle(getResources().getString(R.string.action_about));
    }

    private void updateDrawerSelectedItem(Fragment fragment) {
        Menu menu = drawer.getMenu();

        if (fragment instanceof GoalsFragment) {
            menu.getItem(0).setChecked(true);
            return;
        }

        if (fragment instanceof ArchiveFragment)
            menu.getItem(1).setChecked(true);

        if (fragment instanceof SettingsFragment)
            menu.getItem(2).setChecked(true);

        if (fragment instanceof HelpFragment)
            menu.getItem(3).setChecked(true);

        if (fragment instanceof AboutFragment)
            menu.getItem(4).setChecked(true);
    }

    private void setTipsText() {
        View drawerHeader = drawer.getHeaderView(0);
        TextView tipsView = (TextView) drawerHeader.findViewById(R.id.tips);
        tipsView.setText(tips[new Random().nextInt(3)]);
    }

    public void newGoal(View view) {
        newGoal((Goal) null);
    }

    public void newGoal(Goal goal) {
        Context context = getBaseContext();
        Intent intent = new Intent(context, EditGoalActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (goal != null)
            intent.putExtra(MainActivity.GOAL_ID, goal.getId());
        context.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        prepareMenuItems(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    private void prepareMenuItems(Menu menu) {
        if (menu == null) return;

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            if (item.getItemId() == R.id.action_unarchive) item.setVisible(false);
            if (item.getItemId() == R.id.action_edit) item.setVisible(false);
            if (item.getItemId() == R.id.action_archive) item.setVisible(false);
            if (item.getItemId() == R.id.action_delete) item.setVisible(false);

            if (item.getItemId() == R.id.action_add) item.setVisible(false);
            if (item.getItemId() == R.id.action_save) item.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment currentFragment;

        switch (item.getItemId()) {
            case android.R.id.home:
                currentFragment = switchToGoalsFragment();
                break;

            case R.id.action_add:
                newGoal((Goal) null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

        updateToolbarTitle(currentFragment);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class DrawerListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment currentFragment;

            switch (item.getItemId()) {
                case R.id.action_goals:
                    currentFragment = switchToGoalsFragment();
                    break;

                case R.id.action_archive:
                    currentFragment = switchToArchiveFragment();
                    break;

                case R.id.action_settings:
                    currentFragment = switchToSettingsFragment();
                    break;

                case R.id.action_about:
                    currentFragment = switchToAboutFragment();
                    break;

                case R.id.action_help:
                    currentFragment = switchToHelpFragment();
                    break;

                default:
                    return false;
            }

            updateDrawerState(currentFragment instanceof GoalsFragment);
            updateToolbarTitle(currentFragment);
            setTipsText();
            drawerLayout.closeDrawers();
            drawerToggle.syncState();
            return true;
        }
    }

    private Fragment switchToGoalsFragment() {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof GoalsFragment)
            return currentFragment;

        currentFragment = new GoalsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, currentFragment, "tag")
                .commit();

        return currentFragment;
    }

    private Fragment switchToArchiveFragment() {
        Fragment currentFragment = new ArchiveFragment();

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, currentFragment, "tag")
                .addToBackStack("main")
                .commit();

        return currentFragment;
    }

    private Fragment switchToSettingsFragment() {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof SettingsFragment)
            return currentFragment;

        currentFragment = new SettingsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, currentFragment, "tag")
                .addToBackStack("main")
                .commit();

        return currentFragment;
    }

    private Fragment switchToHelpFragment() {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof HelpFragment)
            return currentFragment;

        currentFragment = new HelpFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, currentFragment, "tag")
                .addToBackStack("main")
                .commit();

        return currentFragment;
    }

    private Fragment switchToAboutFragment() {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof AboutFragment)
            return currentFragment;

        currentFragment = new AboutFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, currentFragment, "tag")
                .addToBackStack("main")
                .commit();

        return currentFragment;
    }

    private void updateDrawerState(boolean enabled) {
        drawerToggle.setDrawerIndicatorEnabled(enabled);
//        drawerToggle.setHomeAsUpIndicator(enabled ? R.drawable.ic_menu_white_48dp : R.drawable.abc_ic_ab_back_material);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(!enabled);
            actionBar.setDisplayShowHomeEnabled(!enabled);
        }
    }
}