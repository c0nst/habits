package javenue.habits;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javenue.habits.model.FaqEntry;

public class HelpFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity context = getActivity();

        View view = inflater.inflate(R.layout.fragment_help, container, false);

        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.faq);

        HelpAdapter adapter = new HelpAdapter(getEntries());
        recycler.setAdapter(adapter);

        LinearLayoutManager layout = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(layout);

        return view;
    }

    private List<FaqEntry> getEntries() {
        String[] array = getResources().getStringArray(R.array.faq_array);

        List<FaqEntry> entries = new ArrayList<>();
        for (int i = 0; i < array.length / 2; i++) {
            FaqEntry entry = new FaqEntry();
            entry.setQuestion(array[i * 2]);
            entry.setAnswer(array[i * 2 + 1]);

            entries.add(entry);
        }
        return entries;
    }
}