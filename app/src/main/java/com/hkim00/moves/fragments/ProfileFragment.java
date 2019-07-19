package com.hkim00.moves.fragments;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hkim00.moves.HomeActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.hkim00.moves.LogInActivity;
import com.hkim00.moves.R;
import com.hkim00.moves.adapters.ProfileAdapter;
import com.hkim00.moves.models.Restaurant;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {


    public final static String TAG = "ProfileFragment";


    private Button btnSaved;
    private Button btnFavorites;
    private Button btnLogout;
    private RecyclerView rvFavorites;
    private RecyclerView rvSaved;

    //TODO create lists for saved and favorites moves
    private ProfileAdapter Faveadapter;
    private ProfileAdapter Saveadapter;
    private List<Restaurant> rFaveList;
    private List<Restaurant> rSaveList;


    private ProfileAdapter favAdapter;
    private ProfileAdapter saveAdapter;

    private List<Restaurant> favList;
    private List<Restaurant> saveList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnSaved =  view.findViewById(R.id.btnSaved);
        btnFavorites = view.findViewById(R.id.btnFavorites);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        rvSaved = view.findViewById(R.id.rvSaved);


        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSaved.setLayoutManager(new LinearLayoutManager(getContext()));

        rFaveList = new ArrayList<>();
        Faveadapter = new ProfileAdapter(getContext(), rFaveList);
        rvFavorites.setAdapter(Faveadapter);

        rSaveList = new ArrayList<>();
        Saveadapter = new ProfileAdapter(getContext(), rSaveList);
        rvSaved.setAdapter(Saveadapter);

        setupButtons();

        setupRecyclerViews();

        setupButtons();

        rvSaved.setVisibility(View.INVISIBLE);

        getFavoriteRestaurants();
        getSavedRestaurants();
    }

    private void setupRecyclerViews() {
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSaved.setLayoutManager(new LinearLayoutManager(getContext()));

        favList = new ArrayList<>();
        favAdapter = new ProfileAdapter(getContext(), favList);
        rvFavorites.setAdapter(favAdapter);

        saveList = new ArrayList<>();
        saveAdapter = new ProfileAdapter(getContext(), saveList);
        rvSaved.setAdapter(saveAdapter);
    }

    private void getSavedRestaurants() {
        ParseQuery<ParseObject> restaurantQuery = ParseQuery.getQuery("Restaurant");
        restaurantQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        restaurantQuery.whereEqualTo("didSave", true);
        restaurantQuery.orderByDescending("createdAt");

        restaurantQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (int i = 0; i < objects.size(); i++) {
                    if (e == null) {
                        Restaurant restaurant = Restaurant.fromParseObject(objects.get(i));

                        saveList.add(restaurant);
                        saveAdapter.notifyItemInserted(saveList.size() - 1);
                    } else {
                        Log.e(TAG, "Error finding saved restaurants.");
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error saving profile", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private void getFavoriteRestaurants() {
        ParseQuery<ParseObject> restaurantQuery = ParseQuery.getQuery("Restaurant");
        restaurantQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        restaurantQuery.whereEqualTo("didFavorite", true);
        restaurantQuery.orderByDescending("createdAt");

        restaurantQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (int i = 0; i < objects.size(); i++) {
                    if (e == null) {
                        Restaurant restaurant = Restaurant.fromParseObject(objects.get(i));

                        favList.add(restaurant);
                        favAdapter.notifyItemInserted(saveList.size() - 1);
                    } else {
                        Log.e(TAG, "Error finding saved restaurants.");
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error saving profile", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


    private void setupButtons() {
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();

                final Intent intent = new Intent(getContext(), LogInActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show favorites recycler view
                rvSaved.setVisibility(View.INVISIBLE);
                rvFavorites.setVisibility(View.VISIBLE);
            }
        });

        btnSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show saved recycler view
                rvFavorites.setVisibility(View.INVISIBLE);
                rvSaved.setVisibility(View.VISIBLE);
            }
        });
    }
}