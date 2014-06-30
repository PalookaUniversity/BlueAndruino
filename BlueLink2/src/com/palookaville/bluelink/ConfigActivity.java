package com.palookaville.bluelink;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cj.votron.Callback;
import com.cj.votron.HttpAgent;
import com.cj.votron.ElectionsActivity.ElectionUpdater;
import com.cj.votron.ElectionsActivity.VoterUpdater;
import com.palookaville.bluelink.ExecActivity.PlaceholderFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class ConfigActivity extends Activity {
	
	
	public static String displayBuffer = "Move along.  Nothing to see here.";
	EditText debugText;
	Config config;
	
	HttpAgent voterHttpAgent;
	


	private Spinner scriptListSpinner ;  
	private ArrayAdapter<String> scriptListAdapter ;
	private List<String>scriptList = new ArrayList<String>();
	private VoterUpdater scriptUpdater;
	private Context context;
	
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
		config = Config.getInstance();
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

	public void dbg3Pressed(View view){
		String result = "DBG3 pressed: init BT";
		System.out.println(result);
		Toast.makeText(getApplicationContext(), "dbg3", Toast.LENGTH_LONG).show();
		Config.getInstance().btLink.init();
		Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
		//debugText.setText(result);
	}
	
	public void display(String msg){
		System.out.println("DBG display");
		System.out.println(msg);
		debugText.setText(msg);
		System.out.println("DBG display over");
		
	}
	
	class VoterUpdater implements Callback{

		@Override
		public void ok(String jsonData) {
			scriptList = new ArrayList<String>();
			try {
				JSONArray scriptItems = new JSONArray(jsonData);
				for(int i =0; i<scriptItems.length(); i++){
					JSONObject electionObject = scriptItems.getJSONObject(i);//FIX THIS
					scriptList.add(electionObject.getString("name"));
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

}