package com.jikexueyuan.jike_chat.util;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * to deal with the path of the voice messages
 * 
 *
 *
 */
public final class FileUtils {
    /**using this to help MyVoiceMessageItemProvider to get the file
     * turn uri into an absolute path
     */
    public static String uri2File(Activity aty, Uri uri) {
        if (android.os.Build.VERSION.SDK_INT < 11) {
            
            String[] proj = { MediaStore.Images.Media.DATA };
            @SuppressWarnings("deprecation")
            Cursor actualimagecursor = aty.managedQuery(uri, proj, null, null,
                    null);
            int actual_image_column_index = actualimagecursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            String file_path = actualimagecursor
                    .getString(actual_image_column_index);
            return file_path;
        } else {
            
            String res = null;
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = aty.getContentResolver().query(uri, proj, null, null, null);
            if(cursor != null){
                if(cursor.moveToFirst()){;
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    res = cursor.getString(column_index);
                }
                cursor.close();
            }
             else
            {
                res = uri.getPath();
            }

            return res;
        }
    }

}
