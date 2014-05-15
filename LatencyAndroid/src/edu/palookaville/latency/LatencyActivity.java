package edu.palookaville.latency;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
    private static final UUID SECURE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private TextView mTextViewStrength;
    private BluetoothDevice toConnect;
    private BluetoothAdapter bluetoothAdapter;
    private byte[] latencyData =
            ("" +
                    "measure l" +
                    "atency fo" +
                    "r this me" +
                    "ssage").getBytes();
    private byte[] readData = new byte[latencyData.length];
    private BluetoothSocket socket;
    private BroadcastReceiver receiver;

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
        showChooserForOperation(deviceList, connect());
    }

    public void onScanClick(View v) {
        ArrayList<BluetoothDevice> deviceList = getBluetoothDevices();
        showChooserForOperation(deviceList, scanForStrength());
    }

    private Runnable scanForStrength() {
        return new Runnable() {
            @Override
            public void run() {
                final Runnable repeat = new Runnable() {
                    @Override
                    public void run() {
                        bluetoothAdapter.cancelDiscovery();
                        try {
                            Thread.sleep(5000l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bluetoothAdapter.startDiscovery();
                    }
                };

                receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle extras = intent.getExtras();
                        BluetoothDevice device = (BluetoothDevice) extras.get(BluetoothDevice.EXTRA_DEVICE);
                        String address = toConnect.getAddress();
                        if (device.getAddress().equals(address)) {
                            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                                reportStrength(extras.get(BluetoothDevice.EXTRA_RSSI));
                                repeat.run();
                            }
                        }
                    }
                };
                registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));


                bluetoothAdapter.startDiscovery();
            }
        };
    }

    private void showChooserForOperation(final ArrayList<BluetoothDevice> deviceList, final Runnable forOperation) {
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
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (toConnect != null) {
                    new Thread(forOperation).start();
                }
            }
        });
        dialog.show();
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
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
        }

        socket = null;
        bluetoothAdapter = null;
    }

    private Runnable connect() {
        Runnable connector = new Runnable() {
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
                        while (totalReceivedBytes < dataToSend.length) {
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
        };
        return connector;
    }

    private void debug(String msg) {
        Log.d(this.getClass().getName(), msg);
    }

    private void report(final long nanos) {
        addToWindow(nanos);

        final BigDecimal millisBigDecimal = new BigDecimal(averageOfWindow() / 1000000.0).setScale(2, RoundingMode.HALF_EVEN);
        final BigDecimal millisPerByte = millisBigDecimal.setScale(10).divide(BigDecimal.valueOf(latencyData.length)).setScale(4, RoundingMode.HALF_EVEN);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextViewStrength.setText(
                        getResources().getString(R.string.millisReport, millisBigDecimal.toString(), millisPerByte.toString()));
            }
        });
    }

    private void reportStrength(final Object rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextViewStrength.setText(
                        getResources().getString(R.string.rssiReport, rssi.toString()));
            }
        });
    }

    long[] window = new long[50];
    int windowMarker = 0;

    private double averageOfWindow() {
        long sumL = 0;
        int count = 0;
        for (long l : window) {
            if (l > 0) {
                sumL += l;
                count++;
            }
        }
        return (double) sumL / count;
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
