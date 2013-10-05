/*-
 * Arduindow - Opens and closes the windows in your house
 * using an Arduino and open weather data.
 *
 * Homepage: <https://github.com/bassosimone/arduindow>.
 *
 * See LICENSE for license conditions.
 *
 * Written by Fabio Vallone.
 */
package it.polito.nexa;

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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView (R.layout.activity_main);
		/**Disable StrictMode to allow the Network in the main thread.
		 * N.B. not necessary in case of Async Task (getJSON.java)**/
		if (Build.VERSION.SDK_INT >= 9){
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
			StrictMode.setThreadPolicy(policy);
		}
		/*Sets the seekbar to receive changes*/
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

	//execute this method if the button "AGGIORNA" is pressed
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


	/**VIEW UPDATE**/
	void refresh () {
		try{ //trying to read from file
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
		/* serverURL = et.getText().toString(); //* DEBUG without file */

		if (serverURL.length() == 0) {
			Toast t = Toast.makeText (MainActivity.this,"Inserire un server valido",Toast.LENGTH_SHORT);
			t.show();
			return ;
		}
		et.setHint(serverURL);

		//Get Json file and update the View
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

		//METEO STATION
		TextView textView = (TextView) findViewById(R.id.textHome);
	    textView.setText(json.getValue("meteo_station"));
	    textView.setTypeface(null, Typeface.BOLD);

	    //LAST UPDATE
	    textView = (TextView) findViewById(R.id.textHour);
	    textView.setText(json.getValue("date_fancy"));
	    textView.setTypeface(null, Typeface.BOLD);

	    //TEMPERATURE
	    textView = (TextView) findViewById(R.id.textTemp);
	    textView.setText(json.getValue("temperature_celsius"));
	    textView.setTypeface(null, Typeface.BOLD);

	    //DAILY PRECIPITATION
	    textView = (TextView) findViewById(R.id.textPrec);
	    textView.setText(json.getValue("precip_day"));
	    textView.setTypeface(null, Typeface.BOLD);

	    //WINDOW OPEN/CLOSE
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



	/**NOTICE: For the policy of "Strict Mode" by Android, with API >= 9,
	 *  in case of ftp connection, the app crashes;**/
	//Get the Json  OK
	String getJson () {

		String s = "";
		StringBuilder sb = new StringBuilder();
		//Try server connection
		try {
			URL url = new URL (serverURL+"?v");
			URLConnection uc = url.openConnection();
			//reading input stream
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

	//No MENU
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	/**CHANGING WINDOWS STATUS**/
	void changeWindowStatus (boolean newStatus) {
		URL url;

		try {
			String t;
			if (newStatus) {
				t = "open";
				//Toast s = Toast.makeText(MainActivity.this,"open",Toast.LENGTH_SHORT);
				//s.show(); DEBUG
			}
			else {
				//Toast s = Toast.makeText(MainActivity.this,"close",Toast.LENGTH_SHORT);
				//s.show(); DEBUG
				t = "close";
			}
			url = new URL (serverURL+"?e&"+t);
			URLConnection uc = url.openConnection ();
			InputStream is = new BufferedInputStream (uc.getInputStream());
			is.close();

		} catch (IOException e) {
		} catch(NullPointerException e){
		}

	}
}
