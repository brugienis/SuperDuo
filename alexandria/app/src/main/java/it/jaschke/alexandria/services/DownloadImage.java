/*
 * Copyright (C) 2013 The Android Open Source Project
 */

package it.jaschke.alexandria.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;

import it.jaschke.alexandria.R;

/**
 * Created by saj on 11/01/15.
 */
public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
    private ImageView mBmImage;
    private Context mContext;
    private boolean mDownloadUnsuccessful;

    private final static String LOG_TAG = DownloadImage.class.getSimpleName();

    public DownloadImage(ImageView bmImage, Context context) {
        this.mBmImage = bmImage;
        this.mContext = context;
    }

    protected Bitmap doInBackground(String... urls) {
        String urlDisplay = urls[0];
        Bitmap bookCover = null;
        try {
            InputStream in = new java.net.URL(urlDisplay).openStream();
            bookCover = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            mDownloadUnsuccessful = true;
        }
        return bookCover;
    }

    protected void onPostExecute(Bitmap result) {
        if (mDownloadUnsuccessful) {
            Toast.makeText(mContext,
                    mContext.getString(R.string.image_download_problem),
                    Toast.LENGTH_SHORT).show();
        } else {
            mBmImage.setImageBitmap(result);
        }
    }
}

