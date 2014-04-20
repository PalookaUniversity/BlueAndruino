/**
 * 
 */
package edu.palookaville.androclient;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.palookaville.androclient.R;

/**
 * @author gvamos
 *
 */
public class Scanner extends Activity {
	
	  private ListView electionListView ;  
	  private ArrayAdapter<String> listAdapter ; 	  
	  private Config.Elections elections;
	
	  @Override  
	  public void onCreate(Bundle savedInstanceState) {  
	    super.onCreate(savedInstanceState); 
	    Log.i(this.getClass().getName(),":onCreate");
	    setContentView(R.layout.activity_scanner); 
	    	      
	    elections = Config.getInstance().getElections();
	    elections.updateElections();  
  	      
	    // Shave the yak.  
	    electionListView = (ListView) findViewById( R.id.electionsListView );  	    
	    listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, (elections.getElectionsList()));  
	    electionListView.setAdapter( listAdapter );        
	  }  		
}
