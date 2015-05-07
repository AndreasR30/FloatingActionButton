package com.andreas.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

public class MorphLayout extends FrameLayout {

    private static final int DURATION_MOVE = 250;
    private static final int DURATION_SCALE = 80;
    private static final int DURATION_FADE = 250;

    private FloatingActionButton mFab;

    // Positions & dimensions
    private RectF mCircleBounds;
    private Point mCircleCenter;

    // Drawing
    private Paint mCirclePaint;

    // States & Flags
    private State mState = State.NONE;

    // Others
    private OnMorphListener mListener;
    private Animator mFadeAnimator;

    public MorphLayout(Context context) {

        this(context, null);
    }

    public MorphLayout(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public MorphLayout(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        mCircleBounds = new RectF();

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.FILL);

        setWillNotDraw(false);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mState = ss.mState;
        mCirclePaint.setColor(ss.mColor);

        if (mState == State.MORPHED) {
            mFab.setVisibility(INVISIBLE);
            setVisibilityChildren(true, false);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {

        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        switch (mState) {
            case MORPHING:
                mState = State.NONE;
                break;
            case REVERTING:
                mState = State.MORPHED;
                break;
        }

        ss.mState = mState;
        ss.mColor = mCirclePaint.getColor();

        return ss;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        switch (mState) {
            case MORPHED:
                canvas.drawRect(0, 0, getWidth(), getHeight(), mCirclePaint);
                break;
            case MORPHING:
            case REVERTING:
                canvas.drawOval(mCircleBounds, mCirclePaint);
                break;
        }
    }

    @Override
    public void addView(@NonNull View child) {

        child.setVisibility(INVISIBLE);
        super.addView(child);
    }

    @Override
    public void addView(@NonNull View child, int index) {

        child.setVisibility(INVISIBLE);
        super.addView(child, index);
    }

    @Override
    public void addView(@NonNull View child, int width, int height) {

        child.setVisibility(INVISIBLE);
        super.addView(child, width, height);
    }

    @Override
    public void addView(@NonNull View child, ViewGroup.LayoutParams params) {

        child.setVisibility(INVISIBLE);
        super.addView(child, params);
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {

        child.setVisibility(INVISIBLE);
        super.addView(child, index, params);
    }

    public void morph(boolean animateChildren) {

        Point p = new Point();
        p.x = getWidth() / 2;
        p.y = getHeight() / 2;
        morph(p, animateChildren);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void morph(Point circleCenter, final boolean animateChildren) {

        if (mState != State.NONE) {
            return;
        }

        mFab.setTranslationX(0);
        mFab.setTranslationY(0);
        setVisibilityChildren(false, false);
        mFab.setVisibility(VISIBLE);
        mState = State.MORPHING;
        if(mListener != null) {
            mListener.onMorphStart();
        }
        mCirclePaint.setColor(mFab.getCurrentColor());

        if(!isHoneycombOrHigher()) {
            mState = State.MORPHED;
            mFab.setVisibility(INVISIBLE);
            invalidate();
            if(mListener != null) {
                mListener.onMorphEnd();
            }
            if(animateChildren) {
                setVisibilityChildren(true, false);
            }
            return;
        }

        mCircleCenter = circleCenter;

        int[] fabPos = new int[2];
        int[] myPos = new int[2];
        mFab.getLocationOnScreen(fabPos);
        getLocationOnScreen(myPos);

        int newFabPosX = myPos[0] + circleCenter.x - mFab.getWidth() / 2;
        int newFabPosY = myPos[1] + circleCenter.y - mFab.getHeight() / 2;
        final float offsetX = newFabPosX - fabPos[0];
        final float offsetY = newFabPosY - fabPos[1];

        Point[] corners = {new Point(0, 0), new Point(0, getWidth()), new Point(getWidth(), getHeight()), new Point(0, getHeight())};
        int diameter1 = mFab.getCircleBounds().width();
        int diameter2 = calculateDiameter(circleCenter, corners);

        final ObjectAnimator animX = ObjectAnimator.ofFloat(mFab, "translationX", offsetX);
        animX.setDuration(DURATION_MOVE);
        animX.setInterpolator(new LinearInterpolator());

        final ObjectAnimator animY = ObjectAnimator.ofFloat(mFab, "translationY", offsetY);
        animY.setDuration(DURATION_MOVE);
        animY.setInterpolator(new AccelerateInterpolator(2));

        final ObjectAnimator animDiameter = ObjectAnimator.ofInt(this, "circleDiameter", diameter1, diameter2);
        animDiameter.setDuration(DURATION_SCALE);
        animDiameter.setInterpolator(new LinearInterpolator());
        animDiameter.setStartDelay(DURATION_MOVE);
        animDiameter.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {

                mFab.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                mState = State.MORPHED;
                mFab.setTranslationX(0);
                mFab.setTranslationY(0);
                invalidate();
                if(mListener != null) {
                    mListener.onMorphEnd();
                }
                if(animateChildren) {
                    setVisibilityChildren(true, true);
                }
            }
        });

        animX.start();
        animY.start();
        animDiameter.start();
    }

    public void revert(boolean animateChildren) {

        Point p = new Point();
        p.x = getWidth() / 2;
        p.y = getHeight() / 2;
        revert(p, animateChildren);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void revert(Point circleCenter, boolean animateChildren) {

        if (mState != State.MORPHED || (mFadeAnimator != null && mFadeAnimator.isRunning())) {
            return;
        }

        mFab.setVisibility(INVISIBLE);
        mFab.setTranslationX(0);
        mFab.setTranslationY(0);

        if(!isHoneycombOrHigher()) {
            setVisibilityChildren(false, false);
            mState = State.REVERTING;
            if(mListener != null) {
                mListener.onRevertStart();
            }
            mCirclePaint.setColor(mFab.getCurrentColor());
            mFab.setVisibility(VISIBLE);
            mState = State.NONE;
            invalidate();
            if(mListener != null) {
                mListener.onRevertEnd();
            }
            return;
        }

        int additionalDelay = 0;
        if(animateChildren) {
            setVisibilityChildren(false, true);
            additionalDelay = DURATION_FADE;
        }

        mCircleCenter = circleCenter;

        int[] fabPos = new int[2];
        int[] myPos = new int[2];
        mFab.getLocationOnScreen(fabPos);
        getLocationOnScreen(myPos);

        int oldFabPosX = myPos[0] + circleCenter.x - mFab.getWidth() / 2;
        int oldFabPosY = myPos[1] + circleCenter.y - mFab.getHeight() / 2;
        final float offsetX = oldFabPosX - fabPos[0];
        final float offsetY = oldFabPosY - fabPos[1];

        mFab.setTranslationX(offsetX);
        mFab.setTranslationY(offsetY);

        Point[] corners = {new Point(0, 0), new Point(0, getWidth()), new Point(getWidth(), getHeight()), new Point(0, getHeight())};
        int diameter1 = calculateDiameter(circleCenter, corners);
        int diameter2 = mFab.getCircleBounds().width();

        mCirclePaint.setColor(mFab.getCurrentColor());

        final ObjectAnimator animDiameter = ObjectAnimator.ofInt(this, "circleDiameter", diameter1, diameter2);
        animDiameter.setDuration(DURATION_SCALE);
        animDiameter.setInterpolator(new LinearInterpolator());
        animDiameter.setStartDelay(additionalDelay);
        animDiameter.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {

                setVisibilityChildren(false, false); // Ensure that children are INVISIBLE
                mState = State.REVERTING;
                if(mListener != null) {
                    mListener.onRevertStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                mCircleBounds.set(0, 0, 0, 0);
                invalidate();
            }
        });

        final ObjectAnimator animX = ObjectAnimator.ofFloat(mFab, "translationX", 0);
        animX.setDuration(DURATION_MOVE);
        animX.setInterpolator(new LinearInterpolator());
        animX.setStartDelay(DURATION_SCALE + additionalDelay);

        final ObjectAnimator animY = ObjectAnimator.ofFloat(mFab, "translationY", 0);
        animY.setDuration(DURATION_MOVE);
        animY.setInterpolator(new DecelerateInterpolator(2));
        animY.setStartDelay(DURATION_SCALE + additionalDelay);
        animY.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {

                mFab.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                mState = State.NONE;
                if(mListener != null) {
                    mListener.onRevertEnd();
                }
            }
        });

        animDiameter.start();
        animX.start();
        animY.start();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setCircleDiameter(int d) {

        int radius = d / 2;
        mCircleBounds.left = mCircleCenter.x - radius;
        mCircleBounds.top = mCircleCenter.y - radius;
        mCircleBounds.right = mCircleCenter.x + radius;
        mCircleBounds.bottom = mCircleCenter.y + radius;
        invalidate();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setVisibilityChildren(boolean show, boolean animate) {

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(animate && isHoneycombOrHigher()) {
                child.setAlpha(show ? 0 : 1);
                child.setVisibility(VISIBLE);
                mFadeAnimator = ObjectAnimator.ofFloat(child, "alpha", show ? 1 : 0);
                mFadeAnimator.setDuration(DURATION_FADE);
                mFadeAnimator.start();
            } else {
                child.setVisibility(show ? VISIBLE : INVISIBLE);
            }
        }
    }

    private int calculateDiameter(Point center, Point[] corners) {

        int max = 0;
        for (Point corner : corners) {
            int dist = (int) Math.ceil(Math.sqrt(Math.pow(corner.x - center.x, 2) + Math.pow(corner.y - center.y, 2)));
            max = Math.max(max, dist);
        }
        return max * 2;
    }

    private boolean isHoneycombOrHigher() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public void setFab(FloatingActionButton fab) {

        mFab = fab;
    }

    public FloatingActionButton getFab() {

        return mFab;
    }

    public void setMorphListener(OnMorphListener listener) {

        mListener = listener;
    }

    public OnMorphListener getMorphListener() {

        return mListener;
    }

    public State getState() {

        return mState;
    }

    public enum State {

        MORPHED, MORPHING, REVERTING, NONE
    }

    public interface OnMorphListener {

        public void onMorphStart(); // State: Morphing, Children: INVISIBLE, Fab: VISIBLE and no translation

        public void onMorphEnd(); // State: Morphed, Children: INVISIBLE, Fab: INVISIBLE and no translation

        public void onRevertStart(); // State: Reverting, Children: INVISIBLE, Fab: INVISIBLE and no translation

        public void onRevertEnd(); // State: None, Children: INVISIBLE, Fab: VISIBLE and no translation
    }

    private static class SavedState extends BaseSavedState {

        private State mState;
        private int mColor;

        public SavedState(Parcelable superState) {

            super(superState);
        }

        private SavedState(Parcel in) {

            super(in);
            mState = State.values()[in.readInt()];
            mColor = in.readInt();
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {

            super.writeToParcel(out, flags);
            out.writeInt(mState.ordinal());
            out.writeInt(mColor);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {

                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {

                return new SavedState[size];
            }
        };
    }
}
