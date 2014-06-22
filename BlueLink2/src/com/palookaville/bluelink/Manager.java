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
class Manager {
	
	String mode = "Blink";
	
    public String getMode() { return mode; }

	public void setMode(String mode) { this.mode = mode; }

	BTLink link = new BTLink();
	
	public static Manager instance = new Manager();
	
	public static Manager getInstance(){ return instance ;};

}