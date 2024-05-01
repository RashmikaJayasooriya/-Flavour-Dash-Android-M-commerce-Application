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
import androidx.fragment.app.DialogFragment;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
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

public class DishUpdateFragment extends DialogFragment implements PortionPriceChangeListener {
    public static final String TAG = MainActivity.class.getName();
    private Dish dish;
    private PortionPriceAdapter portionPriceAdapter;
    private DishOptionsAdapter madapter;
    List<String> dishOptions = new ArrayList<>();
    public static List<Pair<String, Double>> portionPriceList = new ArrayList<>();
    private ImageAdapter adapter;
    private ArrayList<Uri> selectedImages = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private ArrayAdapter<String> selectadapter;
    Map<String, Double> portionPrices = new HashMap<>();
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;


    public static DishUpdateFragment newInstance(Dish dish) {
        DishUpdateFragment fragment = new DishUpdateFragment();
        Bundle args = new Bundle();
        args.putSerializable("dish", dish);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dish_update, container, false);

        Button closeButton = view.findViewById(R.id.closeDishUpdateBtn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            dish = (Dish) getArguments().getSerializable("dish");

            EditText dishNameUpdate = view.findViewById(R.id.dishNameUpdateEditText);
            EditText dishDescriptionUpdate = view.findViewById(R.id.dishDescriptionUpdateEditText);
            EditText dishCategoryUpdate = view.findViewById(R.id.dishSelectUpdateCategory);
            EditText dishSubCategoryUpdate = view.findViewById(R.id.dishSelectUpdateSubCategory);

            dishNameUpdate.setText(dish.getName());
            dishDescriptionUpdate.setText(dish.getDescription());
            dishCategoryUpdate.setText(dish.getCategory());
            dishSubCategoryUpdate.setText(dish.getSubCategory());

//        portion price
            RecyclerView recyclerViewPortionPrice = view.findViewById(R.id.recyclerViewUpdatePortionPrice);
            portionPriceList = convertMapToList(dish.getPortionPrices());
            portionPriceAdapter = new PortionPriceAdapter(portionPriceList, this);
            recyclerViewPortionPrice.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewPortionPrice.setAdapter(portionPriceAdapter);

//        meal options

            RecyclerView dishOptionsRecyclerView = view.findViewById(R.id.dishOptionsUpdateRecyclerView);
            dishOptions = dish.getOptions();
            madapter = new DishOptionsAdapter(dishOptions);
            dishOptionsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            dishOptionsRecyclerView.setAdapter(madapter);

//        Image
            RecyclerView recyclerView = view.findViewById(R.id.dishImagesUpdateRecyclerView);
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            recyclerView.setLayoutManager(layoutManager);

            List imageList = new ArrayList();
            imageList = dish.getImages();

            List finalImageList = imageList;
            for (int i = 0; i < imageList.size(); i++) {
                StorageReference reference = FirebaseStorage.getInstance().getReference("dish-images/" + imageList.get(i));

                File localFile = null;
                try {
                    localFile = File.createTempFile("images", "jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (localFile != null) {
                    File finalLocalFile = localFile;
                    reference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Uri localUri = Uri.fromFile(finalLocalFile);
                            selectedImages.add(localUri);

                            if (selectedImages.size() == finalImageList.size()) {
                                // Proceed with the code that depends on selectedImages
                                Toast.makeText(getContext(), "Images downloaded: " + selectedImages.size(), Toast.LENGTH_SHORT).show();
                                // Other code using selectedImages can go here
                                adapter = new ImageAdapter(selectedImages);
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailurepppppp: ", e);
                        }
                    });
                }
            }

        }

        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        //        Category Select
        AutoCompleteTextView autoCompleteCatTextView = view.findViewById(R.id.dishSelectUpdateCategory);

        firebaseFirestore.collection("category").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(MainActivity.class.getName(), "Error getting documents: ", error);
                    return;
                }

                if (value != null && !value.isEmpty()) {
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
        AutoCompleteTextView autoCompleteSubCatTextView = view.findViewById(R.id.dishSelectUpdateSubCategory);
        firebaseFirestore.collection("subCategory").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(MainActivity.class.getName(), "Error getting documents: ", error);
                    return;
                }

                if (value != null && !value.isEmpty()) {
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
        RecyclerView recyclerViewPortionPrice = view.findViewById(R.id.recyclerViewUpdatePortionPrice);
        portionPriceAdapter = new PortionPriceAdapter(portionPriceList, this);
        recyclerViewPortionPrice.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPortionPrice.setAdapter(portionPriceAdapter);

        Button addButton = view.findViewById(R.id.dishPortionPriceUpdateAddButton);
        addButton.setOnClickListener(v -> {
            // Add a new portion-price pair
            portionPriceList.add(new Pair<>("", 0.0));
            portionPriceAdapter.notifyItemInserted(portionPriceList.size() - 1);
        });

//        meal options
        EditText dishOptionInput = view.findViewById(R.id.dishOptionUpdate);
        Button addDishOptionButton = view.findViewById(R.id.dishOptionUpdateAddButton);

        RecyclerView dishOptionsRecyclerView = view.findViewById(R.id.dishOptionsUpdateRecyclerView);
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
        RecyclerView recyclerView = view.findViewById(R.id.dishImagesUpdateRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ImageAdapter(selectedImages);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.selectDishUpdateImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });


//        Update
        Button updateDish = view.findViewById(R.id.updateDish);
        updateDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText dName = view.findViewById(R.id.dishNameUpdateEditText);
                EditText dDescription = view.findViewById(R.id.dishDescriptionUpdateEditText);
                EditText dCat = view.findViewById(R.id.dishSelectUpdateCategory);
                EditText dSubCat = view.findViewById(R.id.dishSelectUpdateSubCategory);
                updatePortionPricesMap();

                String dishName = dName.getText().toString();
                String dishDescription = dDescription.getText().toString();
                String dishCategory = dCat.getText().toString();
                String dishSubCategory = dSubCat.getText().toString();

                List<String> imageIds = new ArrayList<>();
                imageIds = dish.getImages();

                if (dishName.isEmpty() || dishDescription.isEmpty() || dishCategory.isEmpty() || dishSubCategory.isEmpty() || portionPrices.isEmpty() || dishOptions.isEmpty() || imageIds.isEmpty()) {
                    Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(selectedImages.size()>imageIds.size()){
                    for (int i = 0; i < selectedImages.size()-imageIds.size(); i++) {
                        imageIds.add(UUID.randomUUID().toString());
                    }
                }else {
                    for (int j = 0; j < imageIds.size() - selectedImages.size(); j--) {
                        imageIds.remove(imageIds.size() - 1);
                    }
                }

                Dish updatedDish = new Dish(dishCategory, dishSubCategory, dishName, dishDescription, portionPrices, 0.0, false, dishOptions, imageIds);

                CircularProgressIndicator progressBar = view.findViewById(R.id.dishUpdateProgressBar);
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                updateDish.setEnabled(false);

                List<String> finalImageIds = imageIds;
                firebaseFirestore.collection("Dishes").document(dish.getId())
                        .set(updatedDish).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                if (selectedImages.size() > 0) {
                                    for (int i = 0; i < selectedImages.size(); i++) {
                                        progressBar.setIndeterminate(false);
                                        progressBar.setProgressCompat(0, true);

                                        StorageReference reference = storage.getReference("dish-images").child(finalImageIds.get(i));

                                        reference.putFile(selectedImages.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                progressBar.setVisibility(View.GONE);
                                                updateDish.setEnabled(true);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.GONE);
                                                updateDish.setEnabled(true);
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
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                updateDish.setEnabled(true);
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

    private static List<Pair<String, Double>> convertMapToList(Map<String, Double> map) {
        List<Pair<String, Double>> pairList = new ArrayList<>();

        for (Map.Entry<String, Double> entry : map.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            pairList.add(new Pair<>(key, value));
        }

        return pairList;
    }

    @Override
    public void onPortionPriceChanged(int position, Pair<String, Double> updatedPair) {
        portionPriceList.set(position, updatedPair);
    }
}