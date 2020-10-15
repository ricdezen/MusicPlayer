package com.dezen.riccardo.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.dezen.riccardo.musicplayer.utils.NotificationHelper;

/**
 * Main Activity of the app, asks for permissions and shows the song list fragment.
 *
 * @author Riccardo De Zen.
 */
public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 0;

    private PlayerClient playerClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        ensureNotificationServiceExists();
        playerClient = PlayerClient.of(this);
        if (checkPermissions()) {
            init();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        playerClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        playerClient.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            init();
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions;
            if (Build.VERSION.SDK_INT < 28)
                permissions = new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
            else
                permissions = new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.FOREGROUND_SERVICE
                };
            ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    PERMISSION_REQUEST
            );
            return false;
        }
        return true;
    }

    private void ensureNotificationServiceExists() {
        try {
            NotificationHelper.getInstance(this);
        } catch (NullPointerException e) {
            Toast.makeText(
                    this,
                    R.string.no_notification_service_error,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void init() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new SongListFragment());
        transaction.commit();
    }
}
