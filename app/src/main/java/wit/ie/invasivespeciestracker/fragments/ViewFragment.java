package wit.ie.invasivespeciestracker.fragments;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import wit.ie.invasivespeciestracker.R;
import wit.ie.invasivespeciestracker.model.Species;
import wit.ie.invasivespeciestracker.util.LocationUtil;
import wit.ie.invasivespeciestracker.util.PermissionsUtil;

import static wit.ie.invasivespeciestracker.R.id.additionalInfoText;
import static wit.ie.invasivespeciestracker.R.id.dateText;
import static wit.ie.invasivespeciestracker.R.id.editButton;
import static wit.ie.invasivespeciestracker.R.id.habitatText;
import static wit.ie.invasivespeciestracker.R.id.locationText;
import static wit.ie.invasivespeciestracker.R.id.speciesText;


public class ViewFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {
    private static final String PARAM_ITEM_KEY = "param_item_key";

    TextView species, habitat, location, date, additionalInfo;
    Button Button;
    ScrollView scrollView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;

    private GoogleMap mMap;
    private List<LatLng> markers = new ArrayList<>();

    private String itemKey;

    public ViewFragment() {
        // Required empty public constructor
    }

    public static ViewFragment newInstance(String itemKey) {
        Bundle args = new Bundle();
        args.putString(PARAM_ITEM_KEY, itemKey);

        ViewFragment addEditFragment = new ViewFragment();
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
        View view = inflater.inflate(R.layout.fragment_view, container, false);

        scrollView = (ScrollView) view.findViewById(R.id.scrollView);


        if (savedInstanceState == null) {
            if (getArguments() != null && getArguments().containsKey(PARAM_ITEM_KEY)) {
                itemKey = getArguments().getString(PARAM_ITEM_KEY);
            }
        } else {
            itemKey = savedInstanceState.getString(PARAM_ITEM_KEY);
        }

        if (itemKey == null) {
            getActivity().onBackPressed();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PARAM_ITEM_KEY, itemKey);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Initialize Firebase Auth and Database Reference
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
        species = (TextView) view.findViewById(speciesText);
        habitat = (TextView) view.findViewById(habitatText);
        location = (TextView) view.findViewById(locationText);
        date = (TextView) view.findViewById(dateText);
        additionalInfo = (TextView) view.findViewById(additionalInfoText);

        Button = (Button) view.findViewById(editButton);
        Button.setOnClickListener(this);

        location.setText(LocationUtil.getUserFriendlyLocationString(markers));

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

        retrieveItemFromDb();
    }

    @Override
    public void onDestroyView() {
        // Requires to remove map fragment from view to avoid crash
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


      //Get items from DB and show them in view

    private void retrieveItemFromDb() {
        Query speciesList = mDatabase.child("users").child(mFirebaseUser.getUid()).child("items").child(itemKey);
        speciesList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Species item = dataSnapshot.getValue(Species.class);

                renderItem(item);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


     //Render Species into view

    private void renderItem(Species item) {
        habitat.setText(item.getHabitat());
        species.setText(item.getSpecies());

        date.setText(item.getDate());
        additionalInfo.setText(item.getAdditionalInfo());

        markers = LocationUtil.jsonToLocations(item.getLocation());
        location.setText(LocationUtil.getUserFriendlyLocationString(markers));
        LocationUtil.renderMarkersListToMap(mMap, markers);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        // Initialize Firebase Auth and Database Reference
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        LocationUtil.renderMarkersListToMap(mMap, markers);

        if (PermissionsUtil.checkAndRequestLocationPermission(getActivity())) {
            // If permissions allowed, show my location button.
            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.editButton:
                // Open edit data fragment
                getFragmentManager().beginTransaction().replace(R.id.fragmentFrame, AddEditFragment.newInstance(itemKey))
                        .addToBackStack(null)
                        .commit();


        }

    }


    private void loadLogInView() {

    }


}









