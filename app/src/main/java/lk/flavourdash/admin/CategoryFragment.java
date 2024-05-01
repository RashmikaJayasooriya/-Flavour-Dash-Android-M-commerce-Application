package lk.flavourdash.admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.UUID;

import lk.flavourdash.admin.Model.Category;
import lk.flavourdash.admin.adapter.CategoryAdapter;
import lk.flavourdash.admin.listener.OnItemClickListener;

public class CategoryFragment extends Fragment implements OnItemClickListener<Category> {

    private ImageButton imageButton;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private Uri imagePath;
    private ArrayList<Category> categories;
    public static final String TAG = MainActivity.class.getName();

    public CategoryFragment() {
        // empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageButton = view.findViewById(R.id.imageButton);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        categories = new ArrayList<>();
        RecyclerView categoryView = view.findViewById(R.id.categoryRecyclerView);
        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, getContext(), this);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        categoryView.setLayoutManager(linearLayoutManager);
        categoryView.setAdapter(categoryAdapter);

        firebaseFirestore.collection("category").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    Category category = change.getDocument().toObject(Category.class);
                    category.setId(change.getDocument().getId()); // Set the document ID here
                    switch (change.getType()) {
                        case ADDED:
                            categories.add(category);
                            break;
                        case MODIFIED:
                            Category updatedCategory = change.getDocument().toObject(Category.class);
                            updatedCategory.setId(change.getDocument().getId());

                            for (int i = 0; i < categories.size(); i++) {
                                Category existingCategory = categories.get(i);

                                if (existingCategory.getId().equals(updatedCategory.getId())) {
                                    if (!existingCategory.getName().equals(updatedCategory.getName())) {
                                        existingCategory.setName(updatedCategory.getName());
                                    }

                                    if (!existingCategory.getDescription().equals(updatedCategory.getDescription())) {
                                        existingCategory.setDescription(updatedCategory.getDescription());
                                    }

                                    if (!existingCategory.getImage().equals(updatedCategory.getImage())) {
                                        existingCategory.setImage(updatedCategory.getImage());
                                    }

                                    if (existingCategory.isActive() != updatedCategory.isActive()) {
                                        existingCategory.setActive(updatedCategory.isActive());
                                    }

                                    categoryAdapter.notifyItemChanged(i);

                                    break;
                                }
                            }
                            break;
                        case REMOVED:
                            categories.removeIf(i -> i.getId().equals(category.getId()));
                            break;
                    }
                }

                categoryAdapter.notifyDataSetChanged();
            }

        });


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));
            }
        });

        Button addCategory = view.findViewById(R.id.addCategory);
        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText catName = view.findViewById(R.id.categoryNameEditText);
                EditText catDescription = view.findViewById(R.id.categoryDescriptionEditText);

                String categoryName = catName.getText().toString();
                String categoryDescription = catDescription.getText().toString();

                if (categoryName.isEmpty() || categoryDescription.isEmpty() || imagePath == null) {
                    Toast.makeText(getContext(), "Category name, description, and image are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String imageId = UUID.randomUUID().toString();

                Category category = new Category(categoryName, categoryDescription, imageId, true);

                CircularProgressIndicator progressBar = view.findViewById(R.id.progressBar);
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                addCategory.setEnabled(false);

                firebaseFirestore.collection("category").add(category).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (imagePath != null) {
                            progressBar.setIndeterminate(false);
                            progressBar.setProgressCompat(0, true);

                            StorageReference reference = storage.getReference("category-images").child(imageId);
                            reference.putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressBar.setVisibility(View.GONE);
                                    addCategory.setEnabled(true);
                                    Toast.makeText(getContext(), "Success", Toast.LENGTH_LONG).show();
                                    catName.setText("");
                                    catDescription.setText("");
                                    imageButton.setImageResource(R.drawable.baseline_example_vector_24);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    addCategory.setEnabled(true);
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
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        addCategory.setEnabled(true);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    @Override
    public void onItemClick(Category category) {
        CategoryUpdateFragment dialog = CategoryUpdateFragment.newInstance(category);
        dialog.show(getChildFragmentManager(), "FullScreenDialog");
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri selectedImage = result.getData().getData();
                        imagePath = result.getData().getData();
                        Log.i(TAG, "Image Path: " + selectedImage.getPath());

                        Glide.with(getActivity())
                                .load(selectedImage)
                                .centerCrop()
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(imageButton);
                    }
                }
            });


}