package org.tensorflow.lite.examples.classification;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class SendLoginData extends AsyncTask<String, Void, String> {

    String resultString = null;
    String parammetrs = null;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String myURL = "http://192.168.1.16:5000/api/upload_image";
            byte[] data = null;
            InputStream is = null;

            try {
                URL url = new URL(myURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.setRequestProperty("Content-Length", "" + Integer.toString(parammetrs.getBytes().length));
                OutputStream os = conn.getOutputStream();
                data = parammetrs.getBytes("UTF-8");
                os.write(data);
                data = null;

                conn.connect();
                int responseCode= conn.getResponseCode();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                if (responseCode == 200) {
                    is = conn.getInputStream();
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    data = baos.toByteArray();
                    resultString = new String(data, "UTF-8");
                    Log.i("123",resultString);
                } else {
                }



            } catch (MalformedURLException e) {

                //resultString = "MalformedURLException:" + e.getMessage();
            }
            catch (Exception e) {

                //resultString = "Exception:" + e.getMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    @Override
//    protected void onPostExecute(Void result) {
//        super.onPostExecute(result);
//        if(resultString != null) {
//            Log.i("123",resultString);
//        }
//
//    }

}
