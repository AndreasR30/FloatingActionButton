package com.andreas.library;

import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Checkable;


public class FloatingActionButton extends View implements Checkable {

    private static final float RADIUS_SHADOW_RATIO = 0.4f;
    private static final int DEFAULT_BUTTON_COLOR = 0xfff4ff81;
    private static final int DEFAULT_ICON_SIZE = 24;
    private static final float NO_RADIUS = -1f;
    private static final int[] STATE_CHECKED = {android.R.attr.state_checked};
    private static final int[] STATE_CUSTOM_0 = {R.attr.state_custom_0};
    private static final int[] STATE_CUSTOM_1 = {R.attr.state_custom_1};
    private static final int[] STATE_CUSTOM_2 = {R.attr.state_custom_2};
    private static final int[] STATE_CUSTOM_3 = {R.attr.state_custom_3};
    private static final int[] STATE_CUSTOM_4 = {R.attr.state_custom_4};

    // Measurement
    private float mRadius;
    private int mIconSize;
    private Rect mShadowRect;
    private Rect mIconRect;
    private RectF mCircleRect;
    // Drawing
    private ColorStateList mColors;
    private int mCurColor;
    private LayerDrawable mCircle;
    private Drawable mIcon;
    private Drawable mShadow;
    // Flags
    private boolean mChecked;
    private int mCustomState = -1;

    public FloatingActionButton(Context context) {

        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attributeSet) {

        this(context, attributeSet, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        mShadowRect = new Rect();
        mIconRect = new Rect();
        mCircleRect = new RectF();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, 0, 0);
        mColors = a.getColorStateList(R.styleable.FloatingActionButton_fab_color);
        mRadius = a.getDimension(R.styleable.FloatingActionButton_fab_radius, NO_RADIUS);
        mIcon = a.getDrawable(R.styleable.FloatingActionButton_fab_icon);
        mIconSize = (int) a.getDimension(R.styleable.FloatingActionButton_fab_icon_size, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_ICON_SIZE, getResources().getDisplayMetrics()));
        a.recycle();

        mCircle = (LayerDrawable) getResources().getDrawable(R.drawable.fab_bg);
        mCircle.setCallback(this);

        mCurColor = mColors == null ? DEFAULT_BUTTON_COLOR : mColors.getColorForState(getDrawableState(), mColors.getDefaultColor());

        TransitionDrawable d = (TransitionDrawable) mCircle.findDrawableByLayerId(R.id.circle_item);
        GradientDrawable d1 = (GradientDrawable) d.findDrawableByLayerId(R.id.circle_item_1);
        d1.setColor(mCurColor);

        if (isLollipopOrHigher()) {
            setOutlineProvider(new ViewOutlineProvider() {

                @Override
                public void getOutline(View view, Outline outline) {

                    outline.setOval((int) mCircleRect.left, (int) mCircleRect.top, (int) mCircleRect.right, (int) mCircleRect.bottom);
                }
            });

            float elevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            setElevation(elevation);
            setClipToOutline(true);
            setStateListAnimator(AnimatorInflater.loadStateListAnimator(context, R.animator.fab_animator));
        } else {
            mShadow = getResources().getDrawable(R.drawable.fab_shadow);
            mShadow.mutate();
            mShadow.setCallback(this);
        }

        if (mIcon != null) {
            mIcon.mutate();
            mIcon.setCallback(this);
        }

        setClickable(true);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());

        mChecked = ss.mChecked;
        mCustomState = ss.mCustomState;

        refreshDrawableState();

        mCurColor = mColors == null ? DEFAULT_BUTTON_COLOR : mColors.getColorForState(getDrawableState(), mColors.getDefaultColor());

        TransitionDrawable d = (TransitionDrawable) mCircle.findDrawableByLayerId(R.id.circle_item);
        GradientDrawable d1 = (GradientDrawable) d.findDrawableByLayerId(R.id.circle_item_1);
        d1.setColor(mCurColor);
    }

    @Override
    public Parcelable onSaveInstanceState() {

        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.mChecked = mChecked;
        ss.mCustomState = mCustomState;

        return ss;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int shadowDiameter = mRadius == NO_RADIUS ? 0 : (int) (mRadius / RADIUS_SHADOW_RATIO);
        int desiredWidth = Math.max(getSuggestedMinimumWidth(), shadowDiameter);
        int desiredHeight = Math.max(getSuggestedMinimumHeight(), shadowDiameter);
        int measuredWidth = ViewCompat.resolveSizeAndState(desiredWidth, widthMeasureSpec, 0);
        int measuredHeight = ViewCompat.resolveSizeAndState(desiredHeight, heightMeasureSpec, 0);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        int centerX = w / 2;
        int centerY = h / 2;

        float shadowRadius = (mRadius == NO_RADIUS ? Math.min(w, h) : mRadius / RADIUS_SHADOW_RATIO) / 2;
        mShadowRect.left = centerX - (int) shadowRadius;
        mShadowRect.top = centerY - (int) shadowRadius;
        mShadowRect.right = centerX + (int) shadowRadius;
        mShadowRect.bottom = centerY + (int) shadowRadius;

        float iconRadius = mIconSize / 2;
        mIconRect.left = centerX - (int) iconRadius;
        mIconRect.top = centerY - (int) iconRadius;
        mIconRect.right = centerX + (int) iconRadius;
        mIconRect.bottom = centerY + (int) iconRadius;

        float buttonRadius = shadowRadius * RADIUS_SHADOW_RATIO * 2;
        mCircleRect.left = centerX - buttonRadius;
        mCircleRect.top = centerY - buttonRadius;
        mCircleRect.right = centerX + buttonRadius;
        mCircleRect.bottom = centerY + buttonRadius;

        if (!isLollipopOrHigher()) {
            mShadow.setBounds(mShadowRect);
        }
        mCircle.setBounds((int) mCircleRect.left, (int) mCircleRect.top, (int) mCircleRect.right, (int) mCircleRect.bottom);
        if (mIcon != null) {
            mIcon.setBounds(mIconRect);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (!isLollipopOrHigher()) {
            mShadow.draw(canvas);
        }
        mCircle.draw(canvas);
        if (mIcon != null) {
            mIcon.draw(canvas);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y) {

        if (isLollipopOrHigher()) {
            mIcon.setHotspot(x, y);
            mCircle.setHotspot(x, y);
        }
        super.drawableHotspotChanged(x, y);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void jumpDrawablesToCurrentState() {

        super.jumpDrawablesToCurrentState();
        if (mIcon != null) {
            mIcon.jumpToCurrentState();
        }
        mCircle.jumpToCurrentState();
    }

    @Override
    protected void drawableStateChanged() {

        super.drawableStateChanged();
        mCircle.setState(getDrawableState());
        if (mIcon != null && mIcon.isStateful()) {
            mIcon.setState(getDrawableState());
        }
        if (mColors != null && mColors.isStateful()) {
            int nextColor = mColors.getColorForState(getDrawableState(), mColors.getDefaultColor());
            if (mCurColor != nextColor) {
                TransitionDrawable d = (TransitionDrawable) mCircle.findDrawableByLayerId(R.id.circle_item);
                d.resetTransition();
                GradientDrawable d1 = (GradientDrawable) d.findDrawableByLayerId(R.id.circle_item_1);
                GradientDrawable d2 = (GradientDrawable) d.findDrawableByLayerId(R.id.circle_item_2);
                d1.setColor(mCurColor);
                d2.setColor(nextColor);
                d.startTransition(getResources().getInteger(android.R.integer.config_mediumAnimTime));
                mCurColor = nextColor;
            }
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {

        return super.verifyDrawable(who) || (who != null && (who == mCircle || who == mShadow || who == mIcon));
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {

        final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
        if (isChecked()) {
            mergeDrawableStates(drawableState, STATE_CHECKED);
        }
        switch (mCustomState) {
            case 0:
                mergeDrawableStates(drawableState, STATE_CUSTOM_0);
                break;
            case 1:
                mergeDrawableStates(drawableState, STATE_CUSTOM_1);
                break;
            case 2:
                mergeDrawableStates(drawableState, STATE_CUSTOM_2);
                break;
            case 3:
                mergeDrawableStates(drawableState, STATE_CUSTOM_3);
                break;
            case 4:
                mergeDrawableStates(drawableState, STATE_CUSTOM_4);
                break;
            default:
                break;
        }
        return drawableState;
    }

    @Override
    public void setChecked(boolean checked) {

        if (checked == mChecked) {
            return;
        }
        mChecked = checked;
        refreshDrawableState();
    }

    @Override
    public boolean isChecked() {

        return mChecked;
    }

    @Override
    public void toggle() {

        setChecked(!mChecked);
    }

    public void setColor(ColorStateList color) {

        mColors = color;
        int nextColor = color == null ? DEFAULT_BUTTON_COLOR : color.getColorForState(getDrawableState(), color.getDefaultColor());
        if (mCurColor != nextColor) {
            TransitionDrawable d = (TransitionDrawable) mCircle.findDrawableByLayerId(R.id.circle_item);
            d.resetTransition();
            GradientDrawable d1 = (GradientDrawable) d.findDrawableByLayerId(R.id.circle_item_1);
            GradientDrawable d2 = (GradientDrawable) d.findDrawableByLayerId(R.id.circle_item_2);
            d1.setColor(mCurColor);
            d2.setColor(nextColor);
            d.startTransition(getResources().getInteger(android.R.integer.config_shortAnimTime));
            mCurColor = nextColor;
        }
    }

    public ColorStateList getColor() {

        return mColors;
    }

    public void setIcon(Drawable icon) {

        if (mIcon != icon) {
            mIcon = icon;
            mIcon.mutate();
            mIcon.invalidateSelf();
        }
    }

    public Drawable getIcon() {

        return mIcon;
    }

    public void setCustomState(int state) {

        if(mCustomState == state) {
            return;
        }
        mCustomState = state;
        refreshDrawableState();
    }

    public int getCustomState() {

        return mCustomState;
    }

    protected Rect getCircleBounds() {

        return new Rect((int) mCircleRect.left, (int) mCircleRect.top, (int) mCircleRect.right, (int) mCircleRect.bottom);
    }

    protected int getCurrentColor() {

        return mCurColor;
    }

    private boolean isLollipopOrHigher() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private static class SavedState extends BaseSavedState {

        private boolean mChecked;
        private int mCustomState;

        public SavedState(Parcelable superState) {

            super(superState);
        }

        private SavedState(Parcel in) {

            super(in);
            mChecked = in.readByte() != 0;
            mCustomState = in.readInt();
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {

            super.writeToParcel(out, flags);
            out.writeByte((byte) (mChecked ? 1 : 0));
            out.writeInt(mCustomState);
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