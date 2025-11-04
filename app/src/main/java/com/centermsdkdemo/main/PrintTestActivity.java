package com.centermsdkdemo.main;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.centermsdkdemo.R;
import com.pos.sdk.DeviceManager;
import com.pos.sdk.DevicesFactory;
import com.pos.sdk.callback.ResultCallback;
import com.pos.sdk.printer.PrinterDevice;
import com.pos.sdk.printer.PrinterState;
import com.pos.sdk.printer.param.PrintItemAlign;


public class PrintTestActivity extends Activity {
    private TextView tvPrintStatus;
    private Button btnPrintTest, btnCheckPrinter;
    private PrinterDevice printerDevice;
    private static final String TAG = "PrintTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_test);

        tvPrintStatus = findViewById(R.id.tv_print_status);
        btnPrintTest = findViewById(R.id.btn_print_test);
        btnCheckPrinter = findViewById(R.id.btn_check_printer);

        initializeSDK();

        btnCheckPrinter.setOnClickListener(v -> checkPrinterStatus());
        btnPrintTest.setOnClickListener(v -> printTestReceipt());
    }

    private void initializeSDK() {
        Log.d(TAG, "Initializing SDK for printer...");
        tvPrintStatus.setText("Initializing SDK...");

        DevicesFactory.create(this, new ResultCallback<DeviceManager>() {
            @Override
            public void onFinish(DeviceManager deviceManager) {
                Log.d(TAG, "SDK initialized successfully");
                runOnUiThread(() -> {
                    tvPrintStatus.setText("SDK Initialized\nGetting printer device...");
                });

                printerDevice = deviceManager.getPrintDevice();
                if (printerDevice != null) {
                    runOnUiThread(() -> {
                        tvPrintStatus.setText("Printer device ready!\nClick 'Check Printer' to verify status.");
                    });
                    Log.d(TAG, "Printer device obtained successfully");
                } else {
                    runOnUiThread(() -> {
                        tvPrintStatus.setText("Failed to get printer device");
                    });
                    Log.e(TAG, "Printer device is null");
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.e(TAG, "SDK Init Error: " + errorCode + " - " + errorMessage);
                runOnUiThread(() -> {
                    tvPrintStatus.setText("SDK Init Failed: " + errorMessage);
                });
            }
        });
    }

    private void checkPrinterStatus() {
        if (printerDevice == null) {
            tvPrintStatus.setText("Printer device not available");
            return;
        }

        try {
            PrinterState state = printerDevice.getPrinterState();
            String statusMessage = "Printer Status:\n" +
                    "State Code: " + state.getStateCode() + "\n" +
                    "State Message: " + state.getStateMsg() + "\n\n" +
                    "Common Status Codes:\n" +
                    "0: Ready\n" +
                    "1: Busy\n" +
                    "2: Out of Paper\n" +
                    "3: Overheated\n" +
                    "4: Other Error";

            tvPrintStatus.setText(statusMessage);
            Log.d(TAG, "Printer Status - Code: " + state.getStateCode() + ", Msg: " + state.getStateMsg());

        } catch (Exception e) {
            tvPrintStatus.setText("Error checking printer status: " + e.getMessage());
            Log.e(TAG, "Error checking printer status", e);
        }
    }

    private void printTestReceipt() {
        if (printerDevice == null) {
            tvPrintStatus.setText("Printer not available");
            return;
        }

        try {
            // Clear any previous print tasks
            printerDevice.clearBufferArea();

            // Set printer gray level for darker printing
            setPrinterGrayLevel();

            // Add content to print
            printerDevice.addTextPrintItem("TEST RECEIPT", 24, PrintItemAlign.CENTER);
            printerDevice.addTextPrintItem("CPay SDK Demo", 20, PrintItemAlign.CENTER);
            printerDevice.addTextPrintItem("----------------------------", 16, PrintItemAlign.CENTER);

            printerDevice.addTextPrintItem("Date: " + java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()), 16, PrintItemAlign.LEFT);
            printerDevice.addTextPrintItem("Device: Cpay Terminal", 16, PrintItemAlign.LEFT);
            printerDevice.addTextPrintItem("SDK Version: 4.0", 16, PrintItemAlign.LEFT);

            printerDevice.addTextPrintItem("----------------------------", 16, PrintItemAlign.CENTER);
            printerDevice.addTextPrintItem("This is a test receipt", 18, PrintItemAlign.CENTER);
            printerDevice.addTextPrintItem("from PrintDevice demo", 18, PrintItemAlign.CENTER);

            printerDevice.addTextPrintItem("----------------------------", 16, PrintItemAlign.CENTER);
            printerDevice.addTextPrintItem("Thank You!", 20, PrintItemAlign.CENTER);

            // Add some blank lines at the end
            printerDevice.addTextPrintItem("\n\n\n", 16, PrintItemAlign.CENTER);

            // Print synchronously
            PrinterState result = printerDevice.printSync(null);

            String printResult = "Print Result:\n" +
                    "State Code: " + result.getStateCode() + "\n" +
                    "State Message: " + result.getStateMsg() + "\n\n" +
                    (result.getStateCode() == 0 ? "✓ Print Successful" : "✗ Print Failed");

            tvPrintStatus.setText(printResult);
            Log.d(TAG, "Print result - Code: " + result.getStateCode() + ", Msg: " + result.getStateMsg());

        } catch (Exception e) {
            tvPrintStatus.setText("Print Error: " + e.getMessage());
            Log.e(TAG, "Print error", e);
        }
    }

    private void setPrinterGrayLevel() {
        if (printerDevice == null) return;

        try {
            // Set printer gray level (range: 0x10 to 0x40)
            // 0x10 = lightest, 0x40 = darkest
            printerDevice.setPrinterGray(0x30); // Medium-dark setting
            Log.d(TAG, "Printer gray level set to 0x30");
        } catch (Exception e) {
            Log.e(TAG, "Error setting printer gray level: " + e.getMessage());
        }
    }
}