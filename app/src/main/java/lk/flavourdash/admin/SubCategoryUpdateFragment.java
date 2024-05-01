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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

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
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import lk.flavourdash.admin.Model.Category;
import lk.flavourdash.admin.Model.SubCategory;

public class SubCategoryUpdateFragment extends DialogFragment {
    private SubCategory subCategory;
    private ImageButton imageButton;
    private Uri imagePath;
    public static final String TAG = MainActivity.class.getName();
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;

    public static SubCategoryUpdateFragment newInstance(SubCategory subCategory) {
        SubCategoryUpdateFragment fragment = new SubCategoryUpdateFragment();
        Bundle args = new Bundle();
        args.putSerializable("subcategory", subCategory);
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
        View view=inflater.inflate(R.layout.fragment_sub_category_update, container, false);

        Button closeButton = view.findViewById(R.id.updateSubDialogCloseButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if (getArguments() != null) {
            subCategory = (SubCategory) getArguments().getSerializable("subcategory");

            EditText subcatName = view.findViewById(R.id.subcategoryNameUpdateEditText);
            EditText subcatDesc = view.findViewById(R.id.subcategoryUpdateDescriptionEditText);
            ImageButton subcatImageBtn = view.findViewById(R.id.subImageUpdateButton);
            MaterialSwitch statusUpdateSwitch = view.findViewById(R.id.subcategoryUpdateStatus);
            subcatName.setText(subCategory.getName());
            subcatDesc.setText(subCategory.getDescription());
            statusUpdateSwitch.setChecked(subCategory.isActive());

            FirebaseStorage.getInstance().getReference("subCategory-images/" + subCategory.getImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getContext()).load(uri).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).into(subcatImageBtn);
                }
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        imageButton = view.findViewById(R.id.subImageUpdateButton);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));
            }
        });

        Button updateBtn = view.findViewById(R.id.updateSubCategory);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText subcatName = view.findViewById(R.id.subcategoryNameUpdateEditText);
                EditText subcatDescription = view.findViewById(R.id.subcategoryUpdateDescriptionEditText);
                MaterialSwitch subcatStatus = view.findViewById(R.id.subcategoryUpdateStatus);

                String subcategoryName = subcatName.getText().toString();
                String subcategoryDescription = subcatDescription.getText().toString();
                String imageId = subCategory.getImage();

                if (subcategoryName.isEmpty() || subcategoryDescription.isEmpty() || imageId == null) {
                    Toast.makeText(getContext(), "Sub-Category name, description, and image are required", Toast.LENGTH_SHORT).show();
                    return;
                }



                Category updatedCategory = new Category(subcategoryName, subcategoryDescription, imageId, subcatStatus.isChecked());

                CircularProgressIndicator progressBar = view.findViewById(R.id.subUpdateProgressBar);
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                updateBtn.setEnabled(false);

                firebaseFirestore.collection("subCategory").document(subCategory.getId())
                        .set(updatedCategory)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Document successfully updated
                                if (imagePath != null) {
                                    // Your code to handle image upload
                                    StorageReference reference = storage.getReference("subCategory-images").child(imageId);

                                    reference.putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            progressBar.setVisibility(View.GONE);
                                            updateBtn.setEnabled(true);
                                            Toast.makeText(getContext(), "SubCategory updated successfully", Toast.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressBar.setVisibility(View.GONE);
                                            updateBtn.setEnabled(true);
                                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    // No image to upload
                                    progressBar.setVisibility(View.GONE);
                                    updateBtn.setEnabled(true);
                                    Toast.makeText(getContext(), "SubCategory updated successfully", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failures
                                progressBar.setVisibility(View.GONE);
                                updateBtn.setEnabled(true);
                                Toast.makeText(getContext(), "Failed to update subcategory: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
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