package lk.flavourdash.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MealOptionsAdapter extends RecyclerView.Adapter<MealOptionsAdapter.ViewHolder> {

    private List<String> mealOptions;

    public MealOptionsAdapter(List<String> mealOptions) {
        this.mealOptions = mealOptions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mealOption.setText(mealOptions.get(position));
    }

    @Override
    public int getItemCount() {
        return mealOptions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mealOption;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mealOption = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    public void clearData() {
        if (mealOptions != null) {
            mealOptions.clear();
            notifyDataSetChanged();
        }
    }
}
