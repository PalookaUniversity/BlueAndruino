package com.palookaville.bluelink;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends Activity {
    private static final UUID SECURE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    
    TextView mTextViewStrength;
//    private BluetoothDevice toConnect;
//    private BluetoothAdapter bluetoothAdapter;
    private byte[] latencyData = "measure latency for this message".getBytes();
    byte[] readData = new byte[latencyData.length];
    private BluetoothSocket socket;
    Link link;
    


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		link = AppManager.instance.link;
		link.setActivity(this);
		mTextViewStrength = (TextView) findViewById(R.id.strength);
		Toast.makeText(getApplicationContext(), "onCreate", Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	/***********************************************
	 * 
	 * 
	 * 
	 * 
	 ************************************************/
	
	
    public void onRunLinkClick(View v) {
		Toast.makeText(getApplicationContext(), "onRunLinkClick", Toast.LENGTH_LONG).show();
		link.activate(this);
    }
    
    public void onSend1Click(View v) {
		Toast.makeText(getApplicationContext(), "onSend1Click", Toast.LENGTH_LONG).show();
		link.postMessageout("1");
		//link.writeImmediate("3");
    }
    
    public void onSend2Click(View v) {
		Toast.makeText(getApplicationContext(), "onSend2Click", Toast.LENGTH_LONG).show();
		link.postMessageout("2");
		//link.writeImmediate("1");
    }
	
    public void onSend3Click(View v) {
		Toast.makeText(getApplicationContext(), "onSend3Click", Toast.LENGTH_LONG).show();
		link.postMessageout("3");
    }
	
    public void onStatusClick(View v) {
		Toast.makeText(getApplicationContext(), "onStatusClick", Toast.LENGTH_LONG).show();
    }
	
	
		
	
    public void onPairClick(View v) {
        final ArrayList<BluetoothDevice> deviceList = getBluetoothDevices();
        if (deviceList == null) {
        	Toast.makeText(getApplicationContext(), "No device list", Toast.LENGTH_LONG).show();
            return;
        }
        showChooserAndConnect(deviceList);
    }
    
    public void onClickTestLatency(View v){
        if (link.getToConnect() != null) {
            runLatencyTest();
        } else {
        	Toast.makeText(getApplicationContext(), "No Connection", Toast.LENGTH_LONG).show();
        }
    }

    private void showChooserAndConnect(final ArrayList<BluetoothDevice> deviceList) {
        ArrayList<CharSequence> itemList = new ArrayList<CharSequence>();
        for (BluetoothDevice device : deviceList) {
            itemList.add(device.getName() + " [" + device.getAddress() + "]");
        }
        CharSequence[] items = itemList.toArray(new CharSequence[itemList.size()]);

        link.setToConnect(null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        link.setToConnect(deviceList.get(which));  
                    }
                })
                .create();
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (link.getToConnect() != null) {
                	Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                    //connect();
                }
            }
        });
    }

    private ArrayList<BluetoothDevice> getBluetoothDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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

    
    private void runLatencyTest() {
    	Toast.makeText(getApplicationContext(), "latency test clicked", Toast.LENGTH_LONG).show();
    /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                link.getBluetoothAdapter().cancelDiscovery();
                try {

                    socket = link.getBluetoothSocket();
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
        */
    }
    

    void debug(String msg) {
        Log.d(this.getClass().getName(), msg);
    }

    void report(final long nanos) {
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
                Toast.makeText(MainActivity.this, resourceId, Toast.LENGTH_LONG).show();
            }
        });
    }	

}
