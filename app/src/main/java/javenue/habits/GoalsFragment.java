package javenue.habits;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;

import javenue.habits.model.Goal;
import javenue.habits.util.Settings;

public class GoalsFragment extends ArchiveFragment {
    FloatingActionButton fab;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);

        ItemTouchHelper touchHelper = new ItemTouchHelper(new GoalItemTouchCallback());
        touchHelper.attachToRecyclerView(getGoalsListView());

        fab = (FloatingActionButton) view.findViewById(R.id.floating_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.newGoal((Goal) null);
            }
        });

        ColorStateList colorStateList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colorStateList = getResources().getColorStateList(R.color.colorPrimary, getContext().getTheme());
        } else {
            colorStateList = getResources().getColorStateList(R.color.colorPrimary);
        }
        DrawableCompat.setTintList(DrawableCompat.wrap(fab.getBackground()), colorStateList);

        getGoalsListView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                adjustActionButton(recyclerView);
            }
        });

        return view;
    }

    private void adjustActionButton(RecyclerView recyclerView) {
        if (recyclerView.computeVerticalScrollOffset() == 0) {
            fab.show();
        } else {
            if (fab.isShown()) {
                fab.hide();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        adjustActionButton(getGoalsListView());
    }

    protected int getLayoutId() {
        return R.layout.fragment_goals;
    }

    private class GoalItemTouchCallback extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            final int fromPosition = viewHolder.getAdapterPosition();
            final int toPosition = target.getAdapterPosition();

            if (fromPosition == toPosition)
                return true;

            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(getGoals(), i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(getGoals(), i, i - 1);
                }
            }

            getGoalsAdapter().notifyItemMoved(fromPosition, toPosition);
            Settings.storeGoalsPosition(getActivity(), getGoals());

            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) { }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}
