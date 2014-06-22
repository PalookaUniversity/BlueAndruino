package com.palookaville.bluelink;

public interface AsynchWebAction {
	/**
	 * @author gvamos
	 *
	 * Interface for asynch web requests
	 */
	public interface AsyncWebAction {
		
		public void exec();
		public void followUp();
		public String getStatus();
		public String getResult();
	}
}
