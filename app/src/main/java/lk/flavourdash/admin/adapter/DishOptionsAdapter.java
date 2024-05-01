package lk.flavourdash.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.flavourdash.admin.R;

public class DishOptionsAdapter extends RecyclerView.Adapter<DishOptionsAdapter.ViewHolder> {

    private List<String> dishOptions;

    public DishOptionsAdapter(List<String> dishOptions) {
        this.dishOptions = dishOptions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_option_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.dishOption.setText(dishOptions.get(position));
        holder.closeButton.setOnClickListener(v -> {
            dishOptions.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return dishOptions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dishOption;
        ImageButton closeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dishOption =  itemView.findViewById(R.id.dishOptionTextView);
            closeButton=  itemView.findViewById(R.id.dishOptionCloseButton);
        }
    }

    public void clearData() {
        if (dishOptions != null) {
            dishOptions.clear();
            notifyDataSetChanged();
        }
    }
}
