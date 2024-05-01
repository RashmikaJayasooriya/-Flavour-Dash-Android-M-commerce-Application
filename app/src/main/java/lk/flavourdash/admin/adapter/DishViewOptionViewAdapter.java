package lk.flavourdash.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.flavourdash.admin.R;

public class DishViewOptionViewAdapter extends RecyclerView.Adapter<DishViewOptionViewAdapter.ViewHolder> {

    private List<String> dishOptions;

    public DishViewOptionViewAdapter(List<String> dishOptions) {
        this.dishOptions = dishOptions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_view_option_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(dishOptions.get(position));
    }

    @Override
    public int getItemCount() {
        return dishOptions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.dishViewOptionName);
        }
    }

    public void clearData() {
        if (dishOptions != null) {
            dishOptions.clear();
            notifyDataSetChanged();
        }
    }
}
