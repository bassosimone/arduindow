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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

public class getJSON extends AsyncTask<URL, Void, String> {
	protected String doInBackground(URL... urls){
		String s = "";
		StringBuilder sb = new StringBuilder();
		try {
			URLConnection uc = urls[0].openConnection();
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
			s="NP";
		}
		catch (MalformedURLException e) {
			s="MU";
		}
		catch (IOException e) {
			s="IO";
		}
		return s;
		
	}

}
