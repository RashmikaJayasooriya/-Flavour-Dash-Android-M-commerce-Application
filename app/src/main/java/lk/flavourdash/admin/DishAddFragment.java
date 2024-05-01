package lk.flavourdash.admin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lk.flavourdash.admin.Model.Dish;
import lk.flavourdash.admin.adapter.DishOptionsAdapter;
import lk.flavourdash.admin.adapter.ImageAdapter;
import lk.flavourdash.admin.adapter.PortionPriceAdapter;
import lk.flavourdash.admin.listener.PortionPriceChangeListener;


public class DishAddFragment extends Fragment implements PortionPriceChangeListener {

    public static List<Pair<String, Double>> portionPriceList = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private ArrayAdapter<String> selectadapter;
    Map<String, Double> portionPrices = new HashMap<>();
    private PortionPriceAdapter portionPriceAdapter;
    private DishOptionsAdapter madapter;
    List<String> dishOptions = new ArrayList<>();
    private ImageAdapter adapter;
    private ArrayList<Uri> selectedImages = new ArrayList<>();
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dish_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        firebaseFirestore= FirebaseFirestore.getInstance();
        storage=FirebaseStorage.getInstance();

//        Category Select
        AutoCompleteTextView autoCompleteCatTextView = view.findViewById(R.id.dishSelectCategory);


        firebaseFirestore.collection("category").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Handle errors
                    Log.e(MainActivity.class.getName(), "Error getting documents: ", error);
                    return;
                }

                if (value != null && !value.isEmpty()) {
                    // List to store retrieved categories
                    final List<String> categories = new ArrayList<>();

                    for (QueryDocumentSnapshot document : value) {
                        String categoryName = document.getString("name");
                        categories.add(categoryName);
                    }

                    selectadapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
                    autoCompleteCatTextView.setAdapter(selectadapter);
                }
            }
        });
//      SubCategory Select
        AutoCompleteTextView autoCompleteSubCatTextView = view.findViewById(R.id.dishSelectSubCategory);

        firebaseFirestore.collection("subCategory").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(MainActivity.class.getName(), "Error getting documents: ", error);
                    return;
                }

                if (value != null && !value.isEmpty()) {
                    // List to store retrieved categories
                    final List<String> subCategories = new ArrayList<>();

                    for (QueryDocumentSnapshot document : value) {
                        String subCategoryName = document.getString("name");
                        subCategories.add(subCategoryName);
                    }

                    selectadapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, subCategories);
                    autoCompleteSubCatTextView.setAdapter(selectadapter);
                }
            }
        });
//        portion price
        RecyclerView recyclerViewPortionPrice = view.findViewById(R.id.recyclerViewPortionPrice);
        portionPriceAdapter = new PortionPriceAdapter(portionPriceList,this);
        recyclerViewPortionPrice.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPortionPrice.setAdapter(portionPriceAdapter);

        Button addButton = view.findViewById(R.id.dishPortionPriceAddButton);
        addButton.setOnClickListener(v -> {
            // Add a new portion-price pair
            portionPriceList.add(new Pair<>("", 0.0));
            portionPriceAdapter.notifyItemInserted(portionPriceList.size() - 1);
        });

//        meal options
        EditText dishOptionInput = view.findViewById(R.id.dishOption);
        Button addDishOptionButton = view.findViewById(R.id.dishOptionAddButton);

        RecyclerView dishOptionsRecyclerView = view.findViewById(R.id.dishOptionsRecyclerView);
        madapter = new DishOptionsAdapter(dishOptions);
        dishOptionsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dishOptionsRecyclerView.setAdapter(madapter);

        DishOptionsAdapter finalAdapter = madapter;
        addDishOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mealOption = dishOptionInput.getText().toString();
                if (!mealOption.isEmpty()) {
                    dishOptions.add(mealOption);
                    finalAdapter.notifyDataSetChanged();
                    dishOptionInput.setText("");
                }
            }
        });

//        Image
        RecyclerView recyclerView = view.findViewById(R.id.dishImagesRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ImageAdapter(selectedImages);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.selectDishImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });


//        Save
        Button addDish = view.findViewById(R.id.addDish);
        addDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText dName = view.findViewById(R.id.dishNameEditText);
                EditText dDescription = view.findViewById(R.id.dishDescriptionEditText);
                EditText dCat = view.findViewById(R.id.dishSelectCategory);
                EditText dSubCat = view.findViewById(R.id.dishSelectSubCategory);
                updatePortionPricesMap();

                String dishName = dName.getText().toString();
                String dishDescription = dDescription.getText().toString();
                String dishCategory = dCat.getText().toString();
                String dishSubCategory = dSubCat.getText().toString();

                if (dishName.isEmpty() || dishDescription.isEmpty() || dishCategory.isEmpty() || dishSubCategory.isEmpty() || portionPrices.isEmpty() || selectedImages.isEmpty()) {
                    Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<String> imageIds = new ArrayList<>();
                for (int i = 0; i < selectedImages.size(); i++) {
                    imageIds.add(i, UUID.randomUUID().toString());
                }

                Dish dish = new Dish(dishCategory, dishSubCategory, dishName, dishDescription, portionPrices, 0.0, false, dishOptions, imageIds);

                CircularProgressIndicator progressBar = view.findViewById(R.id.dishProgressBar);
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                addDish.setEnabled(false);

                firebaseFirestore.collection("Dishes").add(dish).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (selectedImages.size() > 0) {
                            for (int i = 0; i < selectedImages.size(); i++) {
                                progressBar.setIndeterminate(false);
                                progressBar.setProgressCompat(0, true);

                                StorageReference reference = storage.getReference("dish-images").child(imageIds.get(i));

                                reference.putFile(selectedImages.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        progressBar.setVisibility(View.GONE);
                                        addDish.setEnabled(true);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        addDish.setEnabled(true);
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                        progressBar.setProgressCompat((int) progress, true);
                                    }
                                });
                            }
                        }

                        dName.setText("");
                        dDescription.setText("");
                        dCat.setText("");
                        dSubCat.setText("");
                        portionPrices.clear();
                        portionPriceAdapter.clearData();
                        dishOptions.clear();
                        madapter.clearData();
                        selectedImages.clear();
                        adapter.clearData();
                        imageIds.clear();

                        Toast.makeText(getContext(), "Dish added successfully!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        addDish.setEnabled(true);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    //    Permission
    void checkPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    //    Portion
    private void updatePortionPricesMap() {

        for (int i = 0; i < portionPriceList.size(); i++) {
            Pair<String, Double> portionPrice = portionPriceList.get(i);
            String portion = portionPrice.first;
            Double price = portionPrice.second;

            if (!TextUtils.isEmpty(portion) && price != null) {
                portionPrices.put(portion, price);

                Log.d(MainActivity.class.getName(), "Added Portion: " + portion + ", Price: " + price);
            }
        }
    }

    //    Image
    void pickImage() {
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage();
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            selectedImages.add(selectedImage);
            if (adapter != null) {
                adapter.setImageUris(selectedImages);
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(getActivity(), ImagePicker.Companion.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPortionPriceChanged(int position, Pair<String, Double> updatedPair) {
        portionPriceList.set(position, updatedPair);
    }
}