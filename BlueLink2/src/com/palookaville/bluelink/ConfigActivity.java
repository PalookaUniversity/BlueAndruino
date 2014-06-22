package com.palookaville.bluelink;

import com.palookaville.bluelink.ExecActivity.PlaceholderFragment;

import android.app.Activity;
import android.os.Bundle;


public class ConfigActivity extends Activity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
    }
    
	@Override
	public void onStart(){
	    super.onStart();
	}

}
