package wit.ie.invasivespeciestracker.util;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import wit.ie.invasivespeciestracker.model.Species;

/**
 * Utility class for operations with location lists
 */
public final class LocationUtil {


    /**
     * Convert List< LatLng >} into a Json string.
     */
    public static String locationsToJson(List<LatLng> markers) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(markers);
    }

    /**
     * Convert Json string into {@link List< LatLng >}.
     */
    public static List<LatLng> jsonToLocations(String json) {
        Gson g = new GsonBuilder().create();
        Type listType = new TypeToken<ArrayList<LatLng>>() {
        }.getType();
        return g.fromJson(json, listType);
    }

    /**
     * Makes a readable string out of location list and displays it in view.
     */
    public static String getUserFriendlyLocationString(List<LatLng> markers) {
        if (markers == null || markers.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        if (!markers.isEmpty()) {
            for (LatLng latLng : markers) {
                sb.append("(");
                sb.append(String.format("%.3f", latLng.latitude));
                sb.append(", ");
                sb.append(String.format("%.3f", latLng.longitude));
                sb.append("); ");
            }
            sb.replace(sb.length() - 2, sb.length(), "");
        }

        sb.append("]");

        return sb.toString();
    }

    /**
     * Add markers on map and zoom map view to see all markers
     *
     * @param map - {@link GoogleMap} to add markers on
     * @param markers - {@link List< LatLng >} locations to make markers from
     */
    public static void renderMarkersListToMap(GoogleMap map, List<LatLng> markers) {
        if (!markers.isEmpty()) {
            // Update map markers
            map.clear();

            // need to focus map on locations
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (LatLng latLng : markers) {
                // add each marker on map.
                map.addMarker(new MarkerOptions().position(latLng));
                builder.include(latLng);
            }

            // zoom and focus map on all available locations
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
            map.moveCamera(cu);
        }
    }

    /**
     * Add markers from all species to map. including title and description.
     *
     * @param map - {@link GoogleMap} to add markers on
     * @param species - {@link List< Species >} Species to get locations from
     */
    public static void renderSpeciesMarkersToList(GoogleMap map, List<Species> species) {
        if(!species.isEmpty()) {
            map.clear();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Species s : species) {
                try {
                    List<LatLng> markers = LocationUtil.jsonToLocations(s.getLocation());
                    for (LatLng latLng : markers) {
                        map.addMarker(new MarkerOptions().position(latLng)
                                .title(s.getSpecies())
                                .snippet(s.getAdditionalInfo()));

                        builder.include(latLng);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                LatLngBounds bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                map.animateCamera(cu);
            }
        }
    }
}
