package test.yota_test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    String text = "";
    String url = "";
    private static TextView textView;
    private static EditText edittext;
    GetHtmlTextTask getHtmlTextTask;
    BroadcastReceiver networkStateReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        edittext = (EditText) findViewById(R.id.editText);
        edittext.setSelection(edittext.getText().length());
        edittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    url = edittext.getText().toString();
                    getHtmlTextTask = new GetHtmlTextTask();
                    getHtmlTextTask.execute();
                    return true;
                }
                return false;
            }
        });

    }

    class GetHtmlTextTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            edittext.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            text = getUrlSource(url);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            textView.setText(text);
            edittext.setEnabled(true);
        }
    }
    private String getUrlSource(String input) {
        boolean redirect = false;
        try {
            URL url = new URL(input);
            URLConnection conn = url.openConnection();
            HttpURLConnection urlConnection = (HttpURLConnection) conn;
            int status = urlConnection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }
            if (redirect) {
                String newUrl = urlConnection.getHeaderField("Location");
                urlConnection.disconnect();
                urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();
            }
            urlConnection.setInstanceFollowRedirects(true);
            HttpURLConnection.setFollowRedirects(true);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder a = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                a.append(inputLine);
            in.close();
            return a.toString();
        }
        catch (IOException e) {return "Ошибка подключения к " + input;}
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                final View coordinatorLayout = findViewById(R.id.coordinatorLayout);
                final ConnectivityManager connMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                final android.net.NetworkInfo conn = connMgr
                        .getActiveNetworkInfo();

                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, R.string.internet_required, Snackbar.LENGTH_INDEFINITE);
                if (conn != null && conn.isAvailable()) {
                    MainActivity.edittext.setEnabled(true);
                }
                else
                {
                    edittext.setEnabled(false);
                    snackbar.show();
                }
            }
        };
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStateReceiver);
    }
}
