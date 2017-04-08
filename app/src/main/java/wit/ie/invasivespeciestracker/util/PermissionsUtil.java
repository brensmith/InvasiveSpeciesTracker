package wit.ie.invasivespeciestracker.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Utility class to work with runtime permissions
 */

public class PermissionsUtil {

    /**
     * Checks {@link Manifest.permission#ACCESS_FINE_LOCATION} and {@link Manifest.permission#ACCESS_COARSE_LOCATION}
     * and requests them if not allowed yet.
     *
     * @param activity - {@link Activity}to start check and request flow
     * @return true - if permissions allowed; false - if not
     */
    public static boolean checkAndRequestLocationPermission(Activity activity) {

        // Check if runtime location permissions are allowed.
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Toast.makeText(activity, "to find your location add application permissions", Toast.LENGTH_LONG)
                        .show();
            } else {

                // Request the permission
                ActivityCompat.requestPermissions((Activity) activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        }
        return true;
    }
}
