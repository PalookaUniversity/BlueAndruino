/**
 * 
 */
package com.palookaville.bluelink;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author gvamos
 *
 */
class AppManager {
	
	String mode = "Blink";
	
    public String getMode() { return mode; }

	public void setMode(String mode) { this.mode = mode; }

	Link link = new Link();
	
	public static AppManager instance = new AppManager();	

}