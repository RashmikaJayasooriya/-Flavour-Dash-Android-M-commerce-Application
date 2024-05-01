package lk.flavourdash.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import lk.flavourdash.admin.Model.Branch;

public class NewBranchSelectFragment extends Fragment implements OnMapReadyCallback {

    public static final String TAG = MainActivity.class.getName();

    private FrameLayout mapContainer;
    private GoogleMap mMap;
    private double selectedLatitude;
    private double selectedLongitude;
    private Marker selectedLocationMarker;
    private MarkerOptions selectedLocationMarkerOptions;
    private TextView selectedLocationTextView;
    private FirebaseFirestore firebaseFirestore;
    private final String API_KEY="AIzaSyDVPd1-bmrSDjSwbjdpfZoR3wl-HQnIxCw";

    public NewBranchSelectFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_branch_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseFirestore=FirebaseFirestore.getInstance();

        Places.initialize(getContext().getApplicationContext(), API_KEY);
        PlacesClient placesClient = Places.createClient(getContext());

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
            getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);


        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //  latitude and longitude
                if (place.getLatLng() != null) {
                    selectedLatitude = place.getLatLng().latitude;
                    selectedLongitude = place.getLatLng().longitude;
                    Log.i(TAG, "Place: " + place.getName() + ", ID: " + place.getId() +
                            ", LatLng: " + selectedLatitude + ", " + selectedLongitude);

                    // Update map with selected location
                    updateMap();
                    updateSelectedLocationTextView();
                } else {
                    Log.i(TAG, "Place: " + place.getName() + ", ID: " + place.getId() +
                            ", LatLng: Not available");
                }
            }

            @Override
            public void onError(Status status) {
                // Handle the error
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        mapContainer = view.findViewById(R.id.mapContainer);
        selectedLocationTextView = view.findViewById(R.id.textView2);

        setUpMapIfNeeded();

//      Save Branch
        view.findViewById(R.id.branchSaveBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               EditText bName= view.findViewById(R.id.branchNameEditText);
               String branchName=bName.getText().toString();

                if (branchName.isEmpty() || selectedLatitude == 0.0 || selectedLongitude == 0.0) {
                    Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                Branch branch=new Branch(branchName,selectedLatitude,selectedLongitude);

                firebaseFirestore.collection("restaurant_branches").add(branch).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(), "Branch Added Successfully", Toast.LENGTH_LONG).show();
                        bName.setText("");
                        TextView textView= view.findViewById(R.id.textView2);
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

            mapFragment.getMapAsync(this);
        }
    }

    private void updateMap() {
        if (mMap != null) {
            // Clear existing markers
            mMap.clear();

            LatLng location = new LatLng(selectedLatitude, selectedLongitude);
            selectedLocationMarker = mMap.addMarker(new MarkerOptions().position(location).title("Selected Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

            // click listener for the map
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    selectedLatitude = latLng.latitude;
                    selectedLongitude = latLng.longitude;

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