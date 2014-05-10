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

public class Link  {

	private static final UUID SECURE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private byte[] latencyData = "measure latency for this message".getBytes();


	BluetoothSocket socket;
	private MainActivity activity;

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(MainActivity activity) { this.activity = activity; }

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

	BlockingQueue<String> inQueue = new ArrayBlockingQueue<>(10);
	BlockingQueue<String> outQueue = new ArrayBlockingQueue<>(10);

	public Link(){
		BlockingQueue<String> inQueue = new ArrayBlockingQueue<>(10);
		BlockingQueue<String> outQueue = new ArrayBlockingQueue<>(10);   	
	}
	
	public void activate(MainActivity activity){
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
		
		final MainActivity callingActivity = activity;
		
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



//	@Override
//	public void run(){
//		bluetoothAdapter.cancelDiscovery();
//		try {
//			socket = getBluetoothSocket();
//			socket.connect();
//			while (socket.isConnected()) {
//				socket.getInputStream();
//				String msgOut = outQueue.take();
//				socket.getOutputStream().write(msgOut.getBytes());
//
//				long nanoStart = System.nanoTime();
//				byte[] dataToSend = latencyData;
//				socket.getOutputStream().write(dataToSend);
//
//				int totalReceivedBytes = 0;
//				while(totalReceivedBytes < dataToSend.length) {
//					int numRead = socket.getInputStream().read(activity.readData);
//					activity.debug("read " + numRead + " bytes");
//					totalReceivedBytes += numRead;
//				}
//
//				activity.report(System.nanoTime() - nanoStart);
//			}
//			
//		} catch (IOException e){
//			
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//}




//	@Override
//	public void run() {
//		bluetoothAdapter.cancelDiscovery();
//		try {
//			socket = toConnect.createRfcommSocketToServiceRecord(SECURE_UUID);
//			socket.connect();
//			while (socket.isConnected()) {
//
//				long nanoStart = System.nanoTime();
//				byte[] dataToSend = latencyData;
//				socket.getOutputStream().write(dataToSend);
//
//				int totalReceivedBytes = 0;
//				while(totalReceivedBytes < dataToSend.length) {
//					int numRead = socket.getInputStream().read(activity.readData);
//					activity.debug("read " + numRead + " bytes");
//					totalReceivedBytes += numRead;
//				}
//
//				activity.report(System.nanoTime() - nanoStart);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (socket != null) {
//				try {
//					socket.getInputStream().close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				try {
//					socket.getOutputStream().close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				try {
//					socket.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		activity.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				activity.mTextViewStrength.append(" " + activity.getString(R.string.disconnected));
//			}
//		}
//				);


//	}).start();

//}
//
//}
