package lk.flavourdash.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import lk.flavourdash.admin.Model.Dish;
import lk.flavourdash.admin.adapter.DishAdapter;
import lk.flavourdash.admin.listener.OnItemClickListener;

public class DishViewFragment extends Fragment implements OnItemClickListener<Dish> {
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<Dish> dishes;
    private DishAdapter dishAdapter;
    public static final String TAG = MainActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dish_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseFirestore = FirebaseFirestore.getInstance();
        dishes = new ArrayList<>();
        dishAdapter = new DishAdapter(dishes, getContext(),this);
        RecyclerView dishRecyclerView = view.findViewById(R.id.dishRecyclerView);
        dishRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dishRecyclerView.setAdapter(dishAdapter);
        setupFirestoreListener();
    }

    private void setupFirestoreListener() {
        firebaseFirestore.collection("Dishes").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }

            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    Dish dish = change.getDocument().toObject(Dish.class);
                    dish.setId(change.getDocument().getId());

                    switch (change.getType()) {
                        case ADDED:
                            dishes.add(dish);
                            break;
                        case MODIFIED:
                            updateModifiedDish(change);
                            break;
                        case REMOVED:
                            dishes.removeIf(i -> i.getId().equals(dish.getId()));
                            break;
                    }
                }

                dishAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateModifiedDish(DocumentChange change) {
        Dish updatedDish = change.getDocument().toObject(Dish.class);
        updatedDish.setId(change.getDocument().getId());

        for (int i = 0; i < dishes.size(); i++) {
            Dish existingDish = dishes.get(i);

            if (existingDish.getId().equals(updatedDish.getId())) {
                // Update only the fields that have changed
                if (!existingDish.getName().equals(updatedDish.getName())) {
                    existingDish.setName(updatedDish.getName());
                }
                if (!existingDish.getDescription().equals(updatedDish.getDescription())) {
                    existingDish.setDescription(updatedDish.getDescription());
                }
                if (!existingDish.getCategory().equals(updatedDish.getCategory())) {
                    existingDish.setCategory(updatedDish.getCategory());
                }
                if (!existingDish.getSubCategory().equals(updatedDish.getSubCategory())) {
                    existingDish.setSubCategory(updatedDish.getSubCategory());
                }
                if (!existingDish.getRating().equals(updatedDish.getRating())) {
                    existingDish.setRating(updatedDish.getRating());
                }
                if (!existingDish.getAvailability().equals(updatedDish.getAvailability())) {
                    existingDish.setAvailability(updatedDish.getAvailability());
                }

                if (!existingDish.getOptions().equals(updatedDish.getOptions())) {
                    existingDish.setOptions(updatedDish.getOptions());
                }

                if (!existingDish.getImages().equals(updatedDish.getImages())) {
                    existingDish.setImages(updatedDish.getImages());
                }

                if (!existingDish.getPortionPrices().equals(updatedDish.getPortionPrices())) {
                    existingDish.setPortionPrices(updatedDish.getPortionPrices());
                }

                dishAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onItemClick(Dish dish) {
        DishUpdateFragment dialog = DishUpdateFragment.newInstance(dish);
        dialog.show(getChildFragmentManager(), "FullScreenDialog");
    }
}