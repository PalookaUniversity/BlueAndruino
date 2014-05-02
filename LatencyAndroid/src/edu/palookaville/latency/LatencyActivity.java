package edu.palookaville.latency;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class LatencyActivity extends Activity {
    private static final int DEVICE_CHOOSER_RESULT_CODE = 1904152098;
    private static final UUID SECURE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private TextView mTextViewStrength;
    private BluetoothDevice toConnect;
    private BluetoothAdapter bluetoothAdapter;
    private byte[] latencyData = "measure latency for this message".getBytes();
    private byte[] readData = new byte[latencyData.length];
    private BluetoothSocket socket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTextViewStrength = (TextView) findViewById(R.id.strength);
    }

    public void onPairClick(View v) {
        final ArrayList<BluetoothDevice> deviceList = getBluetoothDevices();
        if (deviceList == null) {
            return;
        }
        showChooserAndConnect(deviceList);
    }

    private void showChooserAndConnect(final ArrayList<BluetoothDevice> deviceList) {
        ArrayList<CharSequence> itemList = new ArrayList<CharSequence>();
        for (BluetoothDevice device : deviceList) {
            itemList.add(device.getName() + " [" + device.getAddress() + "]");
        }
        CharSequence[] items = itemList.toArray(new CharSequence[itemList.size()]);

        toConnect = null;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toConnect = deviceList.get(which);
                    }
                })
                .create();
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (toConnect != null) {
                    connect();
                }
            }
        });
    }

    private ArrayList<BluetoothDevice> getBluetoothDevices() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            alarm(R.string.noBtOnDevice);
            return null;
        }

        if (!bluetoothAdapter.isEnabled()) {
            alarm(R.string.btDisabled);
            return null;
        }

        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        if (deviceSet.isEmpty()) {
            alarm(R.string.pairFirst);
            return null;
        }

        if (deviceSet == null) {
            return null;
        }
        final ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>(deviceSet);
        return deviceList;
    }

    public void onStopClick(View v) throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    private void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.cancelDiscovery();
                try {
                    socket = toConnect.createRfcommSocketToServiceRecord(SECURE_UUID);
                    socket.connect();
                    while (socket.isConnected()) {

                        long nanoStart = System.nanoTime();
                        byte[] dataToSend = latencyData;
                        socket.getOutputStream().write(dataToSend);

                        int totalReceivedBytes = 0;
                        while(totalReceivedBytes < dataToSend.length) {
                            int numRead = socket.getInputStream().read(readData);
                            debug("read " + numRead + " bytes");
                            totalReceivedBytes += numRead;
                        }

                        report(System.nanoTime() - nanoStart);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (socket != null) {
                        try {
                            socket.getInputStream().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.getOutputStream().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewStrength.append(" " + getString(R.string.disconnected));
                    }
                });
            }
        }).start();
    }

    private void debug(String msg) {
        Log.d(this.getClass().getName(), msg);
    }

    private void report(final long nanos) {
        addToWindow(nanos);

        final BigDecimal millisBigDecimal = new BigDecimal(averageOfWindow() / 1000000.0).setScale(2, RoundingMode.HALF_EVEN);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextViewStrength.setText(getResources().getString(R.string.millisReport, millisBigDecimal.toString()));
            }
        });
    }

    long[] window = new long[50];
    int windowMarker = 0;

    private double averageOfWindow() {
        long sumL = 0;
        for (long l : window) {
            sumL += l;
        }
        return (double) sumL / window.length;
    }

    private void addToWindow(long nanos) {
        window[windowMarker++] = nanos;
        windowMarker = windowMarker % window.length;
    }

    public void alarm(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LatencyActivity.this, resourceId, Toast.LENGTH_LONG).show();
            }
        });
    }
}
