/**
 * 
 */
package com.palookaville.bluelink;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;


class Config {
	
	public static final String NONE = "None";
	public static final String TEST_SERVER = "TestServer";
	public static final String SCRIPT_PATH = "ScriptPath";
	public static final String SCRIPT_DIRPATH = "scripts";
	public static final String CURRENT_SCRIPT_URL = "CurrentScriptUrl";
	
	public static final String SCRIPTS_DIR = "scripts";
	
	public static final String STATE_INIT = "INIT";
	public static final String STATE_NEW = "NEW";
	public static final String STATE_CONFIGURED = "CONFIGURED";
	public static final String STATE_PAIRED = "PAIRED";
	public static final String STATE_LINKED = "LINKED";
	
	String state = STATE_INIT;	
	public String getState() { return state; }
	public void setState(String state) { this.state = state; }

	Map<String,String>params = new HashMap<String,String>();	
	SharedPreferences sharedPreferences;
	Context context;
	String mode = "Blink";
	BTLink btLink;
	String testServerAddress;
	String btAddress;
	String serverUrl = "";
	String scriptPath = "";	
	String scriptText = "";	
	List<String>scriptList = new ArrayList<String>();
	public String getScriptText() { return scriptText; }
	void setScriptText(String s){ scriptText = s; }
	
	
	private Config(){
		btLink = new BTLink();
	};
	
	public void init(Context context){
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
    public String getMode() { return mode; }

	public void setMode(String mode) { this.mode = mode; }


	
	public static Config instance = new Config();
	
	public static Config getInstance(){ return instance ;};
	

	public void setParam(String key, String value) {
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public String getParam(String key){
		return sharedPreferences.getString(key, null);
	}
	
	public String getParam(String key, String dafaultValue){
		return sharedPreferences.getString(key, dafaultValue);
	}
	
	public void startup(Context context){
        this.context = context;
		if (getParam("INITIALIZED") == null) {
			Util.getInstance().setContext(context).guaranteeEmptyDirectory(SCRIPTS_DIR);
			return;
		}
		setParam("INITIALIZED",new Date().toString());
		serverUrl = getParam(Config.TEST_SERVER, "");
		scriptPath = getParam(Config.SCRIPT_PATH, "");
		state = STATE_CONFIGURED;
	}
	
	class ScriptCapture implements Callback{
		
		final String name;
		
		ScriptCapture(String name){
			this.name = name;
		}

		@Override
		public void ok(String text) {
			setScriptText(text);	
		}

		@Override
		public void fail(String s) {
			throw new RuntimeException("fail called with " +s);			
		}	
	}
	String scriptName(String scriptUrl){
		String[] parts = scriptUrl.split("/");
		return parts[parts.length - 1];
		
	}
}