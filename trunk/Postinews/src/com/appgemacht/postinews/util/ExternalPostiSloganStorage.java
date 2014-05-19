package com.appgemacht.postinews.util;

import android.os.Environment;

public class ExternalPostiSloganStorage {
  
  public boolean isWritable() {
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      return true;
    }
    return false;
  }

}
