package com.baf.arduindow;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	String serverURL = "";
	boolean open;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView (R.layout.activity_main);
		/**Disattiva la StrictMode per permettere il Network nel main thread. 
		 * N.B. non necessario in caso di Async Task (getJSON.java)**/
		if (Build.VERSION.SDK_INT >= 9){
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
			StrictMode.setThreadPolicy(policy);
		}
		/*Imposta la seekbar per ricevere le variazioni*/
		SeekBar bar = (SeekBar) findViewById(R.id.seekBar1); 
		bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			int progressChanged = 0;
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				progressChanged = progress;				
            }
 
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing to do
            }
 
            public void onStopTrackingTouch(SeekBar seekBar) {
            	if(progressChanged>50){
					changeWindowStatus(true);
				} else{
					changeWindowStatus(false);
				}
				refresh();
            }
		});
		refresh ();		
	}	
	
	//Se viene premuto il bottone "AGGIORNA" esegui questa parte
	public void button(View view)
	{
		EditText editText = (EditText) findViewById(R.id.editServer);
	    serverURL = editText.getText().toString();
	    try {
			FileOutputStream fos = openFileOutput ("my_server", Context.MODE_PRIVATE);
			fos.write(serverURL.getBytes());
			fos.close();
			
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} 
		refresh();
	} //
	
	
	/**AGGIORNAMENTO VIEW**/
	void refresh () {
		try{ //prova la lettura da file
			FileInputStream input;
			input = openFileInput("my_server");
			StringBuffer content = new StringBuffer("");
			
			byte[] buffer = new byte[1024];
			while(input.read(buffer) != -1){
				content.append(new String(buffer));
			}
			serverURL=content.toString();
			input.close();
    	}catch(FileNotFoundException e){
				serverURL="";
	   	}catch(IOException e){
				serverURL="";
		} 
		
		EditText et = (EditText) findViewById(R.id.editServer);
		/* serverURL = et.getText().toString(); //* DEBUG senza file */

		if (serverURL.length() == 0) {
			Toast t = Toast.makeText (MainActivity.this,"Inserire un server valido",Toast.LENGTH_SHORT);
			t.show();
			return ;
		}
		et.setHint(serverURL);
		
		//Richiedi il Json e aggiorna la View di conseguenza
		String jc = getJson ();
		
		if (jc.length()==0) {
			return ;
		}
		
		JSONParse json = new JSONParse (jc);
		
		if (json.getValue("meteo_station").length()==0 ||
			json.getValue("date_fancy").length()==0 ||
			json.getValue("temperature_celsius").length()==0 ||
			json.getValue("precip_day").length()==0) {
			
			Toast t = Toast.makeText(MainActivity.this, "Problema con il server", Toast.LENGTH_SHORT);
			t.show();
			return ;
		} 

		/* open = (json.getValue("window_status")=="open"); */
		
		//STAZIONE METEO
		TextView textView = (TextView) findViewById(R.id.textHome);
	    textView.setText(json.getValue("meteo_station"));
	    textView.setTypeface(null, Typeface.BOLD);
	    
	    //ULTIMO AGGIORNAMENTO
	    textView = (TextView) findViewById(R.id.textHour);
	    textView.setText(json.getValue("date_fancy"));
	    textView.setTypeface(null, Typeface.BOLD);
	    
	    //TEMPERATURA
	    textView = (TextView) findViewById(R.id.textTemp);
	    textView.setText(json.getValue("temperature_celsius"));
	    textView.setTypeface(null, Typeface.BOLD);
	    
	    //PRECIPITAZIONI
	    textView = (TextView) findViewById(R.id.textPrec);
	    textView.setText(json.getValue("precip_day"));
	    textView.setTypeface(null, Typeface.BOLD);
	    
	    //APERTURA CHIUSURA FINESTRA
	    textView = (TextView) findViewById(R.id.textFin);
	    SeekBar bar = (SeekBar) findViewById(R.id.seekBar1);
	    if(json.getValue("window_status").compareTo("open")==0){
	    	textView.setText("APERTO");
	    	textView.setTypeface(null, Typeface.BOLD);
	    	bar.setProgress(100);
	    }else{
	    	textView.setText("CHIUSO");
	    	textView.setTypeface(null, Typeface.BOLD);
	    	bar.setProgress(0);
	    }
	}
	
	
	
	/**ATTENZIONE: per la politica dello "Strict Mode" con Android API >= 9, in caso di connessioni ftp:// l'app crasha;
	 * Spostare il getJSON() in un Async Task (getJSON.java) **/
	//Ricevere il Json  OK
	String getJson () {
		
		String s = "";
		StringBuilder sb = new StringBuilder();
		//Provo la connessione al server
		try {
			URL url = new URL (serverURL+"?v"); 
			URLConnection uc = url.openConnection();
			//lettura stream in input
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
		}catch(NullPointerException e){
			Toast t = Toast.makeText(MainActivity.this,"Controllare la connessione con il server",Toast.LENGTH_SHORT);
			t.show();
		}
		catch (MalformedURLException e) {
			Toast t = Toast.makeText(MainActivity.this,"Formato URL non valido",Toast.LENGTH_SHORT);
			t.show();
		}
		catch (IOException e) {
			Toast t = Toast.makeText(MainActivity.this,"Controllare la connessione con il server",Toast.LENGTH_SHORT);
			t.show();
		} catch(IllegalStateException e){
			Toast t = Toast.makeText(MainActivity.this,"Formato URL non valido", Toast.LENGTH_SHORT);
			t.show();
		}
		return s; 
	} /**OK**/

	//Nessun MENU
	@Override 
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	/**PER APRIRE O CHIUDERE LA FINESTRA**/
	void changeWindowStatus (boolean newStatus) {
		URL url;
		
		try {
			String t;
			if (newStatus) {
				t = "open";
				//Toast s = Toast.makeText(MainActivity.this,"open",Toast.LENGTH_SHORT);
				//s.show();
			}
			else {
				//Toast s = Toast.makeText(MainActivity.this,"close",Toast.LENGTH_SHORT);
				//s.show();
				t = "close";
			}
			url = new URL (serverURL+"?e&"+t);
			URLConnection uc = url.openConnection ();
			InputStream is = new BufferedInputStream (uc.getInputStream());
			is.close();
			
		} catch (IOException e) {
		} catch(NullPointerException e){
		}
		open = newStatus;
	}
}
