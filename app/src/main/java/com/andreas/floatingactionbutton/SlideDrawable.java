package com.andreas.floatingactionbutton;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;

public class SlideDrawable extends LayerDrawable implements Drawable.Callback {

    public static final int MODE_HORIZONTAL = 0;
    public static final int MODE_VERTICAL = 1;
    private static final int TRANSITION_STARTING = 0;
    private static final int TRANSITION_RUNNING = 1;
    private static final int TRANSITION_NONE = 2;

    private boolean mReverse;
    private long mStartTimeMillis;
    private int mTransitionState = TRANSITION_NONE;
    private float mFrom;
    private float mTo;
    private float mFraction;
    private int mDuration;
    private int mOriginalDuration;
    private int mAnimationMode;
    private boolean mMirror;
    private Drawable[] mLayers;

    public SlideDrawable(Drawable[] layers, int mode, boolean mirror) {

        super(layers);
        mLayers = layers;
        mAnimationMode = mode;
        mMirror = mirror;
    }

    public void startTransition(int durationMillis) {

        mFrom = 0;
        mTo = 1;
        mFraction = mFrom;
        mDuration = mOriginalDuration = durationMillis;
        mReverse = false;
        mTransitionState = TRANSITION_STARTING;
        invalidateSelf();
    }

    public void resetTransition() {

        mFraction = 0;
        mTransitionState = TRANSITION_NONE;
        invalidateSelf();
    }

    public void reverseTransition(int durationMillis) {

        final long time = SystemClock.uptimeMillis();
        // Animation is over
        if (time - mStartTimeMillis > mDuration) {
            if (mTo == 0) {
                mFrom = 0;
                mTo = 1;
                mFraction = mFrom;
                mReverse = false;
            } else {
                mFrom = 1;
                mTo = 0;
                mFraction = mFrom;
                mReverse = true;
            }
            mDuration = mOriginalDuration = durationMillis;
            mTransitionState = TRANSITION_STARTING;
            invalidateSelf();
            return;
        }

        mReverse = !mReverse;
        mFrom = mFraction;
        mTo = mReverse ? 0 : 1;
        mDuration = (int) (mReverse ? time - mStartTimeMillis : mOriginalDuration - (time - mStartTimeMillis));
        mTransitionState = TRANSITION_STARTING;
    }

    @Override
    public void draw(Canvas canvas) {

        boolean done = true;

        switch (mTransitionState) {
            case TRANSITION_STARTING:
                mStartTimeMillis = SystemClock.uptimeMillis();
                done = false;
                mTransitionState = TRANSITION_RUNNING;
                break;
            case TRANSITION_RUNNING:
                if (mStartTimeMillis >= 0) {
                    float normalized = (float) (SystemClock.uptimeMillis() - mStartTimeMillis) / mDuration;
                    done = normalized >= 1.0f;
                    normalized = Math.min(normalized, 1.0f);
                    mFraction = mFrom + (mTo - mFrom) * normalized;
                }
                break;
        }

        if (done) {
            if (Math.round(mFraction) == 1) {
                // Draw only topmost layer
                mLayers[mLayers.length - 1].draw(canvas);
            }
            if (Math.round(mFraction) == 0) {
                // Draw everything but topmost layer
                for (int index = 0; index < mLayers.length - 1; index++) {
                    mLayers[index].draw(canvas);
                }
            }
            return;
        }

        // Draw everything but topmost layer
        for (int index = 0; index < mLayers.length - 1; index++) {
            mLayers[index].draw(canvas);
        }

        Drawable d = mLayers[mLayers.length - 1];
        Rect bounds = getBounds();
        if(mAnimationMode == MODE_HORIZONTAL) {
            float width = bounds.width() * mFraction;
            float left = mMirror ? bounds.width() - width : 0;
            float right = mMirror ? bounds.width() : width;
            canvas.save();
            canvas.clipRect(left, 0, right, bounds.bottom);
            d.draw(canvas);
            canvas.restore();
        } else {
            float height = bounds.height() * mFraction;
            float top = mMirror ? bounds.height() - height : 0;
            float bottom = mMirror ? bounds.height() : height;
            canvas.save();
            canvas.clipRect(0, top, bounds.right, bottom);
            d.draw(canvas);
            canvas.restore();
        }

        invalidateSelf();
    }
}
