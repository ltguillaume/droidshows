/*
 * Copyright (C) 2017-2025 k3b
 *
 * This file is part of nl.asymmetrics.droidshows (https://github.com/ltguillaume/droidshows) .
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package nl.asymmetrics.droidshows.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.Toast;

import nl.asymmetrics.droidshows.R;

/**
 * Support for android-6.0 (M) ff runtime permissons.
 * Created by k3b on 23.12.2017.
 *
 * implements ActivityCompat.OnRequestPermissionsResultCallback
 */

public class PermissionHelper {
    /**
     * Permissions required to read and write Storage.
     */
    private static final String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //** if true use Permissoins and DocumentFileApi. if false : old android devices - no permission api required
    public static final boolean USE_NEW_PERMISSIONS_FILE_API = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    private PermissionHelper() {/*hide public constructor*/};
    /**
     * called before permission is required.
     *
     * @return true if has-permissions. else false and request permissions
     * */
    public static boolean hasPermissionOrRequest(Activity context, int requestCode) {
        if (!hasPermission(context)) {
            // Storage permission has not been requeste yet. Request for first time.
            ActivityCompat.requestPermissions(context, PERMISSIONS_STORAGE, requestCode);
            // no permission yet
            return false;
        } // if android-m

        // already has permission.
        return true;
    }

    /**
     * @return true if has-(runtime-)permissions.
     * */
    public static boolean hasPermission(Activity context) {
        if (USE_NEW_PERMISSIONS_FILE_API) {
            boolean needsRead = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED;

            boolean needsWrite = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED;

            if (needsRead || needsWrite) {
                // no permission yet
                return false;
            }
        } // if android-m

        // already has permission.
        return true;
    }

    /**
     * called in onRequestPermissionsResult().
     *
     * @return true if just received permissions. else false and calling finish
     */
    public static boolean receivedPermissionsOrFinish(Activity activity,
                                                      @NonNull int[] grantResults) {
        if ( (grantResults.length == 2)
              && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
              && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            return true;
        } else {
            showNowPermissionMessage(activity);
            activity.finish();
        }
        return false;
    }

    public static void showNowPermissionMessage(Activity activity) {
        String format = activity.getString(R.string.messages_no_write_permissions_err);

        String msg = String.format(
                format,
                "",
                "");

        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
    }
}
