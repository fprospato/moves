// May table Restaurant model

package com.hkim00.moves.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import com.parse.ParseObject;

@Parcel
public class Restaurant implements Move {

    public String name, id, text;
    public Integer price_level;
    public Double lat, lng, rating;

    public Restaurant() {}

    @Override
    public int getMoveType() {
        return Move.RESTAURANT;
    }

    public static Restaurant fromJSON(JSONObject jsonObject) throws JSONException {
        Restaurant restaurant = new Restaurant();

        restaurant.name = jsonObject.getString("name");
        restaurant.id = jsonObject.getString("id");

        restaurant.price_level = (jsonObject.has("price_level")) ? jsonObject.getInt("price_level") : -1;

        JSONObject location = jsonObject.getJSONObject("geometry").getJSONObject("location");

        restaurant.lat = location.getDouble("lat");
        restaurant.lng = location.getDouble("lng");

        restaurant.rating = (jsonObject.has("rating")) ? jsonObject.getDouble("rating") : -1;

        return restaurant;
    }

    public static Restaurant fromParseObject(ParseObject parseObject) {
        Restaurant restaurant = new Restaurant();

        restaurant.name = parseObject.getString("name");
        restaurant.id = parseObject.getString("id");

        restaurant.price_level = (Integer) parseObject.getNumber("price_level");

        restaurant.lat = (Double) parseObject.getNumber("lat");
        restaurant.lng = (Double) parseObject.getNumber("lng");
        restaurant.rating = (Double) parseObject.getNumber("rating");

        return restaurant;
    }
}
