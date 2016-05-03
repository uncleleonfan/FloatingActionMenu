package com.leon.floatingactionmenu.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

/**
 * Created by Leon on 2016/4/7.
 */
public class FloatingActionMenu extends FrameLayout implements View.OnClickListener{

    private boolean mExpanding = false;
    private boolean mCollapsing = false;

    private static final int DEFAULT_ANIMATION_DURATION = 200;
    private static final int DEFAULT_BUTTON_GAP = 50;

    private static final String TAG = "FloatingActionMenu";
    private FloatingActionButton mPrimaryButton;
    private View mCover;
    private OvershootInterpolator mOvershootInterpolator;
    private AccelerateInterpolator mAccelerateInterpolator;

    private int mHorizontalGravity = -1;

    private int mExpandChildRightMargin = -1;
    private int mExpandChildLeftMargin = -1;
    private int mExpandChildBottomMargin = -1;
    private int mTranslationY = -1;

    private OnMenuItemClickListener mOnMenuItemClickListener;


    public FloatingActionMenu(Context context) {
        this(context, null);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mOvershootInterpolator = new OvershootInterpolator();
        mAccelerateInterpolator = new AccelerateInterpolator();
        initCover();

        setFocusableInTouchMode(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initEvent();
    }

    private void initEvent() {
        mPrimaryButton = (FloatingActionButton) getChildAt(getChildCount() - 1);
        mPrimaryButton.setOnClickListener(mPrimaryOnClickListener);

        for (int i = 0; i < getChildCount() - 1; i ++) {
            View fab =  getChildAt(i);
            fab.setOnClickListener(this);
        }
    }

    private void initCover() {
        mCover = new View(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        mCover.setLayoutParams(layoutParams);
        mCover.setBackgroundColor(Color.WHITE);
        mCover.setOnClickListener(mOnCoverClickListener);
        mCover.setAlpha(0f);
    }

    private OnClickListener mOnCoverClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggle();
        }
    };

    private OnClickListener mPrimaryOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggle();
        }
    };

    private void toggle() {
        Log.d(TAG, "toggle");
        if (mExpanding) {
            collapse();
        } else {
            expand();
        }
    }

    private void collapse() {
        Log.d(TAG, "collapse: ");
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        requestLayout();
        mExpanding = false;
        mCollapsing = true;
    }

    private void expand() {
        Log.d(TAG, "expand: ");
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

        LayoutParams primaryLayoutParams = (LayoutParams) mPrimaryButton.getLayoutParams();
        if (mHorizontalGravity < 0) {
            int layoutDirection = ViewCompat.getLayoutDirection(this);
            int gravity = GravityCompat.getAbsoluteGravity(layoutParams.gravity, layoutDirection);
            mHorizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        }

        for (int i = 0; i < getChildCount(); i ++) {
            LayoutParams childLayoutParams = (LayoutParams) getChildAt(i).getLayoutParams();
            childLayoutParams.gravity = layoutParams.gravity;
            if (i != getChildCount() -1) {
                switch (mHorizontalGravity) {
                    case Gravity.RIGHT:
                        if (mExpandChildRightMargin < 0) {
                            mExpandChildRightMargin = primaryLayoutParams.rightMargin
                                    + mPrimaryButton.getWidth() /2 - getChildAt(i).getWidth() /2;
                        }
                        childLayoutParams.rightMargin = mExpandChildRightMargin;
                        break;
                    case Gravity.LEFT:
                        if (mExpandChildLeftMargin < 0) {
                            mExpandChildLeftMargin = primaryLayoutParams.leftMargin
                                    + mPrimaryButton.getWidth() /2 - getChildAt(i).getWidth() /2;
                        }
                        childLayoutParams.leftMargin = mExpandChildLeftMargin;
                        break;
                }
                if (mExpandChildBottomMargin < 0) {
                    mExpandChildBottomMargin = primaryLayoutParams.bottomMargin
                            + mPrimaryButton.getHeight() /2 - getChildAt(i).getHeight() /2;
                }
                childLayoutParams.bottomMargin = mExpandChildBottomMargin;
            }
        }

        addView(mCover, 0);

        mExpanding = true;
        mCollapsing = false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mExpanding) {
            mCover.animate().alpha(0.7f).setDuration(DEFAULT_ANIMATION_DURATION).setListener(mExpandAlphaAnimationListener).start();
            int totalFabHeight = 0;
            for (int i = 1; i < getChildCount() - 1; i ++ ) {
                FloatingActionButton fab = (FloatingActionButton) getChildAt(i);
                fab.setVisibility(VISIBLE);
                int finalY = - getChildAt(i).getHeight()
                        - (mPrimaryButton.getHeight() / 2 - getChildAt(i).getHeight() / 2)
                        - DEFAULT_BUTTON_GAP * i - totalFabHeight;
                fab.animate().translationY(finalY)
                        .setInterpolator(mOvershootInterpolator)
                        .setDuration(DEFAULT_ANIMATION_DURATION)
                        .start();
                totalFabHeight = getChildAt(i).getHeight() + totalFabHeight;
            }
            ViewCompat.animate(mPrimaryButton).rotation(135f)
                    .withLayer().setInterpolator(mOvershootInterpolator)
                    .setDuration(DEFAULT_ANIMATION_DURATION).start();

            return;
        }

        if (mCollapsing) {
            mCover.animate().alpha(0).setDuration(DEFAULT_ANIMATION_DURATION).setListener(mCollapseAlphaAnimationListener).start();
            for (int i = 1;  i < getChildCount() - 1; i ++) {
                FloatingActionButton fab = (FloatingActionButton) getChildAt(i);
                fab.animate().translationY(0)
                        .setInterpolator(mAccelerateInterpolator)
                        .setDuration(DEFAULT_ANIMATION_DURATION)
                        .start();
            }
            ViewCompat.animate(mPrimaryButton).rotation(0)
                    .withLayer().setInterpolator(mOvershootInterpolator)
                    .setDuration(DEFAULT_ANIMATION_DURATION).start();

        }
    }

    private Animator.AnimatorListener mExpandAlphaAnimationListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mCover.setClickable(true);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    private Animator.AnimatorListener mCollapseAlphaAnimationListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {
            mCover.setClickable(false);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            Log.d(TAG, "onAnimationEnd: ");
            mCollapsing = false;
            mExpanding = false;
            removeView(mCover);

            for (int i = 0; i < getChildCount() - 1; i ++) {
                getChildAt(i).setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };


    public static class Behavior extends CoordinatorLayout.Behavior<FloatingActionMenu> {

        private boolean mScrollDown = false;

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionMenu child, View dependency) {
            return dependency instanceof RecyclerView;
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionMenu child,
                                           View directTargetChild, View target, int nestedScrollAxes) {
            return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionMenu child,
                                   View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                                   int dyUnconsumed) {
            Log.d(TAG, "onNestedScroll: dyConsumed " + dyConsumed);
            mScrollDown = dyConsumed > 0;
            float translationY = child.getTranslationY() + dyConsumed;
            if (0 < translationY && translationY < child.getTranslationYForRecyclerView()) {
                child.setTranslationY(child.getTranslationY() + dyConsumed);
            }
        }

        @Override
        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionMenu child, View target) {
            Log.d(TAG, "onStopNestedScroll: ");
            if (mScrollDown) {
                ViewCompat.animate(child).translationY(child.getTranslationYForRecyclerView())
                        .setInterpolator(new LinearInterpolator()).start();
            } else {
                ViewCompat.animate(child).translationY(0)
                        .setInterpolator(new LinearInterpolator()).start();
            }
        }
    }

    private int getTranslationYForRecyclerView() {
        if (mTranslationY < 0) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) getLayoutParams();
            mTranslationY = layoutParams.bottomMargin + getHeight();
        }
        return mTranslationY;
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(FloatingActionButton fab);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener l) {
        mOnMenuItemClickListener = l;
    }

    @Override
    public void onClick(View v) {
        toggle();
        if (mOnMenuItemClickListener != null) {
            mOnMenuItemClickListener.onMenuItemClick((FloatingActionButton) v);
        }
    }

}