/**
 * 
 */
package com.palookaville.bluelink;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;


class Config {
	
	Map<String,String>params = new HashMap<String,String>();	
	SharedPreferences sharedPreferences;
	String mode = "Blink";
	BTLink btLink;
	
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
	

}