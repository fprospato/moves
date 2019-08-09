package com.hkim00.moves.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import static com.hkim00.moves.util.JSONResponseHelper.getPriceRange;
import static com.hkim00.moves.util.JSONResponseHelper.getStartDate;
import static com.hkim00.moves.util.JSONResponseHelper.getStartTime;

@Parcel
public class Event extends Move {
    public int count;
    public String name, id, startDate, startTime, priceRange, venueName, venuePhotoUrl;
    public Boolean didSave, didFavorite, didComplete;

    public Event() {
        super();
        super.moveType = "event";
    }

    public void fromDetailsJSON(JSONObject jsonObject) throws JSONException {
        super.name = jsonObject.getString("name");
        super.id = jsonObject.getString("id");

        this.startDate = getStartDate(jsonObject);
        this.startTime = getStartTime(jsonObject);

        this.priceRange = getPriceRange(jsonObject);

        this.lat = jsonObject.getJSONObject("_embedded").getJSONArray("venues").getJSONObject(0).getJSONObject("location").getDouble("latitude");
        this.lng = jsonObject.getJSONObject("_embedded").getJSONArray("venues").getJSONObject(0).getJSONObject("location").getDouble("longitude");

        if (jsonObject.has("seatmap")) {
            venueName = jsonObject.getJSONObject("_embedded").getJSONArray("venues").getJSONObject(0).getString("name");
            venuePhotoUrl = jsonObject.getJSONObject("seatmap").getString("staticUrl");
        }
    }


}
