package com.appgemacht.postinews.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

public class ExternalPostiSloganStorage {
  
  private File postiNewsFile;
  
  private ArrayList <Integer> hashesOfStoredSlogans;
  
  private long postiNewsFileSize;
  
  /**
   * To check ability to write to external storage acc. to
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
  
  /**
   * The 'PostiNewsDir' is located as follows:
   *  /mnt/sdcard/download/
   *  
   */
  public void openFile() {
    File postiNewsDir = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS), "PostiNewsDir");
    postiNewsFile = new File(
        postiNewsDir, "PostiNews.txt");
    
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

    // read in the hashes of already stored slogans ...
    try {
      FileReader r = new FileReader(postiNewsFile);
      BufferedReader br = new BufferedReader(r);
      
      hashesOfStoredSlogans = new ArrayList<Integer>();
      
      String line;
      do {
        line = br.readLine();
        if (line == null) {
          break;
        }
        int hash = Integer.parseInt(line.substring(2,line.indexOf(",",2)));
        //Log.i("POSTI_STORAGE: ", "hash "+hash+" found.");
        hashesOfStoredSlogans.add(hash);
      } while (true);
      Log.i("POSTI_STORAGE: ", "read in "+hashesOfStoredSlogans.size()+" hashes.");
      
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  public boolean containsStringWithHash(int hash) {
    for (int h : hashesOfStoredSlogans) {
      if (h == hash) {
        Log.i("POSTI_STORAGE: ", "that slogan is already stored !");
        return true;
      }
    }
    return false;
  }
  
  
  /**
   * storage format:
   * <rating>,<hash code>,"<posti news slogan>"[,... for future use]
   * <rating>,<hash code>,"<posti news slogan>"[,... for future use]
   * - one file per rating 1..5
   * - all files will be scanned at startup to read in all the hashes
   *   -> already stored slogans will not be stored again
   * - ..
   * 
   * @param slogan ... slogan rated
   * @param rating ... rating
   */ 
  public void write(String slogan, int rating) {

    int hash = slogan.hashCode();
    
    if (this.containsStringWithHash(hash)) {
      return;
    }
    
    int maxLineLen2store = slogan.length() + 32; // 32 = 1 * [0..9] + 2 * '"' + 2 * ',' + 1 * <integer> + EOL
    StringBuilder line2store = new StringBuilder(maxLineLen2store);
    
    line2store.append(rating);
    line2store.append(",");
    line2store.append(hash);
    line2store.append(",\"" + slogan + "\"");
    
    try {
      // the append flag (2nd parameter) makes further output an addition to the existing file
      FileWriter w = new FileWriter(postiNewsFile,true);
      BufferedWriter bw = new BufferedWriter(w);
      
      bw.append(line2store);
      bw.newLine();
      bw.close();
      
      hashesOfStoredSlogans.add(hash);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  } 

}
