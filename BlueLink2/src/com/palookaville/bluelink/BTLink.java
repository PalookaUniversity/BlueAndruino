package com.palookaville.bluelink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BTLink  {

	private static final UUID SECURE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private byte[] latencyData = "measure latency for this message".getBytes();


	BluetoothSocket socket;
	private ExecActivity activity;

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(ExecActivity activity) { this.activity = activity; }

	public BluetoothDevice getToConnect() { return toConnect; }

	public void setToConnect(BluetoothDevice toConnect) { this.toConnect = toConnect; }

	public BluetoothAdapter getBluetoothAdapter() { return bluetoothAdapter; }

	public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) { this.bluetoothAdapter = bluetoothAdapter; }

	public BluetoothSocket  getBluetoothSocket(){
		try {
			socket = toConnect.createRfcommSocketToServiceRecord(SECURE_UUID);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot generatge socket",e);
		}
		return socket;
	}



	private BluetoothDevice toConnect;
	private BluetoothAdapter bluetoothAdapter;

	BlockingQueue<String> inQueue = new ArrayBlockingQueue<String>(10);
	BlockingQueue<String> outQueue = new ArrayBlockingQueue<String>(10);

	public BTLink(){
		BlockingQueue<String> inQueue = new ArrayBlockingQueue<String>(10);
		BlockingQueue<String> outQueue = new ArrayBlockingQueue<String>(10);   	
	}
	
	public void activate(ExecActivity activity){
		this.activity = activity;
		try {
			if (socket != null) {
				socket.close();
			}
			socket = toConnect.createRfcommSocketToServiceRecord(SECURE_UUID);
			socket.connect();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot generate socket",e);
		}
		writer.start();
		reader.start();
		System.out.println("Reader and writer started.");
		
		final ExecActivity callingActivity = activity;
		
		//activity.runOnUiThread(new Runnable() {
		//@Override
		//public void run() {

		//	callingActivity.mTextViewStrength.append(" " + callingActivity.getString(R.string.disconnected));
		//};
		//});
		
		
	}

	public void postMessageout(String msg){
		System.out.println("Attempting to send: "+msg);
		try {
			outQueue.put(msg);
			System.out.println("Enqueued: "+msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}

	public void writeImmediate(String msg) {
		System.out.println("About to send: "+msg);
		try {
			socket.getOutputStream().write(msg.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finished writing");
	}
	
	public Thread writer = new Thread(new Runnable(){
		@Override
		public void run() {
			//bluetoothAdapter.cancelDiscovery();
			try {
//				socket = getBluetoothSocket();
//				socket.connect();
				while (socket.isConnected()) {
					String msgOut = outQueue.take();
					socket.getOutputStream().write(msgOut.getBytes());
				}

			} catch (IOException e){

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();			}			
		}		
	});	


	public Thread reader = new Thread(new Runnable(){
		@Override
		public void run() {
			//bluetoothAdapter.cancelDiscovery();
			Integer ct = 0;
			try {
//				socket = getBluetoothSocket();
//				socket.connect();
				BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				while (socket.isConnected()) {
					String msg = r.readLine();
					//inQueue.add(msg);
					activity.display(msg);
					System.out.println(ct.toString() + " Message in:"+msg);
				}			
			} catch (IOException e){
				e.printStackTrace();
			} 			
		}		
	});	
}


