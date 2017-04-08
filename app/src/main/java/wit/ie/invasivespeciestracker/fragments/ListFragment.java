package wit.ie.invasivespeciestracker.fragments;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import wit.ie.invasivespeciestracker.R;
import wit.ie.invasivespeciestracker.adapter.SpeciesAdapter;
import wit.ie.invasivespeciestracker.model.Species;


public class ListFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    ListView ListView1;
    SpeciesAdapter speciesAdapter;
    ArrayList<String> keysList = new ArrayList<>();
    ArrayList<Species> arraylist = new ArrayList<>();
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);


    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
// Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //FirebaseUser mUserId = mFirebaseUser.getUid();

        ListView1 = (ListView) view.findViewById(R.id.ListView);

        speciesAdapter = new SpeciesAdapter(getActivity(), arraylist);

        ListView1.setAdapter(speciesAdapter);
        ListView1.setOnItemClickListener(this);
        ListView1.setOnItemLongClickListener(this);

        Query speciesList = mDatabase.child("users").child(mFirebaseUser.getUid()).child("items");
        speciesList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arraylist.clear();
                keysList.clear();
                for (DataSnapshot speciesList : dataSnapshot.getChildren()) {
                    keysList.add(speciesList.getKey());
                    arraylist.add(speciesList.getValue(Species.class));
                }
                speciesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final String itemKey = keysList.get(position);

        getFragmentManager().beginTransaction().replace(R.id.fragmentFrame, ViewFragment.newInstance(itemKey))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        final Species item = arraylist.get(position);
        final String itemKey = keysList.get(position);

        // Show dialog with edit/delete options
        new AlertDialog.Builder(getActivity())
                .setTitle(item.getSpecies())
                .setPositiveButton(R.string.delete_btn_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child("users").child(mFirebaseUser.getUid()).child("items").child(itemKey).removeValue();
                        Toast.makeText(getActivity(), "Record deleted.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.edit_btn_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        getFragmentManager().beginTransaction().replace(R.id.fragmentFrame, AddEditFragment.newInstance(itemKey))
                                .addToBackStack(null)
                                .commit();
                    }
                })
                .show();
        return true;

    }
}
