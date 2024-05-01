package lk.flavourdash.admin.adapter;


import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import lk.flavourdash.admin.MainActivity;
import lk.flavourdash.admin.Model.SubCategory;
import lk.flavourdash.admin.listener.OnItemClickListener;
import lk.flavourdash.admin.R;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.MyViewHolder> {

    private ArrayList<SubCategory> data;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private Context context;
    private OnItemClickListener listener;

//    private List<Category> categories;

    final GestureDetector gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    });

    public SubCategoryAdapter(ArrayList<SubCategory> data, Context context, OnItemClickListener listener) {
        this.data = data;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.listener = listener;
    }

    @Nonnull
    @Override
    public MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        SubCategory item = data.get(position);
        holder.categoryNameView.setText(item.getName());
        holder.categoryDescView.setText(item.getDescription());


        holder.categoryStatus.setOnCheckedChangeListener(null);
        holder.categoryStatus.setChecked(item.isActive());
        holder.categoryStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != item.isActive()) {
                String documentId = item.getId();
                DocumentReference categoryReference = firestore.collection("category").document(documentId);

                categoryReference.update("active", isChecked)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Update successful
                                Toast.makeText(context, "DocumentSnapshot successfully updated!", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle errors
                                Toast.makeText(context, "Error updating document", Toast.LENGTH_SHORT).show();
                                Log.e("FirestoreUpdateError", "Error updating document", task.getException());
                            }
                        });
            }
        });

        storage.getReference("subCategory-images/" + item.getImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(holder.categoryImageView.getContext()).load(uri).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).into(holder.categoryImageView);
            }
        });

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    // A double tap has occurred
                    SubCategory category = data.get(holder.getAdapterPosition());
                    Toast.makeText(context, category.getName().toString(), Toast.LENGTH_SHORT).show();
                    listener.onItemClick(category);
                }
                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView categoryImageView;
        TextView categoryNameView;
        TextView categoryDescView;
        MaterialSwitch categoryStatus;

        MyViewHolder(View itemView) {
            super(itemView);
            categoryImageView = itemView.findViewById(R.id.categoryImageView);
            categoryNameView = itemView.findViewById(R.id.categoryName);
            categoryDescView = itemView.findViewById(R.id.categoryDescription);
            categoryStatus = itemView.findViewById(R.id.categoryStatus);
        }
    }
}
