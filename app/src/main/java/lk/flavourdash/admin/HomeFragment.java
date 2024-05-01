package lk.flavourdash.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;

import lk.flavourdash.admin.Model.Branch;
import lk.flavourdash.admin.Model.Category;
import lk.flavourdash.admin.Model.Dish;
import lk.flavourdash.admin.Model.Order;
import lk.flavourdash.admin.Model.SubCategory;
import lk.flavourdash.admin.Model.User;

public class HomeFragment extends Fragment {
    private FirebaseFirestore firebaseFirestore;
    private TextView noOfBranches;
    private TextView noOfCategories;
    private TextView noOfSubCategories;
    private TextView noOfDishes;
    private TextView noOfCustomers;
    private TextView noOfOrders;
    private int numberOfCategories = 0;
    private int numberOfSubCategories = 0;
    private int numberOfDishes = 0;
    private int numberOfUsers = 0;
    private int numberOfOrders = 0;
    private int numberOfBranches = 0;

    public static final String TAG = MainActivity.class.getName();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseFirestore = FirebaseFirestore.getInstance();

        noOfBranches = view.findViewById(R.id.noOfBranches);
        noOfCategories = view.findViewById(R.id.noOfCategories);
        noOfSubCategories = view.findViewById(R.id.noOfSubCategories);
        noOfDishes = view.findViewById(R.id.noOfDishes);
        noOfCustomers = view.findViewById(R.id.noOfCustomers);
        noOfOrders = view.findViewById(R.id.noOfOrders);
        categories();
        subCategories();
        dishes();
        customers();
        orders();
        branches();
    }


    @SuppressLint("SetTextI18n")
    private void categories() {
        firebaseFirestore.collection("category").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }
            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    switch (change.getType()) {
                        case ADDED:
                            numberOfCategories++;
                            break;
                        case REMOVED:
                            numberOfCategories--;
                            break;
                    }
                }

                noOfCategories.setText("No. of Categories: " + numberOfCategories);
                Log.d(TAG, "Number of Categories: " + numberOfCategories);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void subCategories() {
        firebaseFirestore.collection("subCategory").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }
            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    switch (change.getType()) {
                        case ADDED:
                            numberOfSubCategories++;
                            break;
                        case REMOVED:
                            numberOfSubCategories--;
                            break;
                    }
                }

                noOfSubCategories.setText("No. of Sub Categories: " + numberOfSubCategories);
                Log.d(TAG, "Number of Sub Categories: " + numberOfSubCategories);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void dishes() {
        firebaseFirestore.collection("Dishes").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }
            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    switch (change.getType()) {
                        case ADDED:
                            numberOfDishes++;
                            break;
                        case REMOVED:
                            numberOfDishes--;
                            break;
                    }
                }

                noOfDishes.setText("No. of Dishes: " + numberOfDishes);
                Log.d(TAG, "Number of Dishes: " + numberOfDishes);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void customers() {
        firebaseFirestore.collection("users").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }
            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    switch (change.getType()) {
                        case ADDED:
                            numberOfUsers++;
                            break;
                        case REMOVED:
                            numberOfUsers--;
                            break;
                    }
                }

                noOfCustomers.setText("No. of Users: " + numberOfUsers);
                Log.d(TAG, "Number of Users: " + numberOfUsers);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void orders() {
        firebaseFirestore.collection("Orders").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }
            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    switch (change.getType()) {
                        case ADDED:
                            numberOfOrders++;
                            break;
                        case REMOVED:
                            numberOfOrders--;
                            break;
                    }
                }

                noOfOrders.setText("No. of Orders: " + numberOfOrders);
                Log.d(TAG, "Number of Orders: " + numberOfOrders);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void branches() {
        firebaseFirestore.collection("restaurant_branches").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }
            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    switch (change.getType()) {
                        case ADDED:
                            numberOfBranches++;
                            break;
                        case REMOVED:
                            numberOfBranches--;
                            break;
                    }
                }

                noOfBranches.setText("No. of Branches: " + numberOfBranches);
                Log.d(TAG, "Number of Branches: " + numberOfBranches);
            }
        });
    }


}