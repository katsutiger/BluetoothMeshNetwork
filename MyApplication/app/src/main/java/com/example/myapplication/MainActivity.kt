import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    /**
     * Tag for Log
     */
    private static final String TAG = "MainActivity";

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.main_layout, fragment);
            transaction.commit();
        }
        // Check if the user revoked runtime permissions.
        if (!checkPermissions()) {
            requestPermissions();
        }
    }
    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissionState = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT);
        }
        Log.i(TAG, "android.os.Build.VERSION.SDK_INT = " + android.os.Build.VERSION.SDK_INT + ",checkPermissions = " + permissionState);    //  PERMISSION_GRANTED = 0
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH_CONNECT);
        }

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {   // 今後は確認しない をチェックしているしているか判断する
            //  false ならば表示する必要が無いと判断でき、かつ「今後は確認しない」状態であると判断することができる
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_PERMISSIONS_REQUEST_CODE);
            }
        }
    }
}

