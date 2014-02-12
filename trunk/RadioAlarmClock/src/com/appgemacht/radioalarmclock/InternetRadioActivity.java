package com.appgemacht.radioalarmclock;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;

import android.app.Activity;
import android.content.*;
import android.media.AudioManager;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.appgemacht.radioalarmclock.InternetRadio.InternetRadioListener;

/**
 * Activity to be used for adjusting the settings related with internet radio
 * streams - URL, volume, etc.
 * 
 * It facilitates the package internal class InternetRadio
 * 
 * @author Dietmar (derived from c't sample application RadioAlarmClock)
 * 
 */
public class InternetRadioActivity extends Activity implements
        InternetRadioListener {
    private final InternetRadio radio = new InternetRadio();
    private AudioManager audioManager;
    private int originalVolume; // Wiederherstellen der Original-Lautstärke beim
                                // Verlassen der Activity
    private TextView editStreamURL;
    private SeekBar seekVolume;
    private TextView statusView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setradio);
        setTitle("Radio");

        editStreamURL = (TextView) findViewById(R.id.editStreamURL);
        editStreamURL.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                radio.storeStreamURL(InternetRadioActivity.this, editStreamURL
                        .getText().toString());
                startRadio();
            }
        });
        statusView = (TextView) findViewById(R.id.statusView);

        seekVolume = (SeekBar) findViewById(R.id.seekVolume);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        seekVolume.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                    radio.storeVolume(InternetRadioActivity.this, progress);
                }
            }
        });

        ((ImageButton) findViewById(R.id.lookupStations))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lookupStations();
                    }
                });

        radio.internetRadioListener = this;
    }

    protected void lookupStations() {
        String url = "http://www.listenlive.eu/germany.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
        // Hinweis was zu tun ist
        Toast.makeText(this, R.string.ClickStreamLink, Toast.LENGTH_LONG)
                .show();
    }

    private void startRadio() {
        if (radio.loadStreamURL(this).length() != 0) {
            statusView.setText(R.string.LoadingStation);
            radio.start(this, false);
        } else
            statusView.setText(R.string.NoRadioStream);

    }

    @Override
    public void onInternetRadioPrepareFinished() {
        statusView.setText("");
    }

    @Override
    public void onInternetRadioError(String error) {
        statusView.setText(error);
    }

    @Override
    protected void onResume() {
        super.onResume();
        originalVolume = audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        editStreamURL.setText(radio.loadStreamURL(this));
        seekVolume.setProgress(radio.loadVolume(this));

        Uri uri = getIntent().getData();
        if (uri != null) {
            // network activity not allowed on UI thread
            AsyncTask<Uri, Void, Void> asyncTask = new AsyncTask<Uri, Void, Void>() {
                @Override
                protected Void doInBackground(Uri... uri) {
                    Log.v("Radio", "Open URI " + uri[0]);
                    final String streamURL = openURI(uri[0]);
                    Log.v("Radio", "Stream URL " + streamURL);
                    if (streamURL != null) {
                        // back to UI thread for updating text
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editStreamURL.setText(streamURL);
                                radio.storeStreamURL(
                                        InternetRadioActivity.this, streamURL);
                                startRadio();
                            }
                        });
                    }
                    return null;
                }
            };
            asyncTask.execute(uri);
        }
        getIntent().setData(null); // nicht mehrfach öffnen
        startRadio();
    }

    private String openURI(Uri uri) {
        // handle local and remote uris
        String data;
        if (uri.getScheme() != null && uri.getScheme().startsWith("http"))
            data = downloadRemoteURI(uri); // remote file, z.B. Browser-Link
        else
            data = openLocalURI(uri); // local file, z.B. Mail-Anhang
        if (data != null) {
            Matcher matcher = Patterns.WEB_URL.matcher(data);
            if (matcher.find()) {
                String streamURL = matcher.group();
                if (!streamURL.startsWith("http")) // manchmal lässt der Matcher
                                                   // das Schema weg
                    streamURL = "http://" + streamURL;
                Log.v("Radio", "Stream URL: " + streamURL);
                return streamURL;
            }
        }
        return null;
    }

    private String openLocalURI(Uri uri) {
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(uri);
            BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                    "UTF8"), 4096);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            return sb.toString();
        } catch (Exception e) {
            Log.e("uri", "No data from " + uri);
            return null;
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    Log.v("uri", e.getLocalizedMessage());
                }
        }
    }

    private String downloadRemoteURI(Uri uri) {
        InputStream is = null;
        try {
            URL url = new URL(uri.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10 * 1000);
            is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                    "UTF8"), 4096);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line + "\n");
            }
            rd.close();
            return sb.toString();

        } catch (IOException e) {
            Log.e("Radio", "Error downloading " + uri, e);
            return null;
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    Log.v("uri", e.getLocalizedMessage());
                }
        }
    }

    @Override
    protected void onPause() {
        radio.stop();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume,
                0);
        super.onPause();
    }
}
