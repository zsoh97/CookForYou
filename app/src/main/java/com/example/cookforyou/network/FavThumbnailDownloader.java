package com.example.cookforyou.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.cookforyou.FavouriteAdapter;
import com.example.cookforyou.R;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FavThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "FavThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private boolean mHasQuit = false;
    private Context mContext;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mFavResponseHandler;
    private FavThumbnailDownloader.FavThumbnailDownloadListener<T> mFavThumbnailDownloadListener;

    public interface FavThumbnailDownloadListener<T> {
        void onFavThumbnailDownloaded(T target, Bitmap thumbnail);

        void onFavThumbnailDownloaded(FavouriteAdapter.FavouriteHolder target, Bitmap thumbnail);
    }

    public void setFavThumbnailDownloadListener(FavThumbnailDownloader.FavThumbnailDownloadListener<T> listener) {
        mFavThumbnailDownloadListener = listener;
    }

    public FavThumbnailDownloader(Context context, Handler responseHandler) {
        super(TAG);
        mContext = context;
        mFavResponseHandler = responseHandler;
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
            final Bitmap bitmap;
            if(bitmapBytes == null) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.image_not_available_image);
                Log.i(TAG, "Thumbnail URL empty. Placing no image available");
            } else {
                bitmap = BitmapFactory
                        .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

                Log.i(TAG, "Bitmap created from thumbnail URL");
            }

            mFavResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target) != url ||
                            mHasQuit) {
                        return;
                    }

                    mRequestMap.remove(target);
                    mFavThumbnailDownloadListener.onFavThumbnailDownloaded(target, bitmap);
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

