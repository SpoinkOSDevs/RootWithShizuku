import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import dev.rikka.shizuku.api;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int SHIZUKU_REQUEST_CODE = 1;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 2;

    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        Button executeButton = findViewById(R.id.executeButton);

        executeButton.setOnClickListener(view -> {
            // Request Shizuku permissions
            requestShizukuPermissions();
        });
    }

    private void requestShizukuPermissions() {
        Intent shizukuIntent = new Intent("rikka.shizuku.intent.action.REQUEST_PERMISSIONS");
        shizukuIntent.setPackage("rikka.shizuku.privileged.api");
        shizukuIntent.putExtra("rikka.shizuku.intent.extra.REQUEST_PERMISSIONS", new String[]{
                "rikka.shizuku.manager.permission.API"
        });

        startActivityForResult(shizukuIntent, SHIZUKU_REQUEST_CODE);
    }

    private void requestStoragePermission() {
        Intent storageIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(storageIntent, STORAGE_PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHIZUKU_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Shizuku permissions granted, proceed with tasks
                executeRootingTasks();
            } else {
                showPermissionDeniedDialog("Shizuku");
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri treeUri = data.getData();
                // Handle external storage permission result
                // You can perform file operations using treeUri
                statusTextView.setText("Storage permission granted: " + treeUri);
                executeRootingTasks();
            } else {
                showPermissionDeniedDialog("Storage");
            }
        }
    }

    private void executeRootingTasks() {
        // SELinux Bypass
        executeShizukuCommand("setenforce 0");

        // Copy SU binary to /data/local/tmp
        executeShizukuCommand("cp /storage/emulated/0/su /data/local/tmp/su");
        executeShizukuCommand("chmod +x /data/local/tmp/su");

        // Copy SU binary from /data/local/tmp to /system/xbin
        executeShizukuCommand("cp /data/local/tmp/su /system/xbin/su");
        executeShizukuCommand("chmod 6755 /system/xbin/su");

        // Additional tasks if needed

        // Update status
        statusTextView.setText("Rooting tasks executed successfully!");
    }

    private void executeShizukuCommand(String command) {
        try {
            Shizuku.executeCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
            statusTextView.setText("Error executing command: " + e.getMessage());
        }
    }

    private void showPermissionDeniedDialog(String permissionType) {
        new AlertDialog.Builder(this)
                .setTitle("Permission Denied")
                .setMessage(permissionType + " permissions were not granted.")
                .setPositiveButton("OK", (dialog, which) -> statusTextView.setText(permissionType + " permission denied."))
                .show();
    }
}
