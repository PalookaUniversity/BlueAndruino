package com.palookaville.bluelink;

import com.palookaville.bluelink.ExecActivity.PlaceholderFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class ConfigActivity extends Activity {
	
	
	public static String displayBuffer = "Move along.  Nothing to see here.";
	EditText debugText;
	Config config;

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
//		debugText.setText(result);
	}
	
	public void dbg2Pressed(View view){
		System.out.println("Pressed DBG2");
		String result = "Exec resumed";

	}

	public void dbg3Pressed(View view){
		System.out.println("Pressed DBG3");
		String result = "DBG3 pressed";
		debugText.setText(result);
	}
	
	public void display(String msg){
		System.out.println("DBG display");
		System.out.println(msg);
		debugText.setText(msg);
		System.out.println("DBG display over");
		
	}

}
