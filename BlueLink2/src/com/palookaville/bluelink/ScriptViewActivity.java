package com.palookaville.bluelink;

import com.palookaville.bluelink.ExecActivity.ScriptLoader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ScriptViewActivity extends Activity {

	public static final String SCRIPT_URL = "SCRIPT_URL";
	String scriptUrl = "Uninitialized";
	String scriptName = "Uninitialized";
	String scriptText = "Uninitialized";
	
	EditText scriptView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_script_view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.script_view, menu);
		return true;
	}
	
	Intent configIntent = null;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_edit_save) {
			scriptText = scriptView.getText().toString();
			Util.getInstance().saveTextFile(scriptName, Config.getInstance().getExternalScriptPath(),scriptText);
			Toast.makeText(getApplicationContext(), "Save Edits", Toast.LENGTH_LONG).show();			
			return true;
		}
		if (id == R.id.action_edit_exit) {
			scriptText = scriptView.getText().toString();
			Util.getInstance().saveTextFile(scriptName, Config.getInstance().getExternalScriptPath(),scriptText);
			Toast.makeText(getApplicationContext(), "Exit", Toast.LENGTH_LONG).show();
			if (configIntent == null){
				configIntent = new Intent(ScriptViewActivity.this,ExecActivity.class);
			}
	    	startActivity(configIntent);
			return true;
		}
		if (id == R.id.action_edit_abort) {
			Toast.makeText(getApplicationContext(), "Abort", Toast.LENGTH_LONG).show();
			if (configIntent == null){
				configIntent = new Intent(ScriptViewActivity.this,ExecActivity.class);
			}
	    	startActivity(configIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onStart(){
	    super.onStart();
	    init();

	}
	
	private void init(){
		Bundle extras = getIntent().getExtras();
		scriptName = extras.getString(SCRIPT_URL);
		scriptText = Util.getInstance().getTextFile(scriptName, Config.getInstance().getExternalScriptPath());
		scriptView = (EditText)findViewById(R.id.textview_scriptview);
		scriptView.setText(scriptText);				
	}
	
    public void onClickScriptViewActivityReturn(View v) {
        Intent configIntent = new Intent(ScriptViewActivity.this,ExecActivity.class);
    	startActivity(configIntent);
    }
}
