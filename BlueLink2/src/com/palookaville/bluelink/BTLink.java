package com.palookaville.bluelink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

public class BTLink  {

	private static final UUID SECURE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private byte[] latencyData = "measure latency for this message".getBytes();
	
	public static String BT_ADDRESS = "BT_ADDRESS";
	public static String NON_SELECTED = "none selected";


	BluetoothSocket socket;
	ExecActivity activity;
	BluetoothDevice device;
    String btAddress = NON_SELECTED;
    

	private BluetoothDevice toConnect;
	private BluetoothAdapter bluetoothAdapter;

	BlockingQueue<String> inQueue = new ArrayBlockingQueue<String>(10);
	BlockingQueue<String> outQueue = new ArrayBlockingQueue<String>(10);
	
	public BTLink(){
		BlockingQueue<String> inQueue = new ArrayBlockingQueue<String>(10);
		BlockingQueue<String> outQueue = new ArrayBlockingQueue<String>(10); 
	}
	
	public void init(){
		btAddress = Config.getInstance().getParam(BT_ADDRESS);
	}
    
	public Activity getActivity() { return activity; }

	public void setActivity(ExecActivity activity) { this.activity = activity; }

	public BluetoothDevice getToConnect() { return toConnect; }

	public void setToConnect(BluetoothDevice toConnect) {  
		this.toConnect = toConnect; 		
		btAddress = (toConnect == null) ? NON_SELECTED : toConnect.getAddress();
		Config.getInstance().setParam(BT_ADDRESS, btAddress);
	}
	
	public String getLinkState(){
		StringBuilder sb = new StringBuilder();
		sb.append("Address:").append(btAddress);		
		return sb.toString();
	}

	public BluetoothAdapter getBluetoothAdapter() { return bluetoothAdapter; }

	public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) { this.bluetoothAdapter = bluetoothAdapter; }

	public BluetoothSocket  getBluetoothSocket(){
		try {
			socket = toConnect.createRfcommSocketToServiceRecord(SECURE_UUID);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot generate socket",e);
		}
		return socket;
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
		
	
    private ArrayList<BluetoothDevice> getBluetoothDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //alarm("No BT on device");
            return null;
        }

        if (!bluetoothAdapter.isEnabled()) {
            //alarm("BT on device  disabled");
            return null;
        }
        
        

        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        if (deviceSet.isEmpty()) {
            //alarm("Pair first");
            return null;
        }

        if (deviceSet == null) {
            return null;
        }
        final ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>(deviceSet);
        return deviceList;
    }
	
	void pair(Activity ac){
        final ArrayList<BluetoothDevice> deviceList = getBluetoothDevices();
        if (deviceList == null) {
        	Toast.makeText(ac.getApplicationContext(), "No device list", Toast.LENGTH_LONG).show();
        } else {
        	showChooserAndConnect(ac, deviceList);	
        }
	}
	
	/**
	 * 
	 * @param deviceList
	 */
	
    void showChooserAndConnect(final Activity ac, final ArrayList<BluetoothDevice> deviceList) {
        ArrayList<CharSequence> itemList = new ArrayList<CharSequence>();
        for (BluetoothDevice device : deviceList) {
            itemList.add(device.getName() + " [" + device.getAddress() + "]");
        }
        CharSequence[] items = itemList.toArray(new CharSequence[itemList.size()]);

        setToConnect(null);

        AlertDialog dialog = new AlertDialog.Builder(ac)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	device = deviceList.get(which);
                    	String address = device.getAddress();
                    	String name = device.getName();
                    	System.out.println("DBG connect: Name="+name+" address="+address);
                        setToConnect(device);  
                    }
                })
                .create();
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (getToConnect() != null) {
                	Toast.makeText(ac.getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                    //connect();
                }
            }
        });
    }
}


