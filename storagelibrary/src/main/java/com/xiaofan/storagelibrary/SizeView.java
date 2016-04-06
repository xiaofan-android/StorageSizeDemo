package com.xiaofan.storagelibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.xiaofan.storagelibrary.util.StorageSizeUtil;

/**
 * 显示外置存储的总空间和剩余空间的TextView
 */
public class SizeView extends TextView {

    private static final long REFRESH_SIZE_DELAY= 5 * 1000;

    private long mTotleSize;
    private long mAvailableSize;
    private HandlerThread mThread;
    private Handler mHandler;

    private boolean isRemovable = false;
    private boolean isAutoRefresh = true;

    public SizeView(Context context) {
        this(context, null);
    }

    public SizeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRemovable(boolean removable){
        this.isRemovable = removable;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        if(isAutoRefresh == autoRefresh){
            return;
        }
        this.isAutoRefresh = autoRefresh;
        if (mHandler != null) {
            mHandler.removeCallbacks(r);
            mHandler.post(r);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mThread = new HandlerThread("SizeView_Thread");
        mThread.start();
        mHandler = new Handler(mThread.getLooper());

        mHandler.post(r);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(r);
        mThread.quit();
        mHandler = null;
        mThread = null;
    }

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            initSize();
            post(new Runnable() {
                @Override
                public void run() {
                    setText(getContext().getString(R.string.storage_size_content, Formatter.formatFileSize(getContext(), mTotleSize), Formatter.formatFileSize(getContext(), mAvailableSize)));
                }
            });
            if(isAutoRefresh){
                mHandler.postDelayed(this, REFRESH_SIZE_DELAY);
            }
        }
    };

    private void initSize(){
        mTotleSize = StorageSizeUtil.getExternalStorageSizeTotleSize(getContext(), isRemovable);
        mAvailableSize = StorageSizeUtil.getExternalStorageSizeAvailableSize(getContext(), isRemovable);
    }
}
