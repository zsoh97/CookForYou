package com.example.cookforyou.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;

/**
 * Background thread class to start up a thumbnail downloader in a background thread.
 *
 * <p>
 *     This class helps to download thumbnails with the intent that it uses
 *     the background thread, HandlerThread to download it in the background
 *     instead of freezing up the main UI thread.
 * </p>
 * @param <T> The target of the downloader
 */
public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";

    private boolean mHasQuit = false;

    public ThumbnailDownloader() {
        super(TAG);
    }

    /**
     * Cancels the current message.
     *
     * @return True if it has quit, false otherwise.
     */
    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    /**
     * Attempts to download the thumbnail url.
     *
     * <p>
     *     This method attempts to download the url being passed in as
     *     an argument. It uses the RecipeFetcher's genereic getUrlBytes
     *     method to fetch the image byte array and is then decoded
     *     by the BitmapFactory class.
     * </p>
     * @param url The url of the thumbnail
     * @return A bitmap of the thumbnail
     */
    public Bitmap downloadThumbnail(String url) {
        if(url == null) {
            return null;
        }

        try {
            byte[] bitmapBytes = new RecipeFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "Unable to fetch thumbnail from URL: " + url, e);
            return null;
        }
    }
}
