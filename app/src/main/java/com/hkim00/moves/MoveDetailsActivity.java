package com.hkim00.moves;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.*;

import androidx.appcompat.app.AppCompatActivity;

import com.hkim00.moves.models.Event;
import com.hkim00.moves.models.Move;
import com.hkim00.moves.models.Restaurant;
import com.hkim00.moves.models.UserLocation;

import com.hkim00.moves.util.ParseUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import com.lyft.lyftbutton.LyftButton;
import com.lyft.lyftbutton.RideParams;
import com.lyft.networking.ApiConfig;
import com.lyft.deeplink.RideTypeEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

//import util methods: JSONObject response formatters
import static com.hkim00.moves.util.JSONResponseHelper.getPriceRange;
import static com.hkim00.moves.util.JSONResponseHelper.getStartTime;

public class MoveDetailsActivity extends AppCompatActivity {

    private final static String TAG = "MoveDetailsActivity";

    private TextView tvMoveName, tvTime, tvGroupNum, tvDistance, tvPrice;
    private ImageView ivGroupNum, ivTime, ivPrice, ivSave, ivFavorite;
    private RatingBar moveRating;
    private Button btnChooseMove, btnFavorite, btnSave, btnAddToTrip;

    private ParseUser currUser;
    private Move move;
    private Restaurant restaurant;
    private Event event;
    private boolean isTrip;
    private List<Move> selectedMoves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_details);

        if (this.move != null) {
            if (this.move.getDidSave() == true) {
                ivSave.setImageResource(R.drawable.ufi_save_active);
            } else {
                ivSave.setImageResource(R.drawable.ufi_save);
            }
        }


        getViewIds();

        setupButtons();

        lyftButton();

        getMove();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void getMove() {
        move = Parcels.unwrap(getIntent().getParcelableExtra("move"));
        if (move.getMoveType() == Move.RESTAURANT) {
            restaurant = (Restaurant) move;
            getFoodView();
        } else {
            event = (Event) move;
            getEventView();
        }

        isTrip = getIntent().getBooleanExtra("isTrip", false);
        btnAddToTrip.setVisibility(isTrip ? View.VISIBLE : View.INVISIBLE);
        if (isTrip) {
            selectedMoves = TripActivity.selectedMoves;

            if (selectedMoves.contains(move)) {
                btnAddToTrip.setText("Remove From Trip");
            } else {
                btnAddToTrip.setText("Add To Trip");
            }
        }
    }

    private void getViewIds() {
        tvMoveName = findViewById(R.id.tvMoveName);
        tvTime = findViewById(R.id.tvTime);
        ivTime = findViewById(R.id.ivTime);
        tvGroupNum = findViewById(R.id.tvGroupNum);
        tvDistance = findViewById(R.id.tvDistance);
        tvPrice = findViewById(R.id.tvPrice);
        ivPrice = findViewById(R.id.ivPrice);
        moveRating = findViewById(R.id.moveRating);
        btnChooseMove = findViewById(R.id.btnChooseMove);
        ivGroupNum = findViewById(R.id.ivGroupNum);
        btnFavorite = findViewById(R.id.btnFavorite);
        ivFavorite = findViewById(R.id.ivFavorite);
        btnSave = findViewById(R.id.btnSave);
        currUser = ParseUser.getCurrentUser();
        ivSave = findViewById(R.id.ivSave);

        btnAddToTrip = findViewById(R.id.btnAddToTrip);
    }

    private void getFoodView() {
         tvMoveName.setText(restaurant.name);

         if (restaurant.lat == null) {
             getFoodDetails();
             return;
         }

          String price = "";
          if (restaurant.price_level < 0) {
              price = "Unknown";
          } else {
              for (int i = 0; i < restaurant.price_level; i++) {
                  price += '$';
              }
          }
          tvPrice.setText(price);

          //hide groupNum and Time tv & iv
          ivGroupNum.setVisibility(View.INVISIBLE);
          tvGroupNum.setVisibility(View.INVISIBLE);
          ivTime.setVisibility(View.INVISIBLE);
          tvTime.setVisibility(View.INVISIBLE);

          tvDistance.setText(restaurant.distanceFromLocation(getApplicationContext()) + " mi");

          if (restaurant.rating < 0) {
              moveRating.setVisibility(View.INVISIBLE);
          } else {
              float moveRate = restaurant.rating.floatValue();
              moveRating.setRating(moveRate = moveRate > 0 ? moveRate / 2.0f : moveRate);
          }
    }

    private void getFoodDetails() {
        String apiUrl = "https://maps.googleapis.com/maps/api/place/details/json";

        RequestParams params = new RequestParams();
        params.put("placeid", restaurant.id);
        params.put("key", getString(R.string.api_key));

        HomeActivity.client.get(apiUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                JSONObject result;
                try {
                    result = response.getJSONObject("result");

                    Restaurant restaurantResult = Restaurant.fromJSON(result);
                    restaurant = restaurantResult;

                    if (restaurant.lat != null) {
                        getFoodView();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error getting restaurant");

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e(TAG, errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e(TAG, errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e(TAG, responseString);
                throwable.printStackTrace();
            }
        });
    }

    private void getEventView() {
        tvMoveName.setText(event.name);
        String id = event.id;

        //hide groupNum and Time tv & iv
        ivGroupNum.setVisibility(View.INVISIBLE);
        tvGroupNum.setVisibility(View.INVISIBLE);

        // make call to Ticketmaster's event detail API
        getEventDetails(id);
    }

    private void getEventDetails(String id) {
        String API_BASE_URL_TMASTER = "https://app.ticketmaster.com/discovery/v2/events";
        String apiUrl = API_BASE_URL_TMASTER + "/" + id + ".json";

        RequestParams params = new RequestParams();
        params.put("apikey", getString(R.string.api_key_tm));

        HomeActivity.clientTM.get(apiUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                 String startTime = getStartTime(response);
                 String priceRange = getPriceRange(response);
                 tvTime.setText(startTime);
                 tvPrice.setText(priceRange);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.e(TAG, errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, responseString);
                throwable.printStackTrace();
            }
        });
    }

    private void setupButtons() {
        btnChooseMove.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (move != null) {
                     // TODO:
                     ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Move")
                             .whereEqualTo("placeId", move.getId())
                             .whereEqualTo("user", currUser);
                     parseQuery.findInBackground(new FindCallback<ParseObject>() {
                         @Override
                         public void done(List<ParseObject> objects, ParseException e) {
                             if (e == null) {
                                 if (move.getMoveType() == 1) { // 1 means restaurant
                                     currUser.addAllUnique("restaurantsCompleted", Arrays.asList(move.getName()));
                                 } else {
                                     currUser.addAllUnique("eventsCompleted", Arrays.asList(move.getName()));
                                 }
                                 currUser.saveInBackground();
                                 if (objects.size() == 0) { // occurs if the user has not ever completed this move
                                     ParseObject currObj = new ParseObject("Move");
                                     currObj.put("name", move.getName());
                                     currObj.put("placeId", move.getId());
                                     currObj.put("moveType", (move.getMoveType() == 1) ? "food" : "event");
                                     currObj.put("user", currUser);
                                     currObj.put("didComplete", true);
                                     currObj.saveInBackground();
                                 } else { // the user has already completed the move
                                     for (int i = 0; i < objects.size(); i++) {
                                         objects.get(i).put("didComplete", true);
                                         objects.get(i).put("didSave", false); // user cannot save a move that has been done
                                         ivSave.setImageResource(R.drawable.ufi_save);
                                         objects.get(i).saveInBackground();
                                     }
                                 }
                                 Log.d("Move", "Move saved in History Successfully");
                             } else {
                                 Log.d("Move", "Error: saving move to history");
                             }
                         }
                     });
                 }
             }
         });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (move != null) {
                    ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Move");
                    parseQuery.whereEqualTo("placeId", move.getId());
                    parseQuery.whereEqualTo("user", currUser);
                    parseQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (objects.size() > 0) {
                                for (int i = 0; i < objects.size(); i++) {
                                    if (objects.get(i).getBoolean("didComplete") == true) {
                                        Toast.makeText(MoveDetailsActivity.this, "You cannot save a move you have already completed!",
                                                Toast.LENGTH_SHORT).show(); return;
                                    }
                                    if (objects.get(i).getBoolean("didSave") == true){
                                        objects.get(i).put("didSave", false);
                                        ivSave.setImageResource(R.drawable.ufi_save);
                                        objects.get(i).saveInBackground();
                                    } else {
                                        objects.get(i).put("didSave", true);
                                        ivSave.setImageResource(R.drawable.ufi_save_active);
                                        objects.get(i).saveInBackground();
                                    }

                                }
                            } else {
                                ivSave.setImageResource(R.drawable.ufi_save_active);
                                ParseObject currObj = new ParseObject("Move");
                                currObj.put("name", move.getName());
                                currObj.put("user", currUser);
                                currObj.put("didSave", true);
                                currObj.put("didFavorite", false);
                                currObj.put("moveType", (move.getMoveType() == 1 ) ? "food" : "event");
                                currObj.put("placeId", move.getId());
                                currObj.put("didComplete", false);
                                currObj.saveInBackground();
                            }
                        }
                    });
                }
            }
        });

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (restaurant != null) {
                    ParseQuery didFavoriteQuery = ParseUtil.getParseQuery("food", currUser, restaurant);
                    didFavoriteQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (objects.size() > 0) {
                                for (int i = 0; i < objects.size(); i++) {
                                    if (objects.get(i).getBoolean("didComplete") == false) {
                                        Toast.makeText(MoveDetailsActivity.this, "You must complete the move before liking it!",
                                                Toast.LENGTH_SHORT).show(); return;
                                    }
                                    if (objects.get(i).getBoolean("didFavorite") == true){
                                        ivFavorite.setImageResource(R.drawable.ufi_heart);
                                        objects.get(i).put("didFavorite", false);
                                        objects.get(i).saveInBackground();
                                    } else {
                                        objects.get(i).put("didFavorite", true);
                                        ivFavorite.setImageResource(R.drawable.ufi_heart_active);
                                        objects.get(i).saveInBackground();
                                    }
                                }
                            } else {
                                return;
                            }
                        }
                    });
                }
                if (event != null) {
                    ParseQuery didFavoriteQuery = ParseUtil.getParseQuery("event", currUser, event);
                    didFavoriteQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (objects.size() > 0) {
                                for (int i = 0; i < objects.size(); i++) {
                                    if (objects.get(i).getBoolean("didFavorite") == true){
                                        ivFavorite.setImageResource(R.drawable.ufi_heart);
                                        objects.get(i).put("didFavorite", false);
                                        objects.get(i).saveInBackground();
                                    } else {
                                        objects.get(i).put("didFavorite", true);
                                        ivFavorite.setImageResource(R.drawable.ufi_heart);
                                        objects.get(i).saveInBackground();
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });

        btnAddToTrip.setOnClickListener(view -> saveToTrip());
    }

    private void saveToTrip() {
        if (!selectedMoves.contains(move)) {
            selectedMoves.add(move);
            btnAddToTrip.setText("Remove From Trip");
        } else {
            selectedMoves.remove(move);
            btnAddToTrip.setText("Add To Trip");
        }
    }
                               
    private void lyftButton() {
        // add feature to call Lyft to event/restaurant
        ApiConfig apiConfig = new ApiConfig.Builder()
                .setClientId(getString(R.string.client_id_lyft))
                //waiting for Lyft to approve developer signup request
                .setClientToken("...")
                .build();

        LyftButton lyftButton = findViewById(R.id.lyft_button);
        lyftButton.setApiConfig(apiConfig);
        UserLocation currLocation = UserLocation.getCurrentLocation(this);

        RideParams.Builder rideParamsBuilder = new RideParams.Builder()
                .setPickupLocation(Double.valueOf(currLocation.lat), Double.valueOf(currLocation.lng))
                //TODO: add correct dropoff location once Lyft approves developer request
                .setDropoffLocation(37.759234, -122.4135125);
        rideParamsBuilder.setRideTypeEnum(RideTypeEnum.STANDARD);

        lyftButton.setRideParams(rideParamsBuilder.build());
        lyftButton.load();
    }
}
