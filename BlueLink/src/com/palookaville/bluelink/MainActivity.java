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
    
    
    TextView textViewStatus;
    TextView textViewDisplay;
    ActionBar actionBar;
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
		
	}
	
	@Override
	public void onStart(){
	    super.onStart();
	    init();   
	}
	
	void init(){
		textViewStatus = (TextView) findViewById(R.id.statusView);
		textViewDisplay = (TextView) findViewById(R.id.outputView);
		
		//Toast.makeText(getApplicationContext(), "init()", Toast.LENGTH_SHORT).show();		
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
//		actionBar = getActionBar();
//		actionBar.show();
		Toast.makeText(getApplicationContext(), "action bar init()", Toast.LENGTH_SHORT).show();
		return super.onCreateOptionsMenu(menu);
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
	 * Control logic
	 * 
	 ************************************************/
	
	
    public void onRunLinkClick(View v) {
		Toast.makeText(getApplicationContext(), "onRunLinkClick", Toast.LENGTH_LONG).show();
		link.activate(this);
    }
    
    public void onSend1Click(View v) {
		Toast.makeText(getApplicationContext(), "onSend1Click", Toast.LENGTH_LONG).show();
		link.postMessageout("1");
    }
    
    public void onSend2Click(View v) {
		Toast.makeText(getApplicationContext(), "onSend2Click", Toast.LENGTH_LONG).show();
		link.postMessageout("2");
    }
	
    public void onSend3Click(View v) {
		Toast.makeText(getApplicationContext(), "onSend3Click", Toast.LENGTH_LONG).show();
		link.postMessageout("3");
    }
	
    public void onStatusClick(View v) {
//    	actionBar = this.getActionBar();
//    	String foo = actionBar == null ? "missing" : "present";
//    	Toast.makeText(getApplicationContext(), "onStatusClick()"+foo, Toast.LENGTH_SHORT).show();

    	//actionBar.show();
    	textViewStatus.setText("Status Display");
    }
	
	
		
	
    public void onPairClick(View v) {
        final ArrayList<BluetoothDevice> deviceList = getBluetoothDevices();
        if (deviceList == null) {
        	Toast.makeText(getApplicationContext(), "No device list", Toast.LENGTH_LONG).show();
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
    
    public void display(final String msg){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewDisplay.append(msg + "\n");
            }
        });
    }


    void debug(String msg) {
        Log.d(this.getClass().getName(), msg);
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
