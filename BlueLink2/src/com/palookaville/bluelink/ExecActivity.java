package com.palookaville.bluelink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**********************************************************************
 * 
 * TODO:
 *   Modify blink mode:
 *   -- Send message mod:  Blink rate roller, Send button.
 *   Add command builder
 *   -- Init command (clear)
 *   -- Roller for register, field for value, "add" button
 *   -- "Send message"
 *   Foo Bar Baz
 * 
 * @author gvamos
 *
 */

public class ExecActivity extends Activity {
	
    private static final UUID SECURE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
      
    TextView textViewStatus;
    TextView textViewDisplay;
    EditText editTextCommand;
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
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
		link = AppManager.instance.link;
		link.setActivity(this);
    }
    
	@Override
	public void onStart(){
	    super.onStart();
		textViewStatus = (TextView) findViewById(R.id.statusView);
		textViewDisplay = (TextView) findViewById(R.id.outputView);
		editTextCommand = (EditText)findViewById(R.id.edit_text_cmd);
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	View v = getWindow().getDecorView().getRootView();
    	//Context ac = getApplicationContext();
    	//View view = (View)ac;
        switch(item.getItemId()){
        case R.id.action_status: 
//        	Toast.makeText(getApplicationContext(), "onStatusClick()", Toast.LENGTH_SHORT).show();
        	textViewStatus.setText("Status Display"); 
        	break;
        case R.id.action_pair: 
            final ArrayList<BluetoothDevice> deviceList = getBluetoothDevices();
            if (deviceList == null) {
            	Toast.makeText(getApplicationContext(), "No device list", Toast.LENGTH_LONG).show();
            } else {
            	showChooserAndConnect(deviceList);	
            }
            
        	break;
        case R.id.action_link: 
    		link.activate(this); 
        	break;
        case R.id.action_clear: 
    		textViewDisplay.setText(""); 
        	break;
        case R.id.action_config:
        	AppManager.instance.setMode("Config");
        	Toast.makeText(getApplicationContext(), "Config mode", Toast.LENGTH_SHORT).show();

        	break;
        case R.id.action_c2:
        	AppManager.instance.setMode("C2");
        	break;
        case R.id.action_quit: 
            if (socket != null) {
                try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            finish();
        	break;
        }

        
//        if (id == R.id.action_settings) {
//            return true;
//        }
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    
    
    
	
	/***********************************************
	 * 
	 * Control logic
	 * 
	 ************************************************/
    
    public void onSend1Click(View v) {
//		Toast.makeText(getApplicationContext(), "onSend1Click", Toast.LENGTH_LONG).show();
		link.postMessageout("1");
    }
    
    public void onSend2Click(View v) {
//		Toast.makeText(getApplicationContext(), "onSend2Click", Toast.LENGTH_LONG).show();
		link.postMessageout("2");
    }
	
    public void onSend3Click(View v) {
//		Toast.makeText(getApplicationContext(), "onSend3Click", Toast.LENGTH_LONG).show();
		link.postMessageout("3");
    }
    
    public void onClickExec(View v) {
    	String cmd = editTextCommand.getText().toString();
    	link.postMessageout(cmd);
    	String stat = "Exec<"+cmd+">";
    	textViewStatus.setText(stat);
		Toast.makeText(getApplicationContext(), stat, Toast.LENGTH_SHORT).show();
    }
    
    public void onClickAdd(View v) {
		Toast.makeText(getApplicationContext(), "onClickAdd"
				+ "", Toast.LENGTH_SHORT).show();
    }
    
    public void onClickCheck(View v) {
		Editable txt = editTextCommand.getText();
		String diagnostic = "<" + txt.toString().trim() + ">";
		textViewStatus.setText("Check:"+diagnostic); 
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
            alarm("No BT on device");
            return null;
        }

        if (!bluetoothAdapter.isEnabled()) {
            alarm("BT on device  disabled");
            return null;
        }
        
        

        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        if (deviceSet.isEmpty()) {
            alarm("Pair first");
            return null;
        }

        if (deviceSet == null) {
            return null;
        }
        final ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>(deviceSet);
        return deviceList;
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

    public void alarm(final String string) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ExecActivity.this, string, Toast.LENGTH_LONG).show();
            }
        });
    }	

}
