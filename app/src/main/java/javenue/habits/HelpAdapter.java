package javenue.habits;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import javenue.habits.model.FaqEntry;
import javenue.habits.util.ViewHolder;

class HelpAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<FaqEntry> entries;

    HelpAdapter(List<FaqEntry> entries) {
        this.entries = entries;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_faq, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final FaqEntry entry = entries.get(position);
        final View view = holder.view;

        TextView question = (TextView) view.findViewById(R.id.faq_question);
        question.setText(entry.getQuestion());

        TextView answer = (TextView) view.findViewById(R.id.faq_answer);
        answer.setText(entry.getAnswer());
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }
}
