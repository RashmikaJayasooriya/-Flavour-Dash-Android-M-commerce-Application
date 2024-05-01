package lk.flavourdash.admin.adapter;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.flavourdash.admin.listener.PortionPriceChangeListener;
import lk.flavourdash.admin.R;

public class PortionPriceAdapter extends RecyclerView.Adapter<PortionPriceAdapter.ViewHolder> {

    private List<Pair<String, Double>> portionPriceList;
    private PortionPriceChangeListener listener;

    public PortionPriceAdapter(List<Pair<String, Double>> portionPriceList, PortionPriceChangeListener listener) {
        this.portionPriceList = portionPriceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meal_portion_price, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Pair<String, Double> portionPrice = portionPriceList.get(position);
        holder.editTextPortion.setText(portionPrice.first);
        holder.editTextPrice.setText(String.valueOf(portionPrice.second));




        holder.imageButtonRemove.setOnClickListener(v -> {
            portionPriceList.remove(position);
            notifyDataSetChanged();
        });

        holder.editTextPortion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Pair<String, Double> currentPair = portionPriceList.get(position);
                Pair<String, Double> updatedPair = new Pair<>(charSequence.toString(), currentPair.second);
                listener.onPortionPriceChanged(position, updatedPair);
            }


            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        holder.editTextPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Pair<String, Double> currentPair = portionPriceList.get(position);
                try {
                    Pair<String, Double> updatedPair = new Pair<>(currentPair.first, Double.valueOf(charSequence.toString()));
                    listener.onPortionPriceChanged(position, updatedPair);
                } catch (NumberFormatException e) {
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return portionPriceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText editTextPortion;
        EditText editTextPrice;
        ImageButton imageButtonRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editTextPortion = itemView.findViewById(R.id.editTextPortion);
            editTextPrice = itemView.findViewById(R.id.editTextPrice);
            imageButtonRemove = itemView.findViewById(R.id.imageButtonRemove);
        }
    }

    public void clearData() {
        if (portionPriceList != null) {
            portionPriceList.clear();
            notifyDataSetChanged();
        }
    }
}
