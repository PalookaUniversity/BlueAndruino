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
    //String serverAddress;

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
		
		scriptUpdater = new ScriptUpdater();
		scriptListSpinner = (Spinner) findViewById( R.id.ScriptSpinner );  	    
		scriptListAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, scriptList);  
		scriptListSpinner.setAdapter( scriptListAdapter );   
//		scriptHttpAgent = new HttpAgent(context);
		
		textEditServerAddress = (EditText) this.findViewById(R.id.textEditServerAddress);
		textEditServerAddress.setText(Config.getInstance().serverUrl);		
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
	
	public void fetchBillboard(View view){
		

		String serverUrl = textEditServerAddress.getText().toString();
		if (!serverUrl.startsWith("http://")){
			serverUrl = "http://" + serverUrl;	
		}
//		serverUrl = "http://192.168.1.666:8000";
		String result = "Fetch Billboard pressed<"+serverUrl+">";
		Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
		config.setParam(Config.TEST_SERVER, serverUrl);	

		System.out.println(result);
		//
		// Get script list here
		//
		//billboardHttpAgent.fetch(serverAddress,billboardUpdater,"loading billboard");
		
		billboardHttpAgent.fetch(serverUrl,billboardUpdater,"loading billboard");
		Toast.makeText(getApplicationContext(), "fetchBillboard complete " , Toast.LENGTH_LONG).show();
	}
	
	void updateScriptThingie(){

		scriptHttpAgent.fetch(scriptsUrl,scriptUpdater,"loading scripts");
		Toast.makeText(getApplicationContext(), "load scripts", Toast.LENGTH_LONG).show();
	}
	

	public void dbg3Pressed(View view){
		String result = "DBG3 pressed: init BT";
		System.out.println(result);
		//Util.getInstance().setContext(context).driveTest("dbg3");
		selectScriptPressed(view);
		Toast.makeText(getApplicationContext(), "dbg3 ", Toast.LENGTH_LONG).show();
		//Config.getInstance().btLink.init();
		//Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
		//debugText.setText(result);
	}
	
	public void selectScriptPressed(View view){
		String scriptUrl = (String)scriptListSpinner.getSelectedItem();	
		config.setParam(Config.CURRENT_SCRIPT_URL, scriptUrl);
		String fileName = scriptHttpAgent.name(scriptUrl);
		String text = Util.getInstance().getTextFile(fileName, Config.SCRIPT_DIRPATH);
		config.setScriptText(text);
	}
	
	public void loadScriptPressed(View view){
		
		String scriptUrl = (String)scriptListSpinner.getSelectedItem();
		config.setParam(Config.CURRENT_SCRIPT_URL, scriptUrl);

		String result = "Load Script Pressed " + scriptUrl;
		scriptLoader = new ScriptLoader(this);
		scriptLoader.fetch(scriptUrl);
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