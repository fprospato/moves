package com.hkim00.moves;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hkim00.moves.adapters.MoveAdapter;
import com.hkim00.moves.models.Move;
import com.hkim00.moves.models.Restaurant;
import com.hkim00.moves.models.UserLocation;
import com.hkim00.moves.util.MoveCategoriesHelper;
import com.hkim00.moves.util.StatusCodeHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TripActivity extends AppCompatActivity {

    private final static String TAG = "TripActivity";
    public static final String API_BASE_URL = "https://maps.googleapis.com/maps/api";
    private static final int LOCATION_REQUEST_CODE = 20;
    public static final int CALENDAR_REQUEST_CODE = 30;


    private EditText etTrip;
    private TextView tvLocation, tvCalendar;
    private Button btnLocation, btnCalendar;
    private Button btnFood, btnEvents, btnSelected;
    private View vFoodView, vEventsView, vSelectedView;

    private ProgressBar pb;
    private RecyclerView rvFood;
    private MoveAdapter foodAdapter;

    private UserLocation location;
    public static List<CalendarDay> dates;
    private List<Move> foodMoves;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        getViewIds();

        setupRecyclerView();

        setupButtons();

        setupView();
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }


    private void getViewIds() {
        etTrip = findViewById(R.id.etTripName);
        tvLocation = findViewById(R.id.tvLocation);
        btnLocation = findViewById(R.id.btnLocation);
        tvCalendar = findViewById(R.id.tvCalendar);
        btnCalendar = findViewById(R.id.btnCalendar);

        btnFood = findViewById(R.id.btnFood);
        btnEvents = findViewById(R.id.btnEvents);
        btnSelected = findViewById(R.id.btnSelected);

        vFoodView = findViewById(R.id.vFoodView);
        vEventsView = findViewById(R.id.vEventsView);
        vSelectedView = findViewById(R.id.vSelectedView);

        pb = findViewById(R.id.pb);
        rvFood = findViewById(R.id.rvFood);
    }


    private void setupRecyclerView() {
        rvFood.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        foodMoves = new ArrayList<>();
        foodAdapter = new MoveAdapter(getApplicationContext(), foodMoves);
        rvFood.setAdapter(foodAdapter);
    }


    private void setupButtons() {
        btnLocation.setOnClickListener(view -> goToLocationActivity());

        btnCalendar.setOnClickListener(view -> goToCalendarActivity());

        btnFood.setOnClickListener(view -> toggleSection("food"));
        btnEvents.setOnClickListener(view -> toggleSection("events"));
        btnSelected.setOnClickListener(view -> toggleSection("selected"));
    }

    private void toggleSection(String type) {
        vFoodView.setBackgroundColor(getResources().getColor((type.equals("food")) ? R.color.selected_category : R.color.white));
        vEventsView.setBackgroundColor(getResources().getColor((type.equals("events")) ? R.color.selected_category : R.color.white));
        vSelectedView.setBackgroundColor(getResources().getColor((type.equals("selected")) ? R.color.selected_category : R.color.white));
    }


    private void setupView() {
        tvLocation.setTextColor(getResources().getColor(R.color.selected_blue));
        tvCalendar.setTextColor(getResources().getColor(R.color.selected_blue));

        vEventsView.setBackgroundColor(getResources().getColor(R.color.white));
        vSelectedView.setBackgroundColor(getResources().getColor(R.color.white));

        pb.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == LOCATION_REQUEST_CODE) {
            checkForCurrentLocation();
        } else if (resultCode == RESULT_OK && requestCode == CALENDAR_REQUEST_CODE) {
            if (dates.size() > 0) {
                tvCalendar.setTextColor(getResources().getColor(R.color.black));
                String[] months = new DateFormatSymbols().getShortMonths();
                tvCalendar.setText(months[dates.get(0).getMonth() - 1] + " " + dates.get(0).getDay() + " - " + months[dates.get(dates.size() - 1).getMonth() - 1] + " " + dates.get(dates.size() - 1).getDay());
            } else {
                tvCalendar.setText("When?");
                tvCalendar.setTextColor(getResources().getColor(R.color.selected_blue));
            }
        }
    }


    private void checkForCurrentLocation() {
        location = UserLocation.getCurrentTripLocation(this);

        if (location.lat.equals("0.0") && location.name.equals("")) {
            tvLocation.setText("Where?");
            tvLocation.setTextColor(getResources().getColor(R.color.selected_blue));
        } else {
            tvLocation.setText(location.name);
            tvLocation.setTextColor(getResources().getColor(R.color.black));

            getNearbyRestaurants();
        }
    }


    private void goToLocationActivity() {
        Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
        intent.putExtra("isTrip", true);
        startActivityForResult(intent, LOCATION_REQUEST_CODE);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    private void goToCalendarActivity() {
        Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
        startActivityForResult(intent, CALENDAR_REQUEST_CODE);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }


    private void getNearbyRestaurants() {
        pb.setVisibility(View.VISIBLE);

        String apiUrl = API_BASE_URL + "/place/nearbysearch/json";
        float distance = MoveCategoriesHelper.milesToMeters(10);

        RequestParams params = new RequestParams();
        params.put("location",location.lat + "," + location.lng);
        params.put("radius", (distance > 50000) ? 50000 : distance);
        params.put("type","restaurant");

        String userFoodPref = MoveCategoriesHelper.getUserFoodPreferenceString(new ArrayList<>());

        if (!userFoodPref.equals("")) {
            params.put("keyword", "mexican");
        }

        params.put("key", getString(R.string.api_key));

        HomeActivity.client.get(apiUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                pb.setVisibility(View.INVISIBLE);

                foodMoves.clear();
                try {
                    foodMoves.addAll(Restaurant.arrayFromJSONArray(response.getJSONArray("results")));

                    foodAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                pb.setVisibility(View.INVISIBLE);
                new StatusCodeHandler(TAG, statusCode);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                pb.setVisibility(View.INVISIBLE);
                new StatusCodeHandler(TAG, statusCode);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                pb.setVisibility(View.INVISIBLE);
                new StatusCodeHandler(TAG, statusCode);
                throwable.printStackTrace();
            }
        });
    }
}
