package wit.ie.invasivespeciestracker.fragments;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import wit.ie.invasivespeciestracker.R;
import wit.ie.invasivespeciestracker.model.Species;
import wit.ie.invasivespeciestracker.util.LocationUtil;



public class MyMapFragment extends Fragment implements OnMapReadyCallback {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;

    //GoogleMap Object
    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        MapFragment mapFragment = (MapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        // Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        retrieveDataFromDb();


    }

    private void retrieveDataFromDb() {

        com.google.firebase.database.Query speciesList = mDatabase.child("users").child(mFirebaseUser.getUid()).child("items");
        speciesList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Species> species = new ArrayList<>();
                for (DataSnapshot speciesList : dataSnapshot.getChildren()) {
                    species.add(speciesList.getValue(Species.class));
                }

                LocationUtil.renderSpeciesMarkersToList(mMap, species);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}


