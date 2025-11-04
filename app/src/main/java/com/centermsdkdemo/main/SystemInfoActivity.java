package com.centermsdkdemo.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.centermsdkdemo.R;
import com.pos.sdk.DeviceManager;
import com.pos.sdk.DevicesFactory;
import com.pos.sdk.callback.ResultCallback;
import com.pos.sdk.sys.SystemDevice;

public class SystemInfoActivity extends Activity {
    private TextView tvSystemInfo;
    private SystemDevice systemDevice;
    private static final String TAG = "SystemInfoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_info);

        tvSystemInfo = findViewById(R.id.tv_system_info);

        // Set click listener programmatically to avoid XML issues
        findViewById(R.id.btn_refresh).setOnClickListener(v -> {
            if (systemDevice != null) {
                fetchSystemInfo();
            } else {
                initializeSDK();
            }
        });

        // Set click listener for print test button (only if it exists in layout)
        findViewById(R.id.btn_print_test).setOnClickListener(v -> {
            Intent intent = new Intent(SystemInfoActivity.this, PrintTestActivity.class);
            startActivity(intent);
        });

        initializeSDK();
    }

    private void initializeSDK() {
        Log.d(TAG, "Starting SDK initialization...");
        tvSystemInfo.setText("Starting SDK initialization...");

        try {
            DevicesFactory.create(this, new ResultCallback<DeviceManager>() {
                @Override
                public void onFinish(DeviceManager deviceManager) {
                    Log.d(TAG, "SDK initialized successfully!");
                    runOnUiThread(() -> {
                        tvSystemInfo.setText("SDK initialized successfully!\nGetting system device...");
                    });

                    systemDevice = deviceManager.getSystemDevice();
                    if (systemDevice != null) {
                        Log.d(TAG, "System device obtained successfully");
                        fetchSystemInfo();
                    } else {
                        Log.e(TAG, "Failed to get system device");
                        runOnUiThread(() -> {
                            tvSystemInfo.setText("Failed to get System Device\nDeviceManager returned null");
                        });
                    }
                }

                @Override
                public void onError(int errorCode, String errorMessage) {
                    Log.e(TAG, "SDK Initialization Failed - Code: " + errorCode + ", Message: " + errorMessage);
                    runOnUiThread(() -> {
                        tvSystemInfo.setText("SDK Initialization Failed!\n" +
                                "Error Code: " + errorCode + "\n" +
                                "Error Message: " + errorMessage + "\n\n" +
                                "Possible issues:\n" +
                                "• SDK JAR not properly integrated\n" +
                                "• Missing permissions\n" +
                                "• Device not supported");
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception during SDK initialization: " + e.getMessage(), e);
            tvSystemInfo.setText("Exception during SDK initialization:\n" + e.getMessage());
        }
    }

    private void fetchSystemInfo() {
        Log.d(TAG, "Fetching system info...");
        runOnUiThread(() -> {
            if (systemDevice != null) {
                try {
                    tvSystemInfo.setText("Fetching system information...");

                    StringBuilder infoBuilder = new StringBuilder();
                    infoBuilder.append("Terminal System Information\n\n");

                    // Check if SystemInfoType enum exists and has values
                    try {
                        SystemDevice.SystemInfoType[] types = SystemDevice.SystemInfoType.values();
                        if (types.length == 0) {
                            infoBuilder.append("No system info types available\n");
                        } else {
                            for (SystemDevice.SystemInfoType type : types) {
                                String value = systemDevice.getSystemInfo(type);
                                infoBuilder.append(type.name())
                                        .append(": ")
                                        .append(value != null ? value : "N/A")
                                        .append("\n");
                            }
                        }
                    } catch (Exception e) {
                        infoBuilder.append("Error accessing SystemInfoType: ").append(e.getMessage()).append("\n");
                        Log.e(TAG, "Error with SystemInfoType", e);
                    }

                    tvSystemInfo.setText(infoBuilder.toString());
                    Log.d(TAG, "System info fetched successfully");

                } catch (Exception e) {
                    String errorMsg = "Error fetching system info: " + e.getMessage();
                    tvSystemInfo.setText(errorMsg);
                    Log.e(TAG, errorMsg, e);
                }
            } else {
                tvSystemInfo.setText("System Device is null - SDK may not be initialized properly");
                Log.e(TAG, "System Device is null");
            }
        });
    }
}
