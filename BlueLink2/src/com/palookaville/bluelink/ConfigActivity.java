package com.palookaville.bluelink;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ConfigActivity extends Activity {	
	
	public static String displayBuffer = "Move along.  Nothing to see here.";
	EditText debugText;
	Config config;
	
	HttpAgent billboardHttpAgent;
	HttpAgent scriptHttpAgent;
	String scriptsUrl = "";
	
	ScriptLoader scriptLoader;
	
    EditText textEditServerAddress;
    String serverAddress;

    BillboardUpdater billboardUpdater;
	
	Spinner scriptListSpinner ;  
	ArrayAdapter<String> scriptListAdapter ;
	List<String>scriptList = new ArrayList<String>();
	ScriptUpdater scriptUpdater;
	Context context;
	
	
	public String SCRIPT_URL = "http://192.168.1.66.com:8000/scripts";
	public String ELECTION_URL = "http://votecastomatic.com/elections";
	public String VOTER_URL = "http://votecastomatic.com/voters";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);

		initialize();
	}


	void initialize(){

		//debugText = (EditText)findViewById(R.id.debugDisplay);
		context = this;
		config = Config.getInstance();
		
		billboardUpdater = new BillboardUpdater();
		billboardHttpAgent = new HttpAgent(context);
		
		scriptLoader = new ScriptLoader();
		
		scriptUpdater = new ScriptUpdater();
		scriptListSpinner = (Spinner) findViewById( R.id.ScriptSpinner );  	    
		scriptListAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, scriptList);  
		scriptListSpinner.setAdapter( scriptListAdapter );   
		scriptHttpAgent = new HttpAgent(context);
		

		textEditServerAddress = (EditText) this.findViewById(R.id.textEditServerAddress);
		serverAddress = config.getParam(Config.TEST_SERVER);
		textEditServerAddress.setText(serverAddress);		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
	

	public void resumeExec(View view){
    	Toast.makeText(getApplicationContext(), "resumeExec()", Toast.LENGTH_SHORT).show();

		System.out.println("Pressed Resume");
		String result = "Exec resumed";
    	Intent configIntent = new Intent(ConfigActivity.this,ExecActivity.class);
    	startActivity(configIntent);
    	finish();
//		debugText.setText(result);
	}
	
	public void btLinkStatePressed(View view){
		String linkState = Config.getInstance().btLink.getLinkState();
		System.out.println("Pressed DBG2: " + linkState);
		Toast.makeText(getApplicationContext(), linkState, Toast.LENGTH_LONG).show();
	}
	
	public void configExecPressed(View view){
		String result = "Config Exec pressed";
		setServer();
		System.out.println(result);
		//
		// Get script list here
		//
		updateBillboard();
		Toast.makeText(getApplicationContext(), "configExecPressed " , Toast.LENGTH_LONG).show();
	}
	
	void updateBillboard(){

		billboardHttpAgent.fetch(serverAddress,billboardUpdater,"loading billboard");
		Toast.makeText(getApplicationContext(), "update Billboard", Toast.LENGTH_LONG).show();
	}
	
	void updateScriptThingie(){

		scriptHttpAgent.fetch(scriptsUrl,scriptUpdater,"loading scripts");
		Toast.makeText(getApplicationContext(), "load scripts", Toast.LENGTH_LONG).show();
	}
	
	
	private void setServer(){
		String serverUrl = textEditServerAddress.getText().toString();
		if (!serverUrl.startsWith("")){
			serverUrl = "http://" + serverUrl;	
		}
		config.setParam(Config.TEST_SERVER, serverUrl);	
	}

	public void dbg3Pressed(View view){
		String result = "DBG3 pressed: init BT";
		System.out.println(result);
		
		Toast.makeText(getApplicationContext(), "dbg3 ", Toast.LENGTH_LONG).show();
		//Config.getInstance().btLink.init();
		//Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
		//debugText.setText(result);
	}
	
	public void loadScriptPressed(View view){
		
		String scriptUrl = (String)scriptListSpinner.getSelectedItem();
		config.setParam(Config.CURRENT_SCRIPT_URL, scriptUrl);
		scriptHttpAgent.fetch(scriptUrl,scriptLoader,"loading script " + scriptUrl);

		String result = "Load Script Pressed " + scriptUrl;
		System.out.println(result);
		
		Toast.makeText(getApplicationContext(), "Load Script " + scriptUrl, Toast.LENGTH_LONG).show();
		//Config.getInstance().btLink.init();
		//Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
		//debugText.setText(result);
	}
	
	public void display(String msg){
		System.out.println("DBG display");
		System.out.println(msg);
		debugText.setText(msg);
		System.out.println("DBG display over");
		
	}
	
	class ScriptUpdater implements Callback{

		@Override
		public void ok(String jsonData) {
			scriptList = new ArrayList<String>();
			try {
				JSONArray scriptItems = new JSONArray(jsonData);
				for(int i =0; i<scriptItems.length(); i++){
					String scriptUrl = scriptItems.getString(i);
					scriptList.add(scriptUrl);
				}
				scriptListAdapter.clear();
				scriptListAdapter.addAll(scriptList);
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
	
	class BillboardUpdater implements Callback{

		@Override
		public void ok(String jsonData) {
			//scriptList = new ArrayList<String>();
			try {
				JSONObject billboardItems = new JSONObject(jsonData);
				scriptsUrl = billboardItems.getString("scripts");
				updateScriptThingie();
			
			} catch (Exception e){
				throw new RuntimeException(e);
			}		
		}

		@Override
		public void fail(String s) {
			throw new RuntimeException("fail called with " +s);			
		}	
	}
	
	class ScriptLoader implements Callback{

		@Override
		public void ok(String text) {
			config.setScriptText(text);	
		}

		@Override
		public void fail(String s) {
			throw new RuntimeException("fail called with " +s);			
		}	
	}
}