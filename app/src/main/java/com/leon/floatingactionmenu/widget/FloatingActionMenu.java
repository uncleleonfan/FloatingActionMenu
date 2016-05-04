package com.leon.floatingactionmenu.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
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
public class FloatingActionMenu extends FrameLayout implements View.OnClickListener {

    private static final String TAG = "FloatingActionMenu";

    private boolean mExpanding = false;
    private boolean mCollapsing = false;

    private static final int DEFAULT_ANIMATION_DURATION = 200;
    private static final int DEFAULT_BUTTON_GAP = 50;

    private FloatingActionButton mPrimaryButton;
    private View mCover;
    private OvershootInterpolator mOvershootInterpolator;
    private AccelerateInterpolator mAccelerateInterpolator;

    private int mExpandChildRightMargin = -1;
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

    /**
     * 初始化FloatingActionMenu展开后的背景
     */
    private void initCover() {
        mCover = new View(getContext());//创建背景View
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        mCover.setLayoutParams(layoutParams);
        mCover.setBackgroundColor(Color.WHITE);//设置背景颜色为白色
        mCover.setOnClickListener(mOnCoverClickListener);//设置监听，当用户点击背景时, 让FloatingActionMenu收缩
        mCover.setAlpha(0f);//初始化时是全透明
    }

    private OnClickListener mOnCoverClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggle();//当点击背景时，收缩FloatingActionMenu
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

    /**
     * 收缩FloatingActionMenu
     */
    private void collapse() {
        //将FloatingActionMenu的布局参数设回原来的wrap_content.
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        //触发重新布局
        requestLayout();

        //更新展开收缩标记
        mExpanding = false;
        mCollapsing = true;
    }

    /**
     * 展开FloatingAcitonMenu
     */
    private void expand() {
        //在xml里面的布局参数，FloatingActionMenu的宽高是wrap_content,要想展开FloatingActionMenu的话，则必须调整它的宽高，
        //让他们match_parent
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

        //获取主FloatingActionButton(带“+”号的)的布局参数
        LayoutParams primaryLayoutParams = (LayoutParams) mPrimaryButton.getLayoutParams();
        //遍历所有的FloatingActionButton
        for (int i = 0; i < getChildCount(); i ++) {
            //由于所有的FloatingActionButton在xml里面的layout_gravity都是center，所以当FloatingActionMenu的
            //布局变成match_parent的时候，所有的FloatingActionButton都居中显示，为了让它们保持在屏幕右下角，所以
            //设置所有的FloatingActionButton的gravity为FloatingActionMenu的gravity,即"bottom|end"
            LayoutParams childLayoutParams = (LayoutParams) getChildAt(i).getLayoutParams();
            childLayoutParams.gravity = layoutParams.gravity;

            //遍历所有小的FloatingActionButton, 设置它们的rightMargin和bottomMargin，让它们摆在主FloatingActionButton的正中心
            if (i != getChildCount() -1) {
                if (mExpandChildRightMargin < 0) {
                    mExpandChildRightMargin = primaryLayoutParams.rightMargin
                            + mPrimaryButton.getWidth() /2 - getChildAt(i).getWidth() /2;
                }
                childLayoutParams.rightMargin = mExpandChildRightMargin;
                if (mExpandChildBottomMargin < 0) {
                    mExpandChildBottomMargin = primaryLayoutParams.bottomMargin
                            + mPrimaryButton.getHeight() /2 - getChildAt(i).getHeight() /2;
                }
                childLayoutParams.bottomMargin = mExpandChildBottomMargin;
            }
        }

        //requestLayout();触发重新布局
        addView(mCover, 0);

        //更新展开和收缩标记
        mExpanding = true;
        mCollapsing = false;
    }

    /**
     * 在collapse()和expand()会触发FloatingActionMenu的重新布局，继而会回调onLayout方法
     *
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //当触发重新布局是由于要展开FloatingActionMenu时
        if (mExpanding) {
            //背景透明度动画
            mCover.animate().alpha(0.7f).setDuration(DEFAULT_ANIMATION_DURATION).setListener(mExpandAlphaAnimationListener).start();
            int totalFabHeight = 0;//记录弹出FloatingActionButton的本身高度的和(不包括间隔)
            //弹出所有小的FloatingActionButton
            for (int i = 1; i < getChildCount() - 1; i ++ ) {
                FloatingActionButton fab = (FloatingActionButton) getChildAt(i);
                fab.setVisibility(VISIBLE);
                //计算出对应i位置FloatingActionButton最后的位置
                //DEFAULT_BUTTON_GAP是默认FloatingActionButton之间的间隔，这个值可以通过自定义属性来配置
                int finalY = - getChildAt(i).getHeight()
                        - (mPrimaryButton.getHeight() / 2 - getChildAt(i).getHeight() / 2)
                        - DEFAULT_BUTTON_GAP * i - totalFabHeight;
                //属性动画弹出FloatingActionButton,使用OvershootInterpolator插值器
                fab.animate().translationY(finalY)
                        .setInterpolator(mOvershootInterpolator)
                        .setDuration(DEFAULT_ANIMATION_DURATION)
                        .start();
                //更新所有弹出的FloatingActionButton的本身高度的和
                totalFabHeight = getChildAt(i).getHeight() + totalFabHeight;
            }

            //属性动画，旋转主FloatingActionButton, 也是使用的OvershootInterpolator插值器
            ViewCompat.animate(mPrimaryButton).rotation(135f)
                    .withLayer().setInterpolator(mOvershootInterpolator)
                    .setDuration(DEFAULT_ANIMATION_DURATION).start();

            return;
        }
        //当触发重新布局是由于要收缩FloatingActionMenu时
        if (mCollapsing) {
            //给背景添加属性动画，让背景变透明
            mCover.animate().alpha(0).setDuration(DEFAULT_ANIMATION_DURATION).setListener(mCollapseAlphaAnimationListener).start();
            //遍历所有的小的FloatingActionButton,不包括位置为0的背景和主FloatingActionButton
            for (int i = 1;  i < getChildCount() - 1; i ++) {
                //通过属性动画让小的FloatingActionButton回到原来的位置，使用加速插值器AccelerateInterpolator
                FloatingActionButton fab = (FloatingActionButton) getChildAt(i);
                fab.animate().translationY(0)
                        .setInterpolator(mAccelerateInterpolator)
                        .setDuration(DEFAULT_ANIMATION_DURATION)
                        .start();
            }
            //让主FloatingActionButton旋转到初始位置，使用OvershootInterpolator插值器
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
            mCollapsing = false;
            mExpanding = false;
            removeView(mCover);//移除背景View

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

    //创建Behavior继承自CoordinatorLayout.Behavior
    public static class Behavior extends CoordinatorLayout.Behavior<FloatingActionMenu> {

        private boolean mScrollDown = false;

        /**
         *
         * @param parent 即CoordinatorLayout
         * @param child 即FloatingActionMenu
         * @param dependency FloatingActionMenu想要依赖的View,即FloatingActionMenu想要获取这个view的滚动事件
         *
         * @return true，表示依赖View dependency
         */
        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionMenu child, View dependency) {
            //如果依赖的view是一个RecyclerView，则返回true,表示想要监听RecyclerView的滚动事件
            return dependency instanceof RecyclerView;
        }

        /**
         * 由于在上一个函数layoutDependsOn里面我们要监听RecyclerView，当RecyclerView开始滚动的时候，会回调
         * onStartNestedScroll(), 返回true表示确认继续接受滚动事件
         */
        @Override
        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionMenu child,
                                           View directTargetChild, View target, int nestedScrollAxes) {
            //当RecyclerView是竖直方向发生滚动时，返回true，表示只接受竖直方向的滚动事件
            return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
        }

        /**
         * 当上一函数onStartNestedScroll返回true时，RecyclerView发生滚动会继续回调onNestedScroll，这里我们
         * 根据RecyclerView的滚动来操纵FloatingActionMenu的滚动
         */
        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionMenu child,
                                   View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                                   int dyUnconsumed) {
            //记录RecyclerView是否向下滚动，dyConsumed表示RecyclerView滚动的距离，dyConsumed>0表示向下滚动
            mScrollDown = dyConsumed > 0;

            //计算FloatingActionMenu在Y轴上的偏移量
            float translationY = child.getTranslationY() + dyConsumed;
            //把FloatingActionMenu的偏移量控制在0到刚好滚出屏幕的距离之间
            if (0 < translationY && translationY < child.getMaxTranslationY()) {
                //设置FloatingActionMenu在Y轴上的偏移量
                child.setTranslationY(translationY);
            }
        }

        /**
         * 当RecyclerView停止滚动时的回调，当RecyclerView停止滚动时，FloatingActionMenu要么恢复到初始的位置，要么滚动屏幕
         */
        @Override
        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionMenu child, View target) {
            //当RecyclerView是向下滚动后停止
            if (mScrollDown) {
                //让FloatingActionMenu恰好滚出屏幕
                ViewCompat.animate(child).translationY(child.getMaxTranslationY())
                        .setInterpolator(new LinearInterpolator()).start();
            } else {
                //当RecyclerView是向上滚动后停止，让FloatingActionMenu恢复到初始位置
                ViewCompat.animate(child).translationY(0)
                        .setInterpolator(new LinearInterpolator()).start();
            }
        }
    }

    /**
     * 获取FloatingActionMenu滚出屏幕需要的最大的Y轴上的偏移量
     */
    private int getMaxTranslationY() {
        if (mTranslationY < 0) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) getLayoutParams();
            //最大的偏移量为FloatingActionMenu的底部的margin加上它本身的高度
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
