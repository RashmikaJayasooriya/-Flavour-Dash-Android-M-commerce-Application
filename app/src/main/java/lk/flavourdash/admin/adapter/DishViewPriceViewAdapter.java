package lk.flavourdash.admin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lk.flavourdash.admin.R;

public class DishViewPriceViewAdapter extends RecyclerView.Adapter<DishViewPriceViewAdapter.MyViewHolder> {

    private List<LinkedHashMap.Entry<String, Double>> data;
    private Context context;

    public DishViewPriceViewAdapter(Map<String, Double> data, Context context) {
        this.data = new java.util.ArrayList<>(data.entrySet());
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_view_price_view, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Map.Entry<String, Double> item = data.get(position);

        holder.dishPortionName.setText(item.getKey());
        holder.dishPortionPrice.setText(String.valueOf(item.getValue()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView dishPortionName;
        TextView dishPortionPrice;

        MyViewHolder(View itemView) {
            super(itemView);
            dishPortionName = itemView.findViewById(R.id.dishPortionName);
            dishPortionPrice = itemView.findViewById(R.id.dishPortionPrice);
        }
    }
}
