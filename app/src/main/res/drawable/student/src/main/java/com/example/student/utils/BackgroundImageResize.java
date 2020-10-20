package com.example.student.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.Toast;

public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

    private static final int REQUEST_CODE = 1234;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;

    Bitmap mBitmap;
    Context mContext;
    ProgressBar mProgressBar;

    public BackgroundImageResize(Bitmap bm,Context context) {
        if(bm != null){
            mBitmap = bm;
            mContext = context;
        }
        Toast.makeText(context, "compressing image", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected byte[] doInBackground(Uri... uris) {
        return new byte[0];
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        super.onPostExecute(bytes);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }



}
