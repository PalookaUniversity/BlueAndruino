package com.palookaville.bluelink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
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
import android.widget.Toast;

public class BTLink  {

	private static final UUID SECURE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static String BT_ADDRESS = "BT_ADDRESS";
	public static String NONE_SELECTED = "none selected";

	BluetoothSocket socket;
	ExecActivity activity;
	BluetoothDevice device;
    String btAddress = NONE_SELECTED;
    String getBtAddress(){ return btAddress; }
    
    Boolean activated = false;
    Boolean isActive(){    	return activated;    }
    

	private BluetoothDevice toConnect;
	private BluetoothAdapter bluetoothAdapter;

	BlockingQueue<String> inQueue = new ArrayBlockingQueue<String>(10);
	BlockingQueue<String> outQueue = new ArrayBlockingQueue<String>(10);
	
	public BTLink(){}
	
	public void init(){
		btAddress = Config.getInstance().getParam(BT_ADDRESS);
	}
    
	public Activity getActivity() { return activity; }

	public void setActivity(ExecActivity activity) { this.activity = activity; }

	public BluetoothDevice getToConnect() { return toConnect; }

	public void setToConnect(BluetoothDevice toConnect) {  
		this.toConnect = toConnect; 		
		btAddress = (toConnect == null) ? NONE_SELECTED : toConnect.getAddress();
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
        	Toast.makeText(activity, "BT Link failure!", Toast.LENGTH_SHORT).show();

			throw new RuntimeException("Cannot generate socket",e);
		}
		writer.start();
		reader.start();
		activated = true;
		System.out.println("Reader and writer started.");
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
			e.printStackTrace();
		}
		System.out.println("Finished writing");
	}
	
	public Thread writer = new Thread(new Runnable(){
		@Override
		public void run() {
			try {
				while (socket.isConnected()) {
					String msgOut = outQueue.take();
					socket.getOutputStream().write(msgOut.getBytes());
				}
			} catch (Exception e) {
				e.printStackTrace();			}			
		}		
	});	


	public Thread reader = new Thread(new Runnable(){
		@Override
		public void run() {
			Integer ct = 0;
			try {
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
    	Set<BluetoothDevice> deviceSet = new HashSet<BluetoothDevice>();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
        	deviceSet = bluetoothAdapter.getBondedDevices();
        }
        return new ArrayList<BluetoothDevice>(deviceSet);
    }
    
    public BluetoothDevice previousBtDevice(Activity ac){
    	BluetoothDevice btDevice = null;
    	btAddress = Config.getInstance().getParam(BTLink.BT_ADDRESS, Config.NONE);
    	final ArrayList<BluetoothDevice> deviceList = getBluetoothDevices();
        if (deviceList == null) {
        	Toast.makeText(ac.getApplicationContext(), "No device list", Toast.LENGTH_LONG).show();
        } else {
    		for(BluetoothDevice bt: deviceList){
    			if (bt.getAddress().equals(btAddress)){
    				btDevice = bt;
    			}
    		}
        }
    	return btDevice;
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