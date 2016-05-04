# 自定义控件FloatingActionMenu #
在谷歌 Material Design support 包里面有一个控件叫作FloatingActionButton, 它通常停浮在屏幕右下角，能够直接的响应用户的动作，非常的实用与美观。

![](floatingactionbutton.jpg)

但是如果你想使用多个FloatingActionButton,比如你想实现类似知乎的效果：

![](zhihu.jpg)

那你需要在布局文件中添加三个FloatingActionButton, 两个小的一个大的，并且还要处理每个FloatingActionButton的动画和对应RecyclerView的滚动（隐藏或者显示），这些代码如果放在Activity里面会让Activity显得臃肿，缺乏复用性，最好的办法就是自定义一个ViewGroup,专门管理多个FloatingActionButton,在这样的背景下，FloatingActionMenu就应运而生。

* 创建自定义控件FloatingActionMenu

	FloatingActionMenu继承FrameLayout,是为了让多个FloatingActionButton能够叠加的放置在一起。
	
	```java
	public class FloatingActionMenu extends FrameLayout implements View.OnClickListener {
	
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
	```
	
* 在布局文件中使用FloatingActionMenu
	这里要注意的是用FloatingActionMenu包裹的FloatingActionButton里最后一个是默认显示出来的，就是我们在界面上看到的带有"+"的FloatingActionButton, 其他两个都被它盖在下面。
	```xml
	<?xml version="1.0" encoding="utf-8"?>
	<android.support.design.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
	    xmlns:app="http://schemas.android.com/apk/res-auto"
	    xmlns:tools="http://schemas.android.com/tools"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"
	    android:fitsSystemWindows="true"
	    tools:context="com.leon.floatingactionmenu.MainActivity">
	
	    <android.support.design.widget.AppBarLayout
	        android:layout_width="match_parent"
	        android:layout_height="wrap_content"
	        android:theme="@style/AppTheme.AppBarOverlay">
	
	        <android.support.v7.widget.Toolbar
	            android:id="@+id/toolbar"
	            android:layout_width="match_parent"
	            android:layout_height="?attr/actionBarSize"
	            android:background="?attr/colorPrimary"
	            app:popupTheme="@style/AppTheme.PopupOverlay" />
	
	    </android.support.design.widget.AppBarLayout>
	
	    <android.support.v7.widget.RecyclerView
	        android:id="@+id/recycler_view"
	        android:layout_width="match_parent"
	        android:layout_height="match_parent"
	        android:layout_marginTop="?actionBarSize">
	    </android.support.v7.widget.RecyclerView>
	    <com.leon.floatingactionmenu.widget.FloatingActionMenu
	        android:id="@+id/fab_menu"
	        android:layout_width="wrap_content"
	        android:layout_height="wrap_content"
	        android:layout_gravity="bottom|end"
	        app:layout_behavior="com.leon.floatingactionmenu.behavior.ScrollBehavior">
	
	        <android.support.design.widget.FloatingActionButton
	            android:id="@+id/fab_camera"
	            android:layout_width="wrap_content"
	            android:layout_height="wrap_content"
	            android:src="@android:drawable/ic_menu_camera"
	            android:layout_gravity="center"
	            app:backgroundTint="@android:color/white"
	            app:fabSize="mini" />
	
	        <android.support.design.widget.FloatingActionButton
	            android:id="@+id/fab_gallery"
	            android:layout_width="wrap_content"
	            android:layout_height="wrap_content"
	            android:layout_gravity="center"
	            android:src="@android:drawable/ic_menu_edit"
	            app:backgroundTint="@android:color/white"
	            app:fabSize="mini" />
	
	        <android.support.design.widget.FloatingActionButton
	            android:id="@+id/fab_add"
	            android:layout_width="wrap_content"
	            android:layout_height="wrap_content"
	            android:layout_gravity="center"
	            android:layout_margin="@dimen/fab_margin"
	            android:src="@drawable/ic_add_white"
	            app:backgroundTint="@color/colorPrimary" />
	    </com.leon.floatingactionmenu.widget.FloatingActionMenu>
	</android.support.design.widget.CoordinatorLayout>
	```


* 点击大的FloatingActionButton,弹出小的FloatingActionButton
	1. 设置带"+"的FloatingActionButton的点击事件

		```java
			@Override
		    protected void onAttachedToWindow() {
		        super.onAttachedToWindow();
				//当View添加到window后初始化点击事件
		        initEvent();
		    }

		    private void initEvent() {
				//FloatingActionMenu里面最后一个FloatingActionButton即为带“+”号的
		        mPrimaryButton = (FloatingActionButton) getChildAt(getChildCount() - 1);
				//设置点击监听
		        mPrimaryButton.setOnClickListener(mPrimaryOnClickListener);
		    }

		    private OnClickListener mPrimaryOnClickListener = new OnClickListener() {
		        @Override
		        public void onClick(View v) {
					//弹出或者收回小的FloatingActionButton
		            toggle();
		        }
		    };

		    private void toggle() {
		        if (mExpanding) {//用变量mExpanding记录当前FloatingActionMenu的状态，展开或者收缩
					//收缩，将弹出的FloatingActionButton收回来
		            collapse();
		        } else {
					//展开，将隐藏的小的FloatingActionButton弹出去
		            expand();
		        }
		    }
			
		```
	2. 调整布局

		```java
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
		
		        requestLayout();//触发重新布局
		
		        //更新展开和收缩标记
		        mExpanding = true;
		        mCollapsing = false;
		    }
		```
	3. 动画弹出FloatingActionButton

	   ```java
	    /**
	     * 在collapse()和expand()会触发FloatingActionMenu的重新布局，继而会回调onLayout方法
	     *
	     */
	    @Override
	    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
	        super.onLayout(changed, left, top, right, bottom);
	        //当触发回调是由于要展开FloatingActionMenu时
	        if (mExpanding) {
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
	    }
	   ```
* FloatingActionMenu展开时添加背景

	1. 初始化背景

		```java
		//构造函数里面添加初始化函数init()
			public FloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
		        super(context, attrs, defStyleAttr);
		        init();
		    }
			
			//初始化插值器和调用initCover初始化背景
			private void init() {
		        mOvershootInterpolator = new OvershootInterpolator();
		        mAccelerateInterpolator = new AccelerateInterpolator();
		        initCover();
		    }
		
		    // 初始化FloatingActionMenu展开后的背景
		    private void initCover() {
		        mCover = new View(getContext());//创建背景View
		        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		        layoutParams.gravity = Gravity.CENTER;
		        mCover.setLayoutParams(layoutParams);
		        mCover.setBackgroundColor(Color.WHITE);//设置背景颜色为白色
		        mCover.setOnClickListener(mOnCoverClickListener);//设置监听，当用户点击背景时, 让FloatingActionMenu收缩
		        mCover.setAlpha(0f);//初始化时是全透明
		    }
		```

	2. 添加背景到布局

		在expand()函数里面，通过addView(mCover, 0);将背景View作为FloatingActionMenu的第一个孩子孩子添加进去，这时候也会触发FloatingActionMenu的重新布局，可以将requestLayout的方法注释掉

	3. 背景动画
	
		在onLayout方法里面，当是展开的情况时，给背景添加一个属性动画
		```java
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
	        super.onLayout(changed, left, top, right, bottom);
	        //当触发回调是由于要展开FloatingActionMenu时
	        if (mExpanding) {
	            //背景透明度动画, 从0到0.7f的变化
	            mCover.animate().alpha(0.7f).setDuration(DEFAULT_ANIMATION_DURATION).setListener(mExpandAlphaAnimationListener).start();
	        }
	```

* 点击背景，收缩展开的FloatingActionMenu
	
	在初始化背景View的时候，我们给它设置了点击监听，当点击的背景的时候，会触发toggle函数的调用，由于此时mExpanding是为true，也即展开的状态，那么在toggle()
	函数里面会走collapse()。

	```java
    private OnClickListener mOnCoverClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggle();//当点击背景时，收缩FloatingActionMenu
        }
    };

    // 收缩FloatingActionMenu
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

	//collapse()函数里触发FloatingActionMenu的重新布局，继而回调onLayout，在onLayout的方法里判断如果这个重新布局是收缩则做相应的处理
	@Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
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
	
	```

* 移除背景View
	
	我们在收缩FloatingActionMenu是给背景添加了动画监听器mCollapseAlphaAnimationListener，当动画播放结束时，我们应该从布局中移除掉

	```java
    private Animator.AnimatorListener mCollapseAlphaAnimationListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
			//重置标志，收缩和展开都需要设为初始状态
            mCollapsing = false;
            mExpanding = false;
            removeView(mCover);//移除背景View
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

	```

* 对RecyclerView滚动的支持

	1. 创建Behavior
		
		FloatingActionMenu里面放置在CoordinateLayout里面，CoordinateLayout能够协调子View的滚动，当一个子View滚动时，可以将这个View的滚动
		事件告诉给其他的子View。 如果我们要实现根据CoordinateLayout里面的RecyclerView的滚动来控制FloatingActionMenu的滚动该怎么做呢？
		我们需要去实现一个Behavior：
		```java
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
		```

	2. 使用Behavior
	
		要使用Behavior,我们需要再去创建一个类继承我们创建的Behavior的类：
	
		```java
			public class ScrollBehavior extends FloatingActionMenu.Behavior {
			
			    public ScrollBehavior(Context context, AttributeSet attributeSet) {
			        super();
			    }
			}
	
		```
	
		并在布局中使用它
		
		```java
			    <com.leon.floatingactionmenu.widget.FloatingActionMenu
		        android:id="@+id/fab_menu"
		        android:layout_width="wrap_content"
		        android:layout_height="wrap_content"
		        android:layout_gravity="bottom|end"
		        app:layout_behavior="com.leon.floatingactionmenu.behavior.ScrollBehavior">
		```

* FloatingActionMenu点击的回调
	当用户点击某个FloatingActionButton时，FloatingActionMenu可以告诉外界用户点击了哪个FloatingActionButton

	1. 创建接口回调

		```java
	    public interface OnMenuItemClickListener {
	        void onMenuItemClick(FloatingActionButton fab);
	    }		
		```
	2. 设置接口对象

	    ```java
	    public void setOnMenuItemClickListener(OnMenuItemClickListener l) {
	        mOnMenuItemClickListener = l;
	    }		
		```
	3. 在点击FloatingActionButton时调用回调接口函数

	    初始化事件的时候，给每个FloatingAcitonButton设置监听为FloatingActionMenu本身
		```java
	    private void initEvent() {
	        mPrimaryButton = (FloatingActionButton) getChildAt(getChildCount() - 1);
	        mPrimaryButton.setOnClickListener(mPrimaryOnClickListener);
	
	        for (int i = 0; i < getChildCount() - 1; i ++) {
	            View fab =  getChildAt(i);
	            fab.setOnClickListener(this);
	        }
	    }
		```
		FloatingActionMenu实现View.OnClickListener的OnClick函数
		```java
		@Override
	    public void onClick(View v) {
	        toggle();
	        if (mOnMenuItemClickListener != null) {
				//调用回调函数，告诉外界哪个FloatingActionButton被点击了
	            mOnMenuItemClickListener.onMenuItemClick((FloatingActionButton) v);
	        }
	    }
		```
		