package com.tienfeek.handy.view;

import android.content.Context;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public class ListHeaderView extends ViewGroup
{
  private int mHeight;
  private static final Interpolator sInterpolator = new Interpolator()
  {
    public float getInterpolation(float t) {
      t -= 1.0F;
      return t * t * t + 1.0F; }  } ;
  private static final int MAX_DURATION = 350;
  private int mDistance;
  private int mInitHeight;
  private boolean mImediateUpdate = false;
  private int mUpdateHeight;
  protected RefreshableListView mListView;
  private static final int INVALID_STATE = -1;
  private int mNextState = -1;
  RefreshableListView.OnHeaderViewChangedListener mOnHeaderViewChangedListener;
  int mUpdatingStatus = 0;
  private static final int UPDATING_IDLE = 0;
  private static final int UPDATING_READY = 1;
  private static final int UPDATING_ON_GOING = 2;
  private static final int UPDATING_FINISH = 3;
  private int mMaxPullHeight;
  private static final int MAX_PULL_HEIGHT_DP = 200;
  private OnHeaderViewOpenedListener listener;
  private Runnable mUpdateRunnable;
  private boolean mCanUpdate;

  public ListHeaderView(Context context, RefreshableListView list) { super(context);
    this.mListView = list;
    this.mMaxPullHeight = 
      (int)(context.getResources().getDisplayMetrics().density * 
      200.0F + 0.5F);
  }

  protected void onLayout(boolean changed, int l, int t, int r, int b)
  {
    View childView = getChildView();
    if (childView == null) {
      return;
    }

    int childViewWidth = childView.getMeasuredWidth();
    int childViewHeight = childView.getMeasuredHeight();
    int measuredHeight = getMeasuredHeight();
    childView.layout(0, measuredHeight - childViewHeight, childViewWidth, 
      measuredHeight);
  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
  {
    int width = View.MeasureSpec.getSize(widthMeasureSpec);
    if (this.mHeight < 0) {
      this.mHeight = 0;
    }
    setMeasuredDimension(width, this.mHeight);

    View childView = getChildView();
    if (childView != null) {
      childView.measure(widthMeasureSpec, heightMeasureSpec);
      this.mUpdateHeight = childView.getMeasuredHeight();
    }
  }

  public void startUpdate(Runnable runnable) {
    this.mUpdatingStatus = 1;
    this.mUpdateRunnable = runnable;
    this.mInitHeight = this.mHeight;
    this.mDistance = (this.mInitHeight - this.mUpdateHeight);
    if (this.mDistance < 0) {
      this.mDistance = this.mInitHeight;
    }

    int duration = this.mDistance * 3;
    duration = duration > 350 ? 350 : duration;
    Log.d("View", "duration:" + duration);
    CloseTimer timer = new CloseTimer(duration, true);
    timer.startTimer();
  }

  public void setOnHeaderViewOpendListener(OnHeaderViewOpenedListener onHeaderViewOpenedListener)
  {
    this.listener = onHeaderViewOpenedListener;
  }

  public void startUpdateImmediate()
  {
    int duration = this.mDistance * 3;
    duration = duration > 350 ? 350 : duration;
    Log.d("View", "duration:" + duration);
    this.mImediateUpdate = true;
    CloseTimer timer = new CloseTimer(duration, false);
    timer.startTimer();
  }

  public int close(int nextState)
  {
    this.mUpdatingStatus = 3;
    if (this.mOnHeaderViewChangedListener != null) {
      this.mOnHeaderViewChangedListener.onViewUpdateFinish(this);
    }
    this.mDistance = (this.mInitHeight = this.mHeight);
    int duration = this.mDistance * 4;
    duration = duration > 350 ? 350 : duration;
    this.mNextState = nextState;
    CloseTimer timer = new CloseTimer(duration, true);
    timer.startTimer();
    return duration;
  }

  public boolean isUpdateNeeded() {
    if (this.mImediateUpdate) {
      this.mImediateUpdate = false;
      return true;
    }

    int distance = this.mHeight - this.mUpdateHeight;
    boolean needUpdate = distance >= 0;
    return needUpdate;
  }

  public void moveToUpdateHeight() {
    setHeaderHeight(this.mUpdateHeight);
    this.mImediateUpdate = true;
  }

  protected View getChildView()
  {
    int childCount = getChildCount();
    if (childCount != 1) {
      return null;
    }
    return getChildAt(0);
  }

  public void addView(View child)
  {
    int childCount = getChildCount();
    if (childCount > 0) {
      throw new IllegalStateException(
        "ListHeaderView can only have one child view");
    }
    super.addView(child);
  }

  public void setHeaderHeight(int height)
  {
    if ((this.mHeight == height) && (height == 0))
    {
      return;
    }

    if (height > this.mMaxPullHeight) {
      return;
    }

    int updateHeight = this.mUpdateHeight;
    this.mHeight = height;
    float percent = 1.0F;
    if (height < updateHeight) {
      percent = height / updateHeight;
    }
    if (this.mOnHeaderViewChangedListener != null) {
      this.mOnHeaderViewChangedListener.onViewHeightChanged(percent);
    }

    if (this.mUpdatingStatus != 0) {
      if ((this.mUpdatingStatus == 1) && 
        (this.mOnHeaderViewChangedListener != null)) {
        this.mOnHeaderViewChangedListener.onViewUpdating(this);
        this.mUpdatingStatus = 2;
      }
    }
    else if ((height < updateHeight) && (this.mCanUpdate)) {
      if (this.mOnHeaderViewChangedListener != null) {
        this.mOnHeaderViewChangedListener.onViewChanged(this, false);
      }

      this.mCanUpdate = false;
    } else if ((height >= updateHeight) && (!this.mCanUpdate)) {
      if (this.mOnHeaderViewChangedListener != null) {
        this.mOnHeaderViewChangedListener.onViewChanged(this, true);
      }
      this.mCanUpdate = true;
    }

    requestLayout();

    if (height == 0) {
      this.mUpdatingStatus = 0;
      this.mCanUpdate = false;
    }
  }

  private class CloseTimer extends CountDownTimer
  {
    private long mStart;
    private float mDurationReciprocal;
    private boolean isClose;
    private static final int COUNT_DOWN_INTERVAL = 15;

    public CloseTimer(long millisInFuture, boolean isClose)
    {
      super(15L,1000);
      this.isClose = isClose;
      this.mDurationReciprocal = (1.0F / (float)millisInFuture);
    }

    public void startTimer() {
      this.mStart = AnimationUtils.currentAnimationTimeMillis();
      start();
    }

    public void onFinish()
    {
      float x = 1.0F;
      if (ListHeaderView.this.mNextState != -1) {
        ListHeaderView.this.mListView.setState(ListHeaderView.this.mNextState);
        ListHeaderView.this.mNextState = -1;
      }
      ListHeaderView.this.setHeaderHeight((int)(ListHeaderView.this.mInitHeight - ListHeaderView.this.mDistance * x));
      if (ListHeaderView.this.mUpdateRunnable != null) {
        Runnable runnable = ListHeaderView.this.mUpdateRunnable;
        new Thread(runnable).start();
        ListHeaderView.this.mUpdateRunnable = null;
      }
      if (!this.isClose) {
        ListHeaderView.this.moveToUpdateHeight();
        if (ListHeaderView.this.mOnHeaderViewChangedListener != null) {
          ListHeaderView.this.mOnHeaderViewChangedListener.onViewUpdating(ListHeaderView.this);
        }
        if (ListHeaderView.this.listener != null)
          ListHeaderView.this.listener.onHeaderViewOpened();
      }
    }

    public void onTick(long millisUntilFinished)
    {
      int interval = (int)(
        AnimationUtils.currentAnimationTimeMillis() - this.mStart);
      float x = interval * this.mDurationReciprocal;
      x = ListHeaderView.sInterpolator.getInterpolation(x);
      if (this.isClose) {
        ListHeaderView.this.setHeaderHeight((int)(ListHeaderView.this.mInitHeight - ListHeaderView.this.mDistance * x));
      }
      else
        ListHeaderView.this.setHeaderHeight((int)(ListHeaderView.this.mInitHeight * x));
    }
  }

  public static abstract interface OnHeaderViewOpenedListener
  {
    public abstract void onHeaderViewOpened();
  }
}