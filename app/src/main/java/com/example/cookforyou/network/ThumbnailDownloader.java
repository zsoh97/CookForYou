package com.example.cookforyou.network;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    private static final int MESSAGE_DOWNLOAD = 0;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    //TODO change to weak reference for this handler leak using a static inner class.
    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG,
                            "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
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

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a thumbnail url: " + url);

        if(url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    private void handleRequest(final T target) {
        final String url = mRequestMap.get(target);

        if(url == null) {
            return;
        }

        try {
            byte[] bitmapBytes = new RecipeFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created from thumbnail URL");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target) != url ||
                    mHasQuit) {
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Unable to fetch thumbnail from URL: " + url, e);
        }
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
    public Bitmap downloadThumbnail(T target, String url) {
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
