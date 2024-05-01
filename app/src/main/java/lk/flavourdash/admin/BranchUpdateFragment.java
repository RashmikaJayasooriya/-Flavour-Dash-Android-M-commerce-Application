package lk.flavourdash.admin;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import lk.flavourdash.admin.Model.Branch;
import lk.flavourdash.admin.Model.Category;

public class BranchUpdateFragment extends DialogFragment implements OnMapReadyCallback {

    private Branch branch;
    public static final String TAG = MainActivity.class.getName();
    private FirebaseFirestore firebaseFirestore;
    private FrameLayout mapContainer;
    private GoogleMap mMap;
    private double selectedLatitude;
    private double selectedLongitude;
    private Marker selectedLocationMarker;
    private MarkerOptions selectedLocationMarkerOptions;
    private TextView selectedLocationTextView;
    private final String API_KEY="***";

    public static BranchUpdateFragment newInstance(Branch branch) {
        BranchUpdateFragment fragment = new BranchUpdateFragment();
        Bundle args = new Bundle();
        args.putSerializable("branch", branch);
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
        View view = inflater.inflate(R.layout.fragment_branch_update, container, false);

        Button closeButton = view.findViewById(R.id.branchUpdateCloseBtn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if (getArguments() != null) {
            branch = (Branch) getArguments().getSerializable("branch");

            TextView branAdd = view.findViewById(R.id.textViewUpdate2);
            EditText branName = view.findViewById(R.id.branchNameUpdateEditText);

            Geocoder geocoder = new Geocoder(getContext().getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        branch.getLatitude(), branch.getLongitude(), 1);

                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String selectedAddress = address.getAddressLine(0);
                    branAdd.setText(selectedAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            branName.setText(branch.getName());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseFirestore = FirebaseFirestore.getInstance();
        Places.initialize(getContext().getApplicationContext(), API_KEY);
        PlacesClient placesClient = Places.createClient(getContext());

        // Init AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // Get latitude and longitude
                if (place.getLatLng() != null) {
                    selectedLatitude = place.getLatLng().latitude;
                    selectedLongitude = place.getLatLng().longitude;

                    Log.i(TAG, "Place: " + place.getName() + ", ID: " + place.getId() +
                            ", LatLng: " + selectedLatitude + ", " + selectedLongitude);

                    // Update map for selected location
                    updateMap();
                    updateSelectedLocationTextView();
                } else {
                    Log.i(TAG, "Place: " + place.getName() + ", ID: " + place.getId() +
                            ", LatLng: Not available");
                    Toast.makeText(getActivity(), "LatLang not found select another place", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        mapContainer = view.findViewById(R.id.mapContainer);
        selectedLocationTextView = view.findViewById(R.id.textViewUpdate2);
        setUpMapIfNeeded();

//        Save Branch
        view.findViewById(R.id.branchUpdateBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText bName = view.findViewById(R.id.branchNameUpdateEditText);
                String branchName = bName.getText().toString();

                Branch updateBranch = new Branch(branchName, selectedLatitude, selectedLongitude);

                firebaseFirestore.collection("restaurant_branches").document(branch.getId())
                        .set(updateBranch).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getContext(), "Branch Updates Successfully", Toast.LENGTH_LONG).show();
                                bName.setText("");
                                TextView textView = view.findViewById(R.id.textViewUpdate2);
                                textView.setText("Select A Location");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.mapContainer, mapFragment)
                    .commit();
            // callback notify when the map is ready
            mapFragment.getMapAsync(this);
        }
    }

    private void updateMap() {
        if (mMap != null) {
            mMap.clear();

//          + marker and -> camera -> selected location
            LatLng location = new LatLng(selectedLatitude, selectedLongitude);
            selectedLocationMarker = mMap.addMarker(new MarkerOptions().position(location).title("Selected Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    // Update marker and selected coordinates on map click
                    selectedLatitude = latLng.latitude;
                    selectedLongitude = latLng.longitude;

                    // Update the position of the marker
                    if (selectedLocationMarker != null) {
                        selectedLocationMarker.setPosition(latLng);
                        updateSelectedLocationTextView();
                    }

                    Log.i(TAG, "Updated LatLng: " + selectedLatitude + ", " + selectedLongitude);
                }
            });
        }
    }

    private void updateSelectedLocationTextView() {
        if (selectedLocationMarker != null) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        selectedLatitude, selectedLongitude, 1);

                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String selectedAddress = address.getAddressLine(0);
                    selectedLocationTextView.setText(selectedAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMap();
        updateSelectedLocationTextView();
    }
}