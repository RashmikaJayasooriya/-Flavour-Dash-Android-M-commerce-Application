package lk.flavourdash.admin.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

import lk.flavourdash.admin.R;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<Uri> imageUris;

    public ImageAdapter(List<Uri> imageUris) {
        this.imageUris = imageUris;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_view_for_selects, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder.imageView != null) {
            Glide.with(holder.imageView.getContext())
                    .load(imageUris.get(position))
                    .into(holder.imageView);
        }

        holder.imageCloseBtn.setOnClickListener(v -> {
            imageUris.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton imageCloseBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView2);
            imageCloseBtn= itemView.findViewById(R.id.imageCloseBtn);
        }
    }

    public void setImageUris(List<Uri> imageUris) {
        this.imageUris = imageUris;
        notifyDataSetChanged();
    }

    public void clearData() {
        if (imageUris != null) {
            imageUris.clear();
            notifyDataSetChanged();
        }
    }
}


