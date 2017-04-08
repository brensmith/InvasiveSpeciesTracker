package wit.ie.invasivespeciestracker.fragments;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import wit.ie.invasivespeciestracker.R;
import wit.ie.invasivespeciestracker.model.Species;
import wit.ie.invasivespeciestracker.util.LocationUtil;
import wit.ie.invasivespeciestracker.util.PermissionsUtil;

import static wit.ie.invasivespeciestracker.R.id.addButton;
import static wit.ie.invasivespeciestracker.R.id.additionalInfoText;
import static wit.ie.invasivespeciestracker.R.id.dateText;
import static wit.ie.invasivespeciestracker.R.id.habitatText;
import static wit.ie.invasivespeciestracker.R.id.locationText;
import static wit.ie.invasivespeciestracker.R.id.speciesText;

/*
    This fragment is used for both creating and editing a Species.
    create - if itemKey passed to AddEditFragment#newInstance(String) is null;
    edit - if if itemKey passed to AddEditFragment#newInstance(String) is NOT null;
 */
public class AddEditFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, OnMapReadyCallback {
    private static final String PARAM_ITEM_KEY = "param_item_key";

    EditText species, habitat, location, date, additionalInfo;
    Button Button;
    ScrollView scrollView;
    //Get the date from the system
    SimpleDateFormat dateF = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
    String dateTime = dateF.format(Calendar.getInstance().getTime());
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;
    private GoogleMap mMap;
    private List<LatLng> markers = new ArrayList<>();
    private String itemKey;
    private Spinner spinner;
    private Spinner spinner2;


    public AddEditFragment() {
        // Required empty public constructor
    }

    /*
        itemKey if not null edit item with this key
        if null create new Species
        return AddEditFragment instance
     */
    public static AddEditFragment newInstance(String itemKey) {
        Bundle args = new Bundle();
        args.putString(PARAM_ITEM_KEY, itemKey);

        AddEditFragment addEditFragment = new AddEditFragment();
        addEditFragment.setArguments(args);
        return addEditFragment;
    }

    /*
    All binding carried out and listeners set on buttons.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);

        scrollView = (ScrollView) view.findViewById(R.id.scrollView);

        spinner = (Spinner) view.findViewById(R.id.spinner);
        spinner2 = (Spinner) view.findViewById(R.id.spinner2);

        spinner.setOnItemSelectedListener(this);
        spinner2.setOnItemSelectedListener(this);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.species_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(),
                R.array.habitat_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner2.setAdapter(adapter2);

        if (savedInstanceState == null) {
            if (getArguments() != null && getArguments().containsKey(PARAM_ITEM_KEY)) {
                itemKey = getArguments().getString(PARAM_ITEM_KEY);
            }
        } else {
            itemKey = savedInstanceState.getString(PARAM_ITEM_KEY);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PARAM_ITEM_KEY, itemKey);

    }

    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Initialize Firebase Auth Current user and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        } else {
            mUserId = mFirebaseUser.getUid();
        }

        // Add items via the Button and EditText at the bottom of the view.
        species = (EditText) view.findViewById(speciesText);
        species.setVisibility(View.INVISIBLE);
        habitat = (EditText) view.findViewById(habitatText);
        habitat.setVisibility(View.INVISIBLE);
        location = (EditText) view.findViewById(locationText);
        date = (EditText) view.findViewById(dateText);
        //auto set the date
        date.setText(dateTime);
        additionalInfo = (EditText) view.findViewById(additionalInfoText);

        Button = (Button) view.findViewById(addButton);
        Button.setOnClickListener(this);
        // Change button caption
        if (itemKey == null) {
            Button.setText(R.string.add_item);
        } else {
            Button.setText(R.string.save_item);
        }

        updateLocationsString();

        // Setup Map
        MapFragment mapFragment = (MapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // this enables map scroll up and down inside scrollview
        ((WorkaroundMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        if (itemKey != null) {
            // there is an item to edit
            retrieveItemFromDb();
        }

    }

    private void retrieveItemFromDb() {
        Query speciesList = mDatabase.child("users").child(mFirebaseUser.getUid()).child("items").child(itemKey);
        speciesList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Species item = dataSnapshot.getValue(Species.class);

                renderItemIntoView(item);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void renderItemIntoView(Species item) {

        // Set species selection
        List<String> speciesArray = Arrays.asList(getResources().getStringArray(R.array.species_array));
        if (speciesArray.contains(item.getSpecies())) {
            spinner.setSelection(speciesArray.indexOf(item.getSpecies()));
        }

        // Set habitat selection
        List<String> habitatArray = Arrays.asList(getResources().getStringArray(R.array.habitat_array));
        if (habitatArray.contains(item.getHabitat())) {
            spinner2.setSelection(habitatArray.indexOf(item.getHabitat()));
        }

        markers = LocationUtil.jsonToLocations(item.getLocation());
        updateLocationsString();
        if (mMap != null) {
            LocationUtil.renderMarkersListToMap(mMap, markers);
        }

        date.setText(item.getDate());
        additionalInfo.setText(item.getAdditionalInfo());
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
/*
        this is our default position of the map.
        these coordinates were chosen because they are in the centre of ireland
        and allow the map of ireland to be displayed in full
        */
        LatLng latlng = new LatLng(53.419456, -7.936307);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setTrafficEnabled(false);
        //adds zoom in/out buttons to the map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 6));
        LocationUtil.renderMarkersListToMap(mMap, markers);

        // add marker when click on map
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                markers.add(latLng);

                // Just add marker.
                mMap.addMarker(new MarkerOptions().position(latLng));

                updateLocationsString();
            }
        });

        // remove marker on click
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                // Show delete confirmation dialog
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.remove_marker_title)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                markers.remove(marker.getPosition());
                                updateLocationsString();

                                LocationUtil.renderMarkersListToMap(mMap, markers);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)       // do nothing. Dialog dismisses itself automatically
                        .show();
                return true;
            }
        });

        if (PermissionsUtil.checkAndRequestLocationPermission(getActivity())) {
            // If permissions allowed, show my location button.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        // Required to remove map fragment from view to avoid crash
        try {
            Fragment fragment = (getChildFragmentManager()
                    .findFragmentById(R.id.map));
            FragmentTransaction ft = getActivity().getFragmentManager()
                    .beginTransaction();
            ft.remove(fragment);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroyView();
    }

    private void updateLocationsString() {
        location.setText(LocationUtil.getUserFriendlyLocationString(markers));
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.addButton:
                if (species.getText().toString().equals("Click to Select") || (habitat.getText().toString().equals("Click to Select")) || markers.isEmpty() || (date.getText().toString().isEmpty()) || (additionalInfo.getText().toString().length() == 0)) {
                    species.setError("Please Enter Species!");
                    location.setError("Please Select Location");
                    Toast.makeText(getActivity(), "Please check fields, Data not inserted!", Toast.LENGTH_LONG).show();
                } else if (species.getText().toString().length() > 0 || (habitat.getText().toString().length() > 0)) {
                    Species item = new Species(species.getText().toString(), habitat.getText().toString(), LocationUtil.locationsToJson(markers), date.getText().toString(), additionalInfo.getText().toString());

                    Query howMany = mDatabase.child("users").child(mUserId).child("items");
                    howMany.limitToLast(1);

                    howMany.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }

                    });

                    if (itemKey == null) {
                        // Add item
                        mDatabase.child("users").child(mUserId).child("items").push().setValue(item);

                        species.setText("");
                        habitat.setText("");
                        markers.clear();
                        updateLocationsString();
                        date.setText("");
                        additionalInfo.setText("");
                        Toast.makeText(getActivity(), "Invasive Species Added", Toast.LENGTH_LONG).show();
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.fragmentFrame, new AddEditFragment()).commit();
                    } else {
                        // Edit item
                        mDatabase.child("users").child(mUserId).child("items").child(itemKey).setValue(item);
                        itemKey = null;
                        getArguments().putString(PARAM_ITEM_KEY, null);

                        Toast.makeText(getActivity(), "Invasive Species Updated", Toast.LENGTH_LONG).show();
                        getActivity().onBackPressed();
                    }

                }

                break;


        }

    }

    private void loadLogInView() {

    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
// On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.spinner) {
            //do this
            species.setText(item);
        } else if (spinner.getId() == R.id.spinner2) {
            //do this
            habitat.setText(item);
        }

    }


    public void onNothingSelected(AdapterView<?> parent) {

    }

}









