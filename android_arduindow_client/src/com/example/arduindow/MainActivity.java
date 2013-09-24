package com.example.arduindow;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	String urlz = "";
	boolean open;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView (R.layout.activity_main);
		
		refresh ();
	}
	
	void refresh () {
		StringBuilder sb = new StringBuilder();
		try {
			FileInputStream fis = openFileInput ("server_name");
			int i = 0;
			while (i != -1) {
				i = fis.read();
				if (i != -1) {
					sb.append(Character.toChars(i));
				}
			}
			urlz = sb.toString();
			fis.close();
		} catch (FileNotFoundException e) {
			urlz="";
		} catch (IOException e) {
			urlz="";
		}

		if (urlz.length() == 0) {
			Toast t = Toast.makeText (MainActivity.this,"Please specify a server",Toast.LENGTH_SHORT);
			t.show();
			return ;
		}
		String jc = getJSON ();
		
		if (jc.length()==0) {
			return ;
		}
		
		JSONParse json = new JSONParse (jc);
		
		if (json.getValue("meteo_station").length()==0 ||
			json.getValue("date_fancy").length()==0 ||
			json.getValue("temperature_celsius").length()==0 ||
			json.getValue("precip_day").length()==0) {
			
			Toast t = Toast.makeText(MainActivity.this, "Something wrong with the server", Toast.LENGTH_SHORT);
			t.show();
			return ;
		}

		open = (json.getValue("window_status")=="open");
		List<Map<String,String>> s = getList (json);
		ListView lv = (ListView) findViewById(R.id.listView1);
		
		SimpleAdapter sa = new SimpleAdapter (this,s,R.layout.item,new String[]{"title","content"},new int[] {R.id.title,R.id.content});
		lv.setAdapter(sa);
	}
	
	List<Map<String,String>> getList (JSONParse j) {
		List<Map<String,String>> s = new ArrayList<Map<String,String>>();
		Map<String,String> hm = new HashMap<String,String>();
		hm.put ("title",getResources().getString(R.string.meteo_station));
		hm.put ("content",j.getValue("meteo_station"));
		s.add (hm);
		hm = new HashMap<String,String>();
		hm.put ("title",getResources().getString(R.string.last_update));
		hm.put ("content",j.getValue("date_fancy"));
		s.add (hm);
		hm = new HashMap<String,String>();
		hm.put ("title",getResources().getString(R.string.temperature));
		hm.put ("content",j.getValue("temperature_celsius"));
		s.add (hm);
		hm = new HashMap<String,String>();
		hm.put ("title", getResources().getString(R.string.rainfalls));
		hm.put ("content", j.getValue("precip_day"));
		s.add (hm);
		
		return s;
	}
	
	String getJSON () {
		String s = "";
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL (urlz+"?v");
			URLConnection uc = url.openConnection();
			InputStream is = new BufferedInputStream (uc.getInputStream());
			int i = 0;
			while (i != -1) {
				i = is.read ();
				if (i != -1) {
					sb.append (Character.toChars(i));
				}
			}
			s = sb.toString ();
			is.close ();
		}
		catch (MalformedURLException e) {
			Toast t = Toast.makeText(MainActivity.this,"Please make sure the server is up",Toast.LENGTH_SHORT);
			t.show();
		}
		catch (IOException e) {
			Toast t = Toast.makeText(MainActivity.this,"Please make sure the server is up",Toast.LENGTH_SHORT);
			t.show();
		}
		return s;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add (Menu.NONE,1,Menu.NONE,R.string.refresh);
		menu.add (Menu.NONE,2,Menu.NONE,R.string.server);
		menu.add (Menu.NONE,3,Menu.NONE,R.string.status);
		return true;
	}
	
	public boolean onOptionsItemSelected (MenuItem mi) {
		int id = mi.getItemId();
		AlertDialog.Builder bu;
		AlertDialog ad;
		switch (id) {
			/* Refresh */
			case 1:
				refresh ();
				break;
			/* Change server */
			case 2:
				bu = new AlertDialog.Builder(MainActivity.this);
				final EditText tv = new EditText (MainActivity.this);
				tv.setText (urlz);
				bu.setTitle("Change server");
				bu.setView (tv);
				bu.setPositiveButton ("Save",new DialogInterface.OnClickListener () {
					public void onClick (DialogInterface dialog, int which) {
						urlz = tv.getText().toString();
						try {
							FileOutputStream fos = openFileOutput ("server_name", Context.MODE_PRIVATE);
							fos.write(urlz.getBytes());
							fos.close();
							
						} catch (FileNotFoundException e) {
						} catch (IOException e) {
						}
						refresh ();
						
					}
				});
				ad = bu.create();
				ad.show();
				
				break;
			/* Switch (open/close) */
			case 3:
				bu = new AlertDialog.Builder(MainActivity.this);
				bu.setTitle ("Switch");
				ToggleButton tb = new ToggleButton (MainActivity.this);
				tb.setTextOff("Close");
				tb.setTextOn("Open");
				tb.setChecked(open);
				bu.setView(tb);
				tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener () {
					public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
						if ((!isChecked && open) || (isChecked && !open)) {
							Toast t=Toast.makeText(MainActivity.this, "LOL", Toast.LENGTH_SHORT);
							t.show();
							changeWindowStatus (isChecked);
						}
						/* Funny Stuff happens here :D */
					}
				});
				ad = bu.create ();
				ad.show ();
				
				break;
			default:
				break;
		}
		return true;
	}

	
	void changeWindowStatus (boolean newStatus) {
		URL url;
		
		try {
			String t;
			if (newStatus) {
				t = "open";
			}
			else {
				t = "close";
			}
			url = new URL (urlz+"?e&"+t);
			URLConnection uc = url.openConnection ();
			InputStream is = new BufferedInputStream (uc.getInputStream());
			is.close();
			
		} catch (IOException e) {
		}
		open = newStatus;
	}
}
