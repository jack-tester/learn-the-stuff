package com.appgemacht.postinews;

import java.io.BufferedReader;
import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.UnsupportedEncodingException;
//import java.net.HttpURLConnection;
import java.net.URL;
//import java.nio.charset.Charset;
import java.util.Scanner;



//import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
//import android.os.Build;
import android.os.Bundle;
//import android.os.Handler;
import android.util.Log;
//import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.appgemacht.postinews.util.ExternalPostiSloganStorage;
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
//  private static final boolean AUTO_HIDE = true;

  /**
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user
   * interaction before hiding the system UI.
   */
//  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  /**
   * If set, will toggle the system UI visibility upon interaction. Otherwise,
   * will show the system UI visibility upon interaction.
   */
  private static final boolean TOGGLE_ON_CLICK = true;

  /**
   * The flags to pass to {@link SystemUiHider#getInstance}.
   */
//  private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

  /**
   * The instance of the {@link SystemUiHider} for this activity.
   */
//  private SystemUiHider mSystemUiHider;

  private String postiNews;
  private String postiNewsSlogan;
  private int postiNewsSloganRating;
  
  private static final String POSTINEWS_URL = "http://www.der-postillion.de/ticker/newsticker2.php";
  private static final String SLOGAN_START = "{\"text\":";
  private static final String SLOGAN_END = "\",";
  private static final String UNICODE_POINT_PATTERN = "\\\\{1}u[0-9a-fA-F]{4}";
  private int remainingPostiNewsSlogans = 0;
  
  private int lastSloganStartIndex = 0;
  private int lastSloganEndIndex = 0;
  
  private boolean netconnected = false;
  
  // the rating bar object must be reset by opening next slogan
  private static RatingBar postiNewsRating = null;
  private static TextView postiNewsHint = null;
  private static ProgressBar postiNewsProgress = null;
//  private static ExternalPostiSloganStorage sloganStorage = null;

  @Override
  protected void onDestroy() {
    super.onDestroy();
    
    // restore the original Wifi status ... and print a log message about the final state
    String s;
    if (netconnected) {
      s = new String("Nett connected, mein Schatz !");
    } else {
      WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
      wifiManager.setWifiEnabled(false);

      s = new String("Net connected, mei' Schatz'le !");
    }
    Log.v("INITIAL_NET_CONNECT_STATE: ",s);
  }
  
  private ExternalPostiSloganStorage sloganStorage;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // determine the content view that's active ...
    setContentView(R.layout.activity_fullscreen);

    // initiate and reset the rating bar - it is disabled as long as no slogan is displayed
    postiNewsRating = (RatingBar) findViewById(R.id.postiNewsRating);
    postiNewsHint = (TextView) findViewById(R.id.postiNewsHint);
    postiNewsProgress = (ProgressBar) findViewById(R.id.progress_goingOnline);
    postiNewsRating.setRating(0);
    postiNewsRating.setVisibility(RatingBar.VISIBLE);
    postiNewsRating.setEnabled(false);
    postiNewsProgress.setVisibility(ProgressBar.INVISIBLE);
    // the hint text is hidden ...
    postiNewsHint.setVisibility(TextView.INVISIBLE);
    
    // check external storage capabilities
    sloganStorage = new ExternalPostiSloganStorage();
    if (! sloganStorage.isWritable()) {
      ///// TODO - check, what could be done towards HMI counter part to follow up appropriately ...
      Log.e("EXTERNAL_STORAGE_STATE: ","not writable !");
      exit();
    }
    
    sloganStorage.openFile();

    final View contentView = findViewById(R.id.fullscreen_content);
    
    // Set up an instance of SystemUiHider to control the system UI for
    // this activity.
//    mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
//    mSystemUiHider.setup();
//    mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
//          // Cached values.
//          int mControlsHeight;
//          int mShortAnimTime;
//
//          @Override
//          @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//          public void onVisibilityChange(boolean visible) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//              // If the ViewPropertyAnimator API is available
//              // (Honeycomb MR2 and later), use it to animate the
//              // in-layout UI controls at the bottom of the
//              // screen.
//              if (mControlsHeight == 0) {
//                mControlsHeight = controlsView.getHeight();
//              }
//              if (mShortAnimTime == 0) {
//                mShortAnimTime = getResources().getInteger(
//                    android.R.integer.config_shortAnimTime);
//              }
//              controlsView.animate()
//                  .translationY(visible ? 0 : mControlsHeight)
//                  .setDuration(mShortAnimTime);
//            } else {
//              // If the ViewPropertyAnimator APIs aren't
//              // available, simply show or hide the in-layout UI
//              // controls.
//              controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
//            }
//
//            if (visible && AUTO_HIDE) {
//              // Schedule a hide().
//              delayedHide(AUTO_HIDE_DELAY_MILLIS);
//            }
//          }
//        });

    postiNewsRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
      @Override
      public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        /**
         * store the newly chosen rating for later storing of the slogan ...
         */
        if (rating > 0) {
          postiNewsSloganRating = (int) rating;
        }
      }
    });
    
    // Set up the user interaction to manually show or hide the system UI.
    contentView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
//        if (TOGGLE_ON_CLICK) {
//          mSystemUiHider.toggle();
         
          /**
           * change the text displayed in the back of the view ...
           * (later one I would like to display stuff from 
           *    http://www.der-postillion.de/ticker/newsticker2.php
           *  here)
           */
          TextView tv;
          tv = (TextView) findViewById(R.id.fullscreen_content);//="@+id/fullscreen_content")
          
          if (remainingPostiNewsSlogans > 0) {
            
            /* store the currently displayed slogan */
            if (postiNewsSloganRating > 0) {
              sloganStorage.write(postiNewsSlogan,postiNewsSloganRating);
              Log.v("PostiNews ","stored with rating " + postiNewsSloganRating + "\n");
            } else {
              Log.v("PostiNews ","not stored\n");
            }
            
            /* display the next slogan */
            lastSloganStartIndex = postiNews.indexOf(SLOGAN_START,lastSloganStartIndex) + SLOGAN_START.length() + "\"".length();
            lastSloganEndIndex = postiNews.indexOf(SLOGAN_END,lastSloganStartIndex); 
                
            postiNewsSlogan = postiNews.substring( lastSloganStartIndex, lastSloganEndIndex );
            
            Log.v("PostiNews",postiNewsSlogan);
            System.out.print(postiNewsSlogan);

            // replace unicode code points with 'deutsche Umlaute'
            {
              // following code idea found at "http://stackoverflow.com/questions/12640106/android-unicode-to-readable-string"
              //  As not the whole, but just a few char's in postiNewsSlogan's are represented as unicode strings
              //  we need to manually detect and convert them ...
              Scanner scanner =  new Scanner(postiNewsSlogan);
              String unicodeCharStr;
              char unicodeChar;
              
              while(true) {
                unicodeCharStr = scanner.findWithinHorizon(UNICODE_POINT_PATTERN, 0); // horizon=0 -> search in whole scanner input
                if (unicodeCharStr == null) break;
                unicodeChar = (char)(int)Integer.valueOf(unicodeCharStr.substring(2, 6), 16);
                postiNewsSlogan = postiNewsSlogan.replace(unicodeCharStr, unicodeChar+"");
              }
            }
            // replace the 2 back slash that proceed the quotation mark with just one back slash equipped quotation mark
            postiNewsSlogan = postiNewsSlogan.replaceAll("\\\\\"", "\"");
            // resolve HTML code of '&'
            postiNewsSlogan = postiNewsSlogan.replaceAll("&amp;", "&");
            // resolve further HTML like codes starting with '&#'
            // ...todo...
            
            CharSequence cs = postiNewsSlogan.subSequence(0, postiNewsSlogan.length());
            tv.setText(cs);
            
            remainingPostiNewsSlogans--;
            
            postiNewsRating.setRating(0);
            postiNewsSloganRating = 0;
            
            if (sloganStorage.containsStringWithHash(postiNewsSlogan.hashCode())) {
              postiNewsRating.setVisibility(RatingBar.GONE);
              postiNewsHint.setText("... das ist schon gespeichtert.");
              postiNewsHint.setVisibility(TextView.VISIBLE);
            } else {
              postiNewsRating.setVisibility(RatingBar.VISIBLE);
              postiNewsHint.setVisibility(TextView.INVISIBLE);
            }
            
          } else {
            
            try {
              postiNewsProgress.setVisibility(ProgressBar.VISIBLE);
              
              URL url = new URL(POSTINEWS_URL);

              BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));              
              postiNews = in.readLine();
              
              Log.v("PostiNews",postiNews);
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
              }
            } catch (Exception e) {
                e.printStackTrace();
            }          
          
            lastSloganStartIndex = 0;
            lastSloganEndIndex = 0;
            
            if (remainingPostiNewsSlogans == 0) {
              tv.setText(" ...\n tippe nochmal, bitte ... ");
            } else {

              postiNewsProgress.setVisibility(ProgressBar.INVISIBLE);     
              
              tv.setText(" ...\n "+String.valueOf(remainingPostiNewsSlogans)+" neue Postillion News !");
              postiNewsRating.setEnabled(true);
            }
          }
//        } else {
//          mSystemUiHider.show();
//        }
      }
    });

    // Upon interacting with UI controls, delay any scheduled hide()
    // operations to prevent the jarring behavior of controls going away
    // while interacting with the UI.
//    findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    
    // check Internet connectivity and try to enable WLAN if not yet connected ...
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();

    if (netInfo != null)
    {
      TextView tv;
      tv = (TextView) findViewById(R.id.fullscreen_content);//="@+id/fullscreen_content")
      if (netInfo.isConnectedOrConnecting()) {
        netconnected = true;
      } else {
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        postiNewsProgress.setVisibility(ProgressBar.VISIBLE);     
      }    
    } else {
      WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
      wifiManager.setWifiEnabled(true);
      postiNewsProgress.setVisibility(ProgressBar.VISIBLE);     
    }
  }

  private void exit() {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
//    delayedHide(100);
  }

//  /**
//   * Touch listener to use for in-layout UI controls to delay hiding the system
//   * UI. This is to prevent the jarring behavior of controls going away while
//   * interacting with activity UI.
//   */
//  View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//    @Override
//    public boolean onTouch(View view, MotionEvent motionEvent) {
//      if (AUTO_HIDE) {
//        delayedHide(AUTO_HIDE_DELAY_MILLIS);
//      }
//      return false;
//    }
//  };

//  Handler mHideHandler = new Handler();
//  Runnable mHideRunnable = new Runnable() {
//    @Override
//    public void run() {
//      mSystemUiHider.hide();
//    }
//  };

//  /**
//   * Schedules a call to hide() in [delay] milliseconds, canceling any
//   * previously scheduled calls.
//   */
//  private void delayedHide(int delayMillis) {
//    mHideHandler.removeCallbacks(mHideRunnable);
//    mHideHandler.postDelayed(mHideRunnable, delayMillis);
//  }
}
