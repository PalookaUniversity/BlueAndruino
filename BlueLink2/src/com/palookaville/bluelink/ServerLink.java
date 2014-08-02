package com.palookaville.bluelink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.palookaville.bluelink.AsynchWebAction.AsyncWebAction;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;



public class ServerLink {
	

	void crash(String msg, Throwable t){
		System.out.println(msg);
		t.printStackTrace();
		throw new RuntimeException(msg, t);
	}
	
	public class Fetch implements AsyncWebAction {
		
		String result;
		String status = "OK";
		String query;
		Activity activity;
		String label;
		
		public Fetch(String query, Activity activity, String label){
			this.query = query;
			this.activity = activity;
			this.label = label;
		}

		/* (non-Javadoc)
		 * @see com.cj.votron.AsyncWebAction#exec()
		 */
		@Override
		public void exec() {
						
			try {
				HttpURLConnection con = 
						(HttpURLConnection) new URL(query).openConnection();
				String rawJsonStr = readStream(con.getInputStream());
				//result = jsoneriseObject(rawJsonStr);
				result = voterList(rawJsonStr);
				

			} catch (Exception e) {
				crash("Error:  Exec crashed", e);
			}
			return;
		}
		
		private String readStream(InputStream in) {
			  BufferedReader reader = null;
			  StringBuilder sb = new StringBuilder();
			  try {
			    reader = new BufferedReader(new InputStreamReader(in));
			    String line = "";
			    while ((line = reader.readLine()) != null) {
			    	sb.append(line);
			    }
			  } catch (IOException e) {
			    e.printStackTrace();
			  } finally {
			    if (reader != null) {
			      try {
			        reader.close();
			      } catch (IOException e) {
			        e.printStackTrace();
			        }
			    }
			  }
			  return sb.toString();
			} 

		/* (non-Javadoc)
		 * @see com.cj.votron.AsyncWebAction#followUp()
		 */
		@Override
		public void followUp() {
			System.out.println("DBG: Follow up");
			System.out.println(result);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	ConfigActivity ca = (ConfigActivity)activity;
                	ca.display(result);
                    //mTextViewStrength.append(" " + getString(R.string.disconnected));
                }
            });
			Config.getInstance().setParam(label, result);
			ConfigActivity.displayBuffer = result;
		}



		/* (non-Javadoc)
		 * @see com.cj.votron.AsyncWebAction#getStatus()
		 */
		@Override
		public String getStatus() { return status; }

		/* (non-Javadoc)
		 * @see com.cj.votron.AsyncWebAction#getResult()
		 */
		@Override
		public String getResult() { return result; }
	}
	
	String buffer;
	Activity currentActivity;
	private final static String URL_TARGET = "";
	static ServerLink instance = new ServerLink();

	public static ServerLink getInstance() { return instance; }
	
	/**************************************************
	 * 
	 * Election tests
	 * 
	 **************************************************/
	
//	void getElectionQuery(String query, Activity activity) {
//		Fetch fetch = new Fetch(Config.SERVER + "/" + query, activity, "elections");
//		asyncAction(activity,fetch);
//		return;
//	}
	
	
	/**************************************************
	 * 
	 * Dbpedia tests
	 * 
	 **************************************************/



	private static final String DBPEDIAQ = "http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=";
	private static final String DPEDIA_JSONSPEC = "&format=json";

	void getDbpediaQuery(String query, Activity activity) {
		Fetch fetch = new Fetch(DBPEDIAQ + query + DPEDIA_JSONSPEC, activity, "dbpedia");
		asyncAction(activity,fetch);
		return;
	}
	
	void asyncAction(final Activity activity, final Fetch fetch) {
		new AsyncTask<Context, Void, Void>() {
			@Override
			protected Void doInBackground(Context... backgroundListOfParameters) {
				try {
					fetch.exec();
					fetch.followUp();
				} catch (Exception e) {
					crash("Error: Async crashed",e);
				}
				return null;
			}
		}.execute();
	}
	
	String getASCIIContentFromEntity(HttpEntity entity)
			throws IllegalStateException, IOException {

		InputStream in = entity.getContent();
		StringBuffer out = new StringBuffer();
		int n = 1;
		while (n > 0) {
			byte[] b = new byte[4096];
			n = in.read(b);
			if (n > 0)
				out.append(new String(b, 0, n));
		}
		return out.toString();
	}


	private void debug(String msg) {
		// TODO: FOR DEBUGGING
		// Log.d(getClass().getName(), msg);
	}
	
	
	/***********************************************
	 * 
	 * JSON processing
	 * 
	 ************************************************/
	
	String jsoneriseObject(String rawJsonStr){
		
		String cleanJson = "WTF?";
		try {
			JSONObject jsonObj = new JSONObject(rawJsonStr);
			Iterator ks = jsonObj.keys();
			Integer i = 0;
			while (ks.hasNext()){
				Object o = ks.next();
				String str = o.toString();
				String cls = o.getClass().toString();
				System.out.println(">" + i + ":."+cls+"="+str);
				System.out.println();
			}
			System.out.println("DBG: Maybe we got some?");
			cleanJson = jsonObj.toString();
			System.out.println(cleanJson);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return cleanJson;
	}
	
	String voterList(String rawJsonStr){
		
		String cleanJson = "WTF?";
		StringBuilder sb = new StringBuilder();
		try {
			JSONArray arr = new JSONArray(rawJsonStr);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject obj = arr.getJSONObject(i);
				String voterName = (String) obj.get("name");
				//String str = arr.getString(i);
				sb.append(voterName).append(",");
				System.out.println (voterName);
				}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return sb.toString();
	}
	
	String electionList(String rawJsonStr){
		
		String cleanJson = "WTF?";
		try {
			JSONArray arr = new JSONArray(rawJsonStr);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject obj = arr.getJSONObject(i);
				String str = arr.getString(i);
				System.out.println (str);
				}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return cleanJson;
	}
}