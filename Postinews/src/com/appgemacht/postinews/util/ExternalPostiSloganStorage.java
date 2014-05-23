package com.appgemacht.postinews.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

public class ExternalPostiSloganStorage {
  
  private File postiNewsFile;
  
  private ArrayList <Integer> hashesOfStoredSlogans;
  
  private long postiNewsFileSize;
  
  /**
   * To check writeability of external storage acc. to
   *  http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
   * @return
   */
  public boolean isWritable() {
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      return true;
    }
    return false;
  }
  
  public void openFile() {
    
    File postiNewsDir = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS), "PostiNewsDir");
    postiNewsFile = new File(
        postiNewsDir, "PostiNews.5.txt");
    
    if (!postiNewsDir.isDirectory()) {
      if (!postiNewsDir.mkdirs()) {
        Log.e("POSTI_STORAGE: ", "Directory not created");
      }
    }
    if (!postiNewsFile.isFile()) {
      try {
        if (!postiNewsFile.createNewFile()) {
          Log.e("POSTI_STORAGE: ", "File not created");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    postiNewsFileSize = postiNewsFile.length();
    Log.i("POSTI_STORAGE: ", "File length "+postiNewsFileSize);
    
  }
  
  /**
   * storage format:
   * '<posti news slogan>',<hash code>[,... for future use]
   * '<posti news slogan>',<hash code>[,... for future use]
   * - one file per rating 1..5
   * - all files will be scanned at startup to read in all the hashes
   *   -> already stored slogans will not be stored again
   * - ..
   * 
   * @param slogan ... slogan rated
   * @param rating ... rating
   */ 
  public void writeTo(String slogan, int rating) {

    int hash = slogan.hashCode();
    
    int maxLineLen2store = slogan.length() + 16; // 16 = 2 x '"' + 1 x ',' + 1 x <integer> + EOL
    StringBuilder line2store = new StringBuilder(maxLineLen2store);
    
    line2store.append("\""+slogan+"\",");
    line2store.append(hash);
    
    try {
//      FileReader r = new FileReader(postiNewsFile);
//      BufferedReader bw = new BufferedReader(r);
//      
//      bw.
      
      // the append flag (2nd parameter) makes further output an addition to the existing file
      FileWriter w = new FileWriter(postiNewsFile,true);
      BufferedWriter bw = new BufferedWriter(w);
      
      bw.append(line2store);
      bw.newLine();
      bw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  } 

}
