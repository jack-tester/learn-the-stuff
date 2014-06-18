package com.appgemacht.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SystemBroadcastReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
     Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
  }

}
