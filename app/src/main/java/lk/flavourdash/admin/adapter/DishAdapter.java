package lk.flavourdash.admin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.ouattararomuald.slider.ImageSlider;
import com.ouattararomuald.slider.SliderAdapter;
import com.ouattararomuald.slider.loaders.glide.GlideImageLoaderFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import lk.flavourdash.admin.MainActivity;
import lk.flavourdash.admin.Model.Dish;
import lk.flavourdash.admin.listener.OnItemClickListener;
import lk.flavourdash.admin.R;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.MyViewHolder> {

    private ArrayList<Dish> data;
    private Context context;

    private OnItemClickListener listener;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    final GestureDetector gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    });

    public DishAdapter(ArrayList<Dish> data, Context context,OnItemClickListener listener) {
        this.data = data;
        this.context = context;
        this.listener = listener;
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    @Nonnull
    @Override
    public MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_view, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        Dish item = data.get(position);

        holder.dishName.setText(item.getName());
        holder.dishDescription.setText(item.getDescription());
        holder.dishCategory.setText(item.getCategory());
        holder.dishSubcategory.setText(item.getSubCategory());
        holder.dishRating.setText(item.getRating().toString());


        Log.i(MainActivity.class.getName(), "item.getPortionPrices()"+item.getPortionPrices());

        DishViewPriceViewAdapter dishViewPriceViewAdapter = new DishViewPriceViewAdapter(item.getPortionPrices(),context);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        holder.dishPricesViewRecyclerView.setLayoutManager(layoutManager);
        holder.dishPricesViewRecyclerView.setAdapter(dishViewPriceViewAdapter);

        DishViewOptionViewAdapter dishViewOptionViewAdapter=new DishViewOptionViewAdapter(item.getOptions());
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        holder.dishOptionsViewRecyclerView.setLayoutManager(layoutManager2);
        holder.dishOptionsViewRecyclerView.setAdapter(dishViewOptionViewAdapter);

        List<String> savedImages = item.getImages();
//        Toast.makeText(context, "item.getImages()" + item.getImages().size(), Toast.LENGTH_SHORT).show();
        List<String> imageUrls = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();

        for (int i = 0; i < savedImages.size(); i++) {
            Log.i(MainActivity.class.getName(), "savedImages.get(i)" + savedImages.get(i));
            storage.getReference("dish-images/" + savedImages.get(i)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.i(MainActivity.class.getName(), "uri" + uri);
                    try {
                        URL url = new URL(uri.toString());
                        imageUrls.add(url.toString());

                        if (imageUrls.size() == savedImages.size()) {
//                            Toast.makeText(context, "imageUrls" + imageUrls.size(), Toast.LENGTH_SHORT).show();
                            SliderAdapter sliderAdapter = new SliderAdapter(context, new GlideImageLoaderFactory(),imageUrls,descriptions,"1");
                            holder.sliderView.setAdapter(sliderAdapter);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        holder.dishAvailability.setOnCheckedChangeListener(null);
        holder.dishAvailability.setChecked(item.getAvailability());
        holder.dishAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked != item.getAvailability()) {
                        String documentId = item.getId();
                        DocumentReference userReference = firestore.collection("Dishes").document(documentId);

                        userReference.update("availability", isChecked)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "DocumentSnapshot successfully updated!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Error updating document", Toast.LENGTH_SHORT).show();
                                        Log.e("FirestoreUpdateError", "Error updating document", task.getException());
                                    }
                                });
                    }
                });

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    // A double tap
                    Dish dish = data.get(holder.getAdapterPosition());
                    Toast.makeText(context, dish.getName().toString(), Toast.LENGTH_SHORT).show();
                    listener.onItemClick(dish);
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

        TextView dishName;
        TextView dishDescription;
        TextView dishCategory;
        TextView dishSubcategory;
        TextView dishRating;
        MaterialSwitch dishAvailability;
        ImageSlider sliderView;
        RecyclerView dishPricesViewRecyclerView;
        RecyclerView dishOptionsViewRecyclerView;

        MyViewHolder(View itemView) {
            super(itemView);
            dishName=itemView.findViewById(R.id.dishName);
            dishDescription=itemView.findViewById(R.id.dishDescription);
            dishCategory=itemView.findViewById(R.id.dishCategory);
            dishSubcategory=itemView.findViewById(R.id.dishSubcategory);
            dishRating=itemView.findViewById(R.id.dishRating);
            dishAvailability=itemView.findViewById(R.id.dishAvailability);
            sliderView=itemView.findViewById(R.id.dish_image_slider);
            dishPricesViewRecyclerView=itemView.findViewById(R.id.dishPricesViewRecyclerView);
            dishOptionsViewRecyclerView=itemView.findViewById(R.id.dishOptionsViewRecyclerView);
        }
    }
}

