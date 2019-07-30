package com.hkim00.moves.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.litho.Component;
import com.facebook.litho.ComponentContext;
import com.hkim00.moves.LogInActivity;
import com.hkim00.moves.MoveDetailsActivity;
import com.hkim00.moves.R;

//import com.hkim00.moves.adapters.RestaurantAdapter;
import com.hkim00.moves.adapters.MoveAdapter;
import com.hkim00.moves.models.Event;
import com.hkim00.moves.models.Move;
import com.hkim00.moves.models.Restaurant;

import com.hkim00.moves.specs.MoveItem;
import com.hkim00.moves.specs.MoveSection;
import com.hkim00.moves.util.ParseUtil;

import com.hkim00.moves.models.Move;
import com.hkim00.moves.models.Restaurant;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    public final static String TAG = "ProfileFragment";
    ParseUser currUser;

    private Button btnSaved;
    private Button btnFavorites;
    private Button btnLogout;
    private RecyclerView rvFavorites;
    private RecyclerView rvSaved;

    private TextView tvName;
    private TextView tvLocation;
    private TextView tvGender;
    private TextView tvAge;
    private ImageView ivSaved;
    private ImageView ivFavorites;

    private MoveAdapter favAdapter;
    private MoveAdapter saveAdapter;

    private List<Move> favList;
    private List<Move> saveList;

    Move move;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ComponentContext context = new ComponentContext(getContext());

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getViewIds(view);

        fillUserInfo();

        setupButtons();

        setupRecyclerViews();

        getMoveLists("favorites");
        getMoveLists("saved");
    }

    private void getViewIds(View view) {
        tvName = view.findViewById(R.id.tvName);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvGender = view.findViewById(R.id.tvGender);
        tvAge = view.findViewById(R.id.tvAge);
        ivSaved = view.findViewById(R.id.ivSaved);
        ivFavorites = view.findViewById(R.id.ivFavorites);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnSaved =  view.findViewById(R.id.btnSave);
        btnFavorites = view.findViewById(R.id.btnFavorite);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        rvSaved = view.findViewById(R.id.rvSaved);
    }

    private void fillUserInfo() {
        currUser = ParseUser.getCurrentUser();

        tvName.setText(currUser.getUsername());
        tvLocation.setText("Your location: " + currUser.getString("location"));
        tvGender.setText("Gender: " + currUser.getString("gender"));
        tvAge.setText("Age: " + currUser.getInt("age"));
    }

    private void setupRecyclerViews() {
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSaved.setLayoutManager(new LinearLayoutManager(getContext()));

        favList = new ArrayList<>();
        favAdapter = new MoveAdapter(getContext(), favList);
        rvFavorites.setAdapter(favAdapter);

        saveList = new ArrayList<>();
        saveAdapter = new MoveAdapter(getContext(), saveList);
        rvSaved.setAdapter(saveAdapter);

        rvSaved.setVisibility(View.INVISIBLE);
    }

    private void getMoveLists(String listType) {
        ParseQuery<ParseObject> moveQuery = ParseQuery.getQuery("Move");
        moveQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        moveQuery.orderByDescending("createdAt");
        if (listType == "saved") {
            moveQuery.whereEqualTo("didSave", true);
        } else {
            moveQuery.whereEqualTo("didFavorite", true);
        }
            moveQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        List<Move> moves = new ArrayList<>();

                        for (int i = 0; i < objects.size(); i++) {
                            if (objects.get(i).getString("moveType").equals("food")) {
                                moves.add(Restaurant.fromParseObject(objects.get(i)));
                            } else {
                                moves.add(Event.fromParseObject(objects.get(i)));
                            }
                        }
                        saveList.addAll(moves);
                        saveAdapter.notifyItemInserted(saveList.size() - 1);
                    } else {
                        Log.e(TAG, "Error finding saved list.");
                        e.printStackTrace();
                    }
                }
            });

        }


    private void setupButtons() {
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("location", 0); //0 for private mode
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();

                final Intent intent = new Intent(getContext(), LogInActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set details
                ivFavorites.setImageResource(R.drawable.ufi_heart_active);
                ivSaved.setImageResource(R.drawable.ufi_save);
                //show favorites recycler view
                rvSaved.setVisibility(View.INVISIBLE);
                rvFavorites.setVisibility(View.VISIBLE);
            }
        });

        btnSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set details
                ivSaved.setImageResource(R.drawable.ufi_save_active);
                ivFavorites.setImageResource(R.drawable.ufi_heart);
                //show saved recycler view
                rvFavorites.setVisibility(View.INVISIBLE);
                rvSaved.setVisibility(View.VISIBLE);
            }
        });
    }
}
