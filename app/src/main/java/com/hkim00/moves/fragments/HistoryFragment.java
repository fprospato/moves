package com.hkim00.moves.fragments;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hkim00.moves.R;
import com.hkim00.moves.adapters.MoveAdapter;
import com.hkim00.moves.models.Event;
import com.hkim00.moves.models.Move;
import com.hkim00.moves.models.Restaurant;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class HistoryFragment extends Fragment {

    private RecyclerView rvPastMoves;
    private MoveAdapter HistoryAdapter;
    private List<Move> HistoryList;

    public final static String TAG = "HistoryFragment";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpIds(view);
        setupRecyclerView();

        getHistory();


    }

    private void setupRecyclerView() {

        rvPastMoves.setLayoutManager(new LinearLayoutManager(getContext()));
        HistoryList = new ArrayList<>();
        HistoryAdapter = new MoveAdapter(getContext(),  HistoryList);
        rvPastMoves.setAdapter(HistoryAdapter);


    }


    private void getHistory() {
        ParseQuery<ParseObject> restaurantQuery = ParseQuery.getQuery("Restaurant");
        restaurantQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        restaurantQuery.whereEqualTo("didComplete", true);
        restaurantQuery.orderByDescending("createdAt");

        restaurantQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (int i = 0; i < objects.size(); i++) {
                    if (e == null) {
                        Restaurant restaurant = Restaurant.fromParseObject(objects.get(i));
                        HistoryList.add(restaurant);
                        HistoryAdapter.notifyItemInserted( HistoryList.size() - 1);
                    } else {
                        Log.e(TAG, "Error finding saved restaurants.");
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error saving profile", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery("Event");
        eventQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        eventQuery.whereEqualTo("didComplete", true);
        eventQuery.orderByDescending("createdAt");

        eventQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (int i = 0; i < objects.size(); i++) {
                    if (e == null) {
                        Event event = Event.fromParseObject(objects.get(i));
                        HistoryList.add(event);
                        HistoryAdapter.notifyItemInserted(HistoryList.size() - 1);
                    } else {
                        Log.e(TAG, "Error finding saved restaurants.");
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error saving profile", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void setUpIds(View view) {
        rvPastMoves = view.findViewById(R.id.rvPastMoves);
    }

}
