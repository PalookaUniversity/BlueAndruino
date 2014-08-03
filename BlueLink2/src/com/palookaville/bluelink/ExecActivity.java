package com.palookaville.bluelink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
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
    
	String serverUrl = "";
    
    Button buttonSetState;
    Button buttonReSyncServer;
    
    Button buttonExec;
    Button buttonAdd;
    Button buttonRunScript;
    Button buttonCheck;
    
    TextView selectedScript;
    
    Config config;
    String scriptUrl;
    
    private byte[] latencyData = "measure latency for this message".getBytes();
    byte[] readData = new byte[latencyData.length];
    private BluetoothSocket socket;
    BTLink btLink;

	private ArrayList<BluetoothDevice> btDevices;
	BluetoothDevice btDevice;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
        Config.getInstance().init(this);
        
		btLink = Config.getInstance().btLink;
		btLink.setActivity(this);
		btLink.init();
    }
    
	@Override
	public void onStart(){
	    super.onStart();
	    init();

	}
	
	private void init(){
		config = Config.getInstance();
		config.startup(this);
		textViewStatus = (TextView) findViewById(R.id.statusView);
		textViewDisplay = (TextView) findViewById(R.id.outputView);
		textViewDisplay.setMovementMethod(new ScrollingMovementMethod());
		editTextCommand = (EditText)findViewById(R.id.edit_text_cmd);
		
		
		buttonSetState = (Button)findViewById(R.id.btn_setState);
		buttonReSyncServer = (Button)findViewById(R.id.btn_reSynchServer);
		
		buttonExec = (Button)findViewById(R.id.btn_exec);
		
		
		serverUrl = config.getParam(Config.TEST_SERVER, Config.NONE);
		buttonReSyncServer.setEnabled(!(Config.NONE == serverUrl));

		selectedScript = (TextView)findViewById(R.id.txt_selectedScript);
		
		buttonRunScript = (Button)findViewById(R.id.btn_run_script);
		btDevice = btLink.previousBtDevice(this);
		
		scriptUrl = config.getParam(Config.CURRENT_SCRIPT_URL, Config.NONE);		
		selectedScript.setText(scriptUrl);
		
		buttonRunScript.setEnabled((btDevice != null) && (scriptUrl != Config.NONE));
		btLink.setToConnect(btDevice);
				
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
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
    	Context ac = getApplicationContext();
//    	View view = (View)ac;
        switch(item.getItemId()){
        case R.id.action_status: 
//        	Toast.makeText(getApplicationContext(), "onStatusClick()", Toast.LENGTH_SHORT).show();
        	textViewStatus.setText("Status Display"); 
        	break;
        case R.id.action_pair: 
        	btLink.pair(this);
//            final ArrayList<BluetoothDevice> deviceList = getBluetoothDevices();
//            if (deviceList == null) {
//            	Toast.makeText(getApplicationContext(), "No device list", Toast.LENGTH_LONG).show();
//            } else {
//            	Config.getInstance().btLink.showChooserAndConnect(this, deviceList);	
//            }
            
        	break;
        case R.id.action_link: 
    		btLink.activate(this); 
        	break;
        case R.id.action_clear: 
    		textViewDisplay.setText(""); 
        	break;
        case R.id.action_config:
        	Config.instance.setMode("Config");
        	Toast.makeText(getApplicationContext(), "Config mode", Toast.LENGTH_SHORT).show();
        	Intent configIntent = new Intent(ExecActivity.this,ConfigActivity.class);
        	startActivity(configIntent);
        	break;
        case R.id.action_c2:
        	Config.instance.setMode("C2");
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
		btLink.postMessageout("1");
    }
    
    public void onSend2Click(View v) {
//		Toast.makeText(getApplicationContext(), "onSend2Click", Toast.LENGTH_LONG).show();
		btLink.postMessageout("2");
    }
	
    public void onSend3Click(View v) {
//		Toast.makeText(getApplicationContext(), "onSend3Click", Toast.LENGTH_LONG).show();
		btLink.postMessageout("3");
    }
    
    public void onClickReconnect(View v) {
    	buttonSetState.setEnabled(false);
    	String stat = "Reconnected";
		Toast.makeText(getApplicationContext(), stat, Toast.LENGTH_SHORT).show();
    }
    
    public void onClickReSynchServer(View v) {
    	buttonSetState.setText("Boom");
    	String stat = "Resynched";
		Toast.makeText(getApplicationContext(), stat, Toast.LENGTH_SHORT).show();
    }
    
    public void onClickExec(View v) {
    	String cmd = editTextCommand.getText().toString();
        if (!btLink.isActive()){
      	  btLink.activate(this);
        }
    	btLink.postMessageout(cmd);
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
    
    public void onClickRunScript(View v) {
      String scriptText = Config.getInstance().getScriptText();
      if (!btLink.isActive()){
    	  btLink.activate(this);
      }
      btLink.postMessageout(scriptText);
  	  String stat = "Run Script";
  	  textViewStatus.setText(stat);
	  Toast.makeText(getApplicationContext(), scriptText, Toast.LENGTH_LONG).show();
    }
		
	
//    public void onPairClick(View v) {
//        final ArrayList<BluetoothDevice> deviceList = getBluetoothDevices();
//        if (deviceList == null) {
//        	Toast.makeText(getApplicationContext(), "No device list", Toast.LENGTH_LONG).show();
//            return;
//        }
//        showChooserAndConnect(deviceList);
//    }
    


//    private void showChooserAndConnect(final ArrayList<BluetoothDevice> deviceList) {
//        ArrayList<CharSequence> itemList = new ArrayList<CharSequence>();
//        for (BluetoothDevice device : deviceList) {
//            itemList.add(device.getName() + " [" + device.getAddress() + "]");
//        }
//        CharSequence[] items = itemList.toArray(new CharSequence[itemList.size()]);
//
//        btLink.setToConnect(null);
//
//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setItems(items, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        btLink.setToConnect(deviceList.get(which));  
//                    }
//                })
//                .create();
//        dialog.show();
//        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                if (btLink.getToConnect() != null) {
//                	Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
//                    //connect();
//                }
//            }
//        });
//    }

//    private ArrayList<BluetoothDevice> getBluetoothDevices() {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter == null) {
//            alarm("No BT on device");
//            return null;
//        }
//
//        if (!bluetoothAdapter.isEnabled()) {
//            alarm("BT on device  disabled");
//            return null;
//        }
//        
//        
//
//        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
//        if (deviceSet.isEmpty()) {
//            alarm("Pair first");
//            return null;
//        }
//
//        if (deviceSet == null) {
//            return null;
//        }
//        final ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>(deviceSet);
//        return deviceList;
//    }
    
    public void display(final String msg){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewDisplay.append(msg + "\n");
                                
//                int scroll_amount = textViewDisplay.getBottom();
//
//                if(textViewDisplay.getLineCount() > 20){
//                    scroll_amount = scroll_amount + textViewDisplay.getLineHeight();
//                    textViewDisplay.scrollTo(0, scroll_amount - 20);
//                }
             
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
    
    
	class ScriptLoader implements Callback{
		
		final HttpAgent httpAgent;	
		String scriptName;
		public String getScriptName() { return scriptName; }
		public void setScriptName(String scriptName) { this.scriptName = scriptName; }
		
		ScriptLoader(Context context){
			httpAgent = new HttpAgent(context);			
		}
		
		void fetch(String url){
			String[] parts = url.split("/");
			scriptName = parts[parts.length - 1];
			httpAgent.fetch(url,this,"loading script " + url);
			
			
			String scriptUrl = config.getParam(Config.CURRENT_SCRIPT_URL, Config.NONE);
			buttonRunScript.setEnabled(!(Config.NONE == scriptUrl));
			
			selectedScript.setText(scriptUrl);
		}

		@Override
		public void ok(String text) {
			
			Util.getInstance().guaranteeTextFile(scriptName, Config.SCRIPT_DIRPATH, text);
		    String data = Util.getInstance().getTextFile(scriptName, Config.SCRIPT_DIRPATH);
		    // TODO: remove self test when stable
		    assert(text.trim().equals(data.trim()));		    
		    config.setScriptText(text);
		}

		@Override
		public void fail(String s) {
			throw new RuntimeException("fail called with " +s);			
		}	
	}

}
