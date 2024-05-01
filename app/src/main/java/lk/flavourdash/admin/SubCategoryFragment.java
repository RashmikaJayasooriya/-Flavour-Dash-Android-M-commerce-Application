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
import lk.flavourdash.admin.Model.SubCategory;
import lk.flavourdash.admin.adapter.SubCategoryAdapter;
import lk.flavourdash.admin.listener.OnItemClickListener;

public class SubCategoryFragment extends Fragment implements OnItemClickListener<SubCategory> {

    private ImageButton imageButton;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private Uri imagePath;
    private ArrayList<SubCategory> subCategories;
    public static final String TAG = MainActivity.class.getName();


    public SubCategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sub_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageButton = view.findViewById(R.id.subimageButton);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        subCategories = new ArrayList<>();
        RecyclerView subcategoryView = view.findViewById(R.id.subcategoryRecyclerView);
        SubCategoryAdapter subcategoryAdapter = new SubCategoryAdapter(subCategories, getContext(), this);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        subcategoryView.setLayoutManager(linearLayoutManager);
        subcategoryView.setAdapter(subcategoryAdapter);


        firebaseFirestore.collection("subCategory").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    SubCategory subCategory = change.getDocument().toObject(SubCategory.class);
                    subCategory.setId(change.getDocument().getId()); // Set the document ID here
                    switch (change.getType()) {
                        case ADDED:
                            subCategories.add(subCategory);
                            break;
                        case MODIFIED:
                            Category updatedCategory = change.getDocument().toObject(Category.class);
                            updatedCategory.setId(change.getDocument().getId());

                            for (int i = 0; i < subCategories.size(); i++) {
                                SubCategory existingSubCategory = subCategories.get(i);

                                if (existingSubCategory.getId().equals(updatedCategory.getId())) {

                                    if (!existingSubCategory.getName().equals(updatedCategory.getName())) {
                                        existingSubCategory.setName(updatedCategory.getName());
                                    }

                                    if (!existingSubCategory.getDescription().equals(updatedCategory.getDescription())) {
                                        existingSubCategory.setDescription(updatedCategory.getDescription());
                                    }

                                    if (!existingSubCategory.getImage().equals(updatedCategory.getImage())) {
                                        existingSubCategory.setImage(updatedCategory.getImage());
                                    }

                                    if (existingSubCategory.isActive() != updatedCategory.isActive()) {
                                        existingSubCategory.setActive(updatedCategory.isActive());
                                    }

                                    subcategoryAdapter.notifyItemChanged(i);

                                    break;
                                }
                            }
                            break;
                        case REMOVED:
                            subCategories.removeIf(i -> i.getId().equals(subCategory.getId()));
                            break;
                    }
                }

                subcategoryAdapter.notifyDataSetChanged();
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

        Button addSubCategory = view.findViewById(R.id.addSubCategory);
        addSubCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText subcatName = view.findViewById(R.id.subcategoryNameEditText);
                EditText subcatDescription = view.findViewById(R.id.subcategoryDescriptionEditText);

                String subcategoryName = subcatName.getText().toString();
                String subcategoryDescription = subcatDescription.getText().toString();

                if (subcategoryName.isEmpty() || subcategoryDescription.isEmpty() || imagePath == null) {
                    Toast.makeText(getContext(), "Sub-Category name, description, and image are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String imageId = UUID.randomUUID().toString();

                SubCategory subCategory = new SubCategory(subcategoryName, subcategoryDescription, imageId, true);

                CircularProgressIndicator subProgressBar = view.findViewById(R.id.subProgressBar);
                subProgressBar.setIndeterminate(true);
                subProgressBar.setVisibility(View.VISIBLE);
                addSubCategory.setEnabled(false);

                firebaseFirestore.collection("subCategory").add(subCategory).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (imagePath != null) {
                            subProgressBar.setIndeterminate(false);
                            subProgressBar.setProgressCompat(0, true);

                            StorageReference reference = storage.getReference("subCategory-images").child(imageId);
                            reference.putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    subProgressBar.setVisibility(View.GONE);
                                    addSubCategory.setEnabled(true);
                                    Toast.makeText(getContext(), "Success", Toast.LENGTH_LONG).show();
                                    subcatName.setText("");
                                    subcatDescription.setText("");
                                    imageButton.setImageResource(R.drawable.baseline_example_vector_24);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    subProgressBar.setVisibility(View.GONE);
                                    addSubCategory.setEnabled(true);
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                    subProgressBar.setProgressCompat((int) progress, true);
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        subProgressBar.setVisibility(View.GONE);
                        addSubCategory.setEnabled(true);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


    @Override
    public void onItemClick(SubCategory subCategory) {
        SubCategoryUpdateFragment dialog = SubCategoryUpdateFragment.newInstance(subCategory);
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