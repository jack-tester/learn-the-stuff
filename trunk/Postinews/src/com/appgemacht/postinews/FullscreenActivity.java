package com.appgemacht.postinews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.appgemacht.postinews.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
  /**
   * Whether or not the system UI should be auto-hidden after
   * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
   */
  private static final boolean AUTO_HIDE = true;

  /**
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user
   * interaction before hiding the system UI.
   */
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  /**
   * If set, will toggle the system UI visibility upon interaction. Otherwise,
   * will show the system UI visibility upon interaction.
   */
  private static final boolean TOGGLE_ON_CLICK = true;

  /**
   * The flags to pass to {@link SystemUiHider#getInstance}.
   */
  private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

  /**
   * The instance of the {@link SystemUiHider} for this activity.
   */
  private SystemUiHider mSystemUiHider;

  private String postiNews;
  private static final String SLOGAN_START = "{\"text\":";
  private static final String SLOGAN_END = "\",";
  private int remainingPostiNewsSlogans = 0;
  
  private int lastSloganStartIndex = 0;
  private int lastSloganEndIndex = 0;
  
  private Object context;

  private String getPostiNews(InputStream in) {
    String line = "";
    BufferedReader reader = null;
    reader = new BufferedReader(new InputStreamReader(in,Charset.forName("UTF-8")));
    if (reader != null) {
      try {
//          while ((line = reader.readLine()) != null) {
//            System.out.println(line);
//          }
        line = reader.readLine();
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return line;
  }
  
  private void readStream(InputStream in) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(in));
      String line = "";
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
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
  }   
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_fullscreen);

    final View controlsView = findViewById(R.id.fullscreen_content_controls);
    final View contentView = findViewById(R.id.fullscreen_content);

    // Set up an instance of SystemUiHider to control the system UI for
    // this activity.
    mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
    mSystemUiHider.setup();
    mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
          // Cached values.
          int mControlsHeight;
          int mShortAnimTime;

          @Override
          @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
          public void onVisibilityChange(boolean visible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
              // If the ViewPropertyAnimator API is available
              // (Honeycomb MR2 and later), use it to animate the
              // in-layout UI controls at the bottom of the
              // screen.
              if (mControlsHeight == 0) {
                mControlsHeight = controlsView.getHeight();
              }
              if (mShortAnimTime == 0) {
                mShortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);
              }
              controlsView.animate()
                  .translationY(visible ? 0 : mControlsHeight)
                  .setDuration(mShortAnimTime);
            } else {
              // If the ViewPropertyAnimator APIs aren't
              // available, simply show or hide the in-layout UI
              // controls.
              controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
            }

            if (visible && AUTO_HIDE) {
              // Schedule a hide().
              delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
          }
        });

    // Set up the user interaction to manually show or hide the system UI.
    contentView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (TOGGLE_ON_CLICK) {
          mSystemUiHider.toggle();
         
          /**
           * change the text displayed in the back of the view ...
           * (later one I would like to display stuff from 
           *    http://www.der-postillion.de/ticker/newsticker2.php
           *  here)
           */
          TextView tv;
          tv = (TextView) findViewById(R.id.fullscreen_content);//="@+id/fullscreen_content")
          
          if (remainingPostiNewsSlogans > 0) {
            
            lastSloganStartIndex = postiNews.indexOf(SLOGAN_START,lastSloganStartIndex) + SLOGAN_START.length() + "\"".length();
            lastSloganEndIndex = postiNews.indexOf(SLOGAN_END,lastSloganStartIndex); 
                
            String postiNewsSlogan = postiNews.substring( lastSloganStartIndex, lastSloganEndIndex );

            tv.setText(postiNewsSlogan);

            remainingPostiNewsSlogans--;
            
          } else {            
            try {
              // StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
              // StrictMode.setThreadPolicy(policy); 
              URL url = new URL("http://www.der-postillion.de/ticker/newsticker2.php");
              HttpURLConnection con = (HttpURLConnection) url.openConnection();
              InputStream inpStream = con.getInputStream();
              // the following call prints the returned string to the console 
              // !!!!!!!!!!!!!!!!!!!!!!!!
              postiNews = getPostiNews(inpStream);
              
              // scan news for number of slogans ...
              {
                int lastIdx = 0;
                
                while(lastIdx != -1) {
                  lastIdx = postiNews.indexOf(SLOGAN_START,lastIdx);
                  if (lastIdx != -1) {
                    remainingPostiNewsSlogans ++;
                    lastIdx += SLOGAN_START.length();
                  }
                }
                System.out.println(remainingPostiNewsSlogans);
              }
              
            } catch (Exception e) {
                e.printStackTrace();
            }          
          
            tv.setText("Ooooh Mann ...\n "+String.valueOf(remainingPostiNewsSlogans)+" neue Nachrichten\n vom Postillion !");
          }
        } else {
          mSystemUiHider.show();
        }
      }
    });

    // Upon interacting with UI controls, delay any scheduled hide()
    // operations to prevent the jarring behavior of controls going away
    // while interacting with the UI.
    findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    
    // check internet connectivity and try to enable WLAN if not yet connected ...
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();

    if (netInfo != null)
    {
      TextView tv;
      tv = (TextView) findViewById(R.id.fullscreen_content);//="@+id/fullscreen_content")
      if (netInfo.isConnectedOrConnecting()) {
        tv.setText("yeapi-ya-ya--yeapi-ya-ya-yeeeee !!");
      } else {
        tv.setText("... ???");
        
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
      }    
    } else {
      WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
      wifiManager.setWifiEnabled(true);
    }
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    delayedHide(100);
  }

  /**
   * Touch listener to use for in-layout UI controls to delay hiding the system
   * UI. This is to prevent the jarring behavior of controls going away while
   * interacting with activity UI.
   */
  View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (AUTO_HIDE) {
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
      }
      return false;
    }
  };

  Handler mHideHandler = new Handler();
  Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
      mSystemUiHider.hide();
    }
  };

  /**
   * Schedules a call to hide() in [delay] milliseconds, canceling any
   * previously scheduled calls.
   */
  private void delayedHide(int delayMillis) {
    mHideHandler.removeCallbacks(mHideRunnable);
    mHideHandler.postDelayed(mHideRunnable, delayMillis);
  }
}
