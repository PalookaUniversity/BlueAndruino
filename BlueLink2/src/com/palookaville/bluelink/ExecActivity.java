package com.palookaville.bluelink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**********************************************************************
 * 
 * @author gvamos
 *
 */

public class ExecActivity extends Activity {
	
    private static final UUID SECURE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
      
    TextView textViewStatus;
    TextView textOutputView;
    EditText editTextCommand;
    ActionBar actionBar;
    
	String serverUrl = "";
		
	HttpAgent billboardHttpAgent;
	HttpAgent scriptHttpAgent;
	String scriptsUrl = "";
	
	ScriptLoader scriptLoader;
	
    EditText textEditServerAddress;
    //String serverAddress;

    BillboardUpdater billboardUpdater;
    
    Button buttonReSyncServer;
	
	Spinner scriptListSpinner ;  
	ArrayAdapter<String> scriptListAdapter ;
	ScriptUpdater scriptUpdater;
	Context context;
      
    Button buttonExec;
    Button buttonAdd;
    Button buttonRunScript;
    Button buttonCheck;
        
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
		textViewStatus.setText(" init");
		
		textOutputView = (TextView) findViewById(R.id.outputView);
		textOutputView.setMovementMethod(new ScrollingMovementMethod());
		
		String t1 = "Field outputView";
		textOutputView.setText(t1);
		String t2 = textOutputView.getText().toString();
		Boolean matched = t1.equals(t2);
		System.out.println(matched);
		
		
		editTextCommand = (EditText)findViewById(R.id.edit_text_cmd);		
		buttonExec = (Button)findViewById(R.id.btn_exec);	
		serverUrl = config.getParam(Config.TEST_SERVER, Config.NONE);
		
		buttonRunScript = (Button)findViewById(R.id.btn_run_script);
		btDevice = btLink.previousBtDevice(this);
		
		scriptUrl = config.getParam(Config.CURRENT_SCRIPT_URL, Config.NONE);		
		scriptListAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, config.scriptList);  
		
		scriptListSpinner = (Spinner) findViewById( R.id.ScriptListSpinner );  	    
		scriptListSpinner.setAdapter( scriptListAdapter );   
		
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
    		textOutputView.setText(""); 
        	break;
        	
        case R.id.action_server_sync: 
        	
    		String serverUrl = config.getParam(Config.TEST_SERVER, "");;
    		if (!"".equals(serverUrl)){
        		billboardUpdater = new BillboardUpdater(this);
        		billboardUpdater.fetch(serverUrl);
        		String result = "Fetch Billboard pressed<"+serverUrl+">";
        		Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
    		} else {
    			Toast.makeText(getApplicationContext(), "No server URL", Toast.LENGTH_LONG).show();
    		}
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
		textOutputView.append("\nCheck:"+diagnostic);
		//textOutputView.setText("Check:"+diagnostic);
    }
    
    public void onClickLoadScript(View v) {
    	
      scriptUrl = (String) scriptListSpinner.getSelectedItem();
      
      if (scriptUrl == null || scriptUrl.equals("")){
    	  Toast.makeText(getApplicationContext(), "Error: No script selected", Toast.LENGTH_LONG).show();
    	  return;
      }
      ScriptLoader scriptLoader = new ScriptLoader(this);
      scriptLoader.fetch(scriptUrl);
      }
    
    public void onClickEditScript(View v) {
    	
        scriptUrl = (String) scriptListSpinner.getSelectedItem();
        
        if (scriptUrl == null || scriptUrl.equals("")){
      	  Toast.makeText(getApplicationContext(), "Error: No script selected", Toast.LENGTH_LONG).show();
      	  return;
        }		
    	Intent configIntent = new Intent(ExecActivity.this,ScriptViewActivity.class);
    	configIntent.putExtra(ScriptViewActivity.SCRIPT_URL, scriptUrl);
    	startActivity(configIntent);

    }
    
    public void onClickRunScript(View v) {
      String scriptText = Config.getInstance().getScriptText();
      if (scriptText.equals("")){
    	  textOutputView.append("\nScript text empty");
    	  return;
      }
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
                textOutputView.append(msg + "\n");
                                
//                int scroll_amount = textViewDisplay.getBottom();
//
//                if(textViewDisplay.getLineCount() > 20){
//                    scroll_amount = scroll_amount + textViewDisplay.getLineHeight();
//                    textViewDisplay.scrollTo(0, scroll_amount - 20);
//                }
             
            }
        });
    }
    
    
	class BillboardUpdater implements Callback{
		
		HttpAgent billboardHttpAgent;
		
		BillboardUpdater(Context context){
			billboardHttpAgent = new HttpAgent(context);
		}
		void fetch(String url){
			billboardHttpAgent.fetch(url,this,"loading billboard");			
		}

		@Override
		public void ok(String jsonData) {
			//scriptList = new ArrayList<String>();
			try {
				JSONObject billboardItems = new JSONObject(jsonData);
				scriptsUrl = billboardItems.getString("scripts");
				updateScriptList();
			
			} catch (Exception e){
				String msg = e.getMessage();
				System.out.println(msg);
				throw new RuntimeException(e);
			}		
		}

		@Override
		public void fail(String s) {
			throw new RuntimeException("fail called with " +s);			
		}	
	}
	void updateScriptList(){

		scriptUpdater = new ScriptUpdater(this);
		scriptUpdater.fetch(scriptsUrl);
		Toast.makeText(getApplicationContext(), "load scripts", Toast.LENGTH_LONG).show();
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
		String url = "Uninitialized";
		String scriptName;
		public String getScriptName() { return scriptName; }
		public void setScriptName(String scriptName) { this.scriptName = scriptName; }
		
		ScriptLoader(Context context){
			httpAgent = new HttpAgent(context);	
			
		}
		
		void fetch(String url){
			this.url = url;			
			scriptName = Config.getInstance().scriptName(url);
			httpAgent.fetch(url,this,"loading script " + url);						
		}

		@Override
		public void ok(String text) {
			
			Util.getInstance().guaranteeTextFile(scriptName, Config.SCRIPT_DIRPATH, text);
		    String data = Util.getInstance().getTextFile(scriptName, Config.SCRIPT_DIRPATH);
		    // TODO: remove self test when stable
		    Toast.makeText(getApplicationContext(), "Load Script:"+url, Toast.LENGTH_LONG).show();
			String scriptUrl = config.getParam(Config.CURRENT_SCRIPT_URL, Config.NONE);
			Boolean gotIt = (Config.NONE != scriptUrl);
			buttonRunScript.setEnabled(gotIt);
			buttonRunScript.setEnabled(true);

		    
		    assert(text.trim().equals(data.trim()));		    
		    config.setScriptText(text);
		}

		@Override
		public void fail(String s) {
			Toast.makeText(getApplicationContext(), "Failed to load:"+url, Toast.LENGTH_LONG).show();
			throw new RuntimeException("fail called with " +s);			
		}	
	}
	
	class ScriptUpdater implements Callback{
		
		HttpAgent scriptdHttpAgent;
		
		ScriptUpdater(Context context){
			scriptdHttpAgent = new HttpAgent(context);
		}
		void fetch(String url){
			scriptdHttpAgent.fetch(url,this,"loading script list");			
		}

		@Override
		public void ok(String jsonData) {
			config.scriptList = new ArrayList<String>();
			try {
				JSONArray scriptItems = new JSONArray(jsonData);
				for(int i =0; i<scriptItems.length(); i++){
					String scriptUrl = scriptItems.getString(i);
					config.scriptList.add(scriptUrl);
					
				}
				scriptListAdapter.clear();
				scriptListAdapter.addAll(config.scriptList);
				scriptListAdapter.notifyDataSetChanged();				
			} catch (Exception e){
				throw new RuntimeException(e);
			}		
		}

		@Override
		public void fail(String s) {
			throw new RuntimeException("fail called with " +s);			
		}	
	}

}
