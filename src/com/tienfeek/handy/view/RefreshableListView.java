package com.tienfeek.handy.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class RefreshableListView extends ListView
{
  private static final int STATE_NORMAL = 0;
  private static final int STATE_READY = 1;
  private static final int STATE_PULL = 2;
  private static final int STATE_UPDATING = 3;
  private static final int INVALID_POINTER_ID = -1;
  private static final int UP_STATE_READY = 4;
  private static final int UP_STATE_PULL = 5;
  private static final int MIN_UPDATE_TIME = 500;
  protected ListHeaderView mListHeaderView;
  protected ListBottomView mListBottomView;
  private int mActivePointerId;
  private float mLastY;
  private int mState;
  private boolean mPullUpRefreshEnabled = false;
  private OnUpdateTask mOnUpdateTask;
  private OnPullUpUpdateTask mOnPullUpUpdateTask;
  private int mTouchSlop;
  private BaseAdapter mAutoNotifyAdapter;

  public RefreshableListView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    initialize();
  }

  public RefreshableListView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initialize();
  }

  public void setAutoNotifyAdapter(BaseAdapter adapter)
  {
    this.mAutoNotifyAdapter = adapter;
  }

  public void setAdapter(ListAdapter adapter)
  {
    super.setAdapter(adapter);
    if ((adapter instanceof BaseAdapter))
      setAutoNotifyAdapter((BaseAdapter)adapter);
  }

  public void notifyDataSetChanged()
  {
    this.mAutoNotifyAdapter.notifyDataSetChanged();
  }

  public void setTopContentView(int id)
  {
    View view = LayoutInflater.from(getContext()).inflate(id, 
      this.mListHeaderView, false);
    this.mListHeaderView.removeAllViews();
    this.mListHeaderView.addView(view);
  }

  public void setBottomEnabled(boolean enabled)
  {
    this.mPullUpRefreshEnabled = enabled;
  }

  public void setBottomContentView(int id)
  {
    this.mPullUpRefreshEnabled = true;
    View view = LayoutInflater.from(getContext()).inflate(id, 
      this.mListBottomView, false);
    this.mListBottomView.removeAllViews();
    this.mListBottomView.addView(view);
    addFooterView(this.mListBottomView, null, false);
  }

  public void setBottomContentView(View v) {
    this.mListBottomView.addView(v);
  }

  public void setContentView(View v) {
    this.mListHeaderView.addView(v);
  }

  public ListHeaderView getListHeaderView() {
    return this.mListHeaderView;
  }

  public void setOnUpdateTask(OnUpdateTask task)
  {
    this.mOnUpdateTask = task;
  }

  public void setOnPullUpUpdateTask(OnPullUpUpdateTask task) {
    this.mOnPullUpUpdateTask = task;
  }

  public void startUpdateImmediate()
  {
    if (this.mState == 3) {
      return;
    }
    setSelectionFromTop(0, 0);

    this.mListHeaderView.close(3);
    this.mListHeaderView.moveToUpdateHeight();
    update();
  }

  public void setOnHeaderViewChangedListener(OnHeaderViewChangedListener listener)
  {
    this.mListHeaderView.mOnHeaderViewChangedListener = listener;
  }

  public void setOnBottomViewChangedListener(OnBottomViewChangedListener listener)
  {
    this.mListBottomView.mOnHeaderViewChangedListener = listener;
  }

  private void initialize() {
    Context context = getContext();
    this.mListHeaderView = new ListHeaderView(context, this);
    addHeaderView(this.mListHeaderView, null, false);
    this.mListBottomView = new ListBottomView(getContext(), this);
    this.mState = 0;
    ViewConfiguration configuration = ViewConfiguration.get(context);
    this.mTouchSlop = configuration.getScaledTouchSlop();

    this.mListHeaderView.setOnHeaderViewOpendListener(new ListHeaderView.OnHeaderViewOpenedListener()
    {
      public void onHeaderViewOpened()
      {
        if (RefreshableListView.this.mListHeaderView.getChildCount() > 0)
          RefreshableListView.this.update();
      }
    });
  }

  private void pullUpUpdate() {
    if (this.mListBottomView.isUpdateNeeded()) {
      if (this.mOnPullUpUpdateTask != null) {
        this.mOnPullUpUpdateTask.onUpdateStart();
      }

      this.mListBottomView.startUpdate(new Runnable() {
        public void run() {
          long b = System.currentTimeMillis();
          if (RefreshableListView.this.mOnPullUpUpdateTask != null) {
            RefreshableListView.this.mOnPullUpUpdateTask.updateBackground();
          }
          long delta = 500L - (
            System.currentTimeMillis() - b);
          if (delta > 0L) {
            try {
              Thread.sleep(delta);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }

          RefreshableListView.this.post(new Runnable() {
            public void run() {
              int deltay = RefreshableListView.this.mListBottomView.close(0);
              RefreshableListView.this.postDelayed(new Runnable()
              {
                public void run()
                {
                  if (RefreshableListView.this.mAutoNotifyAdapter != null) {
                    RefreshableListView.this.mAutoNotifyAdapter.notifyDataSetChanged();
                  }
                  if (RefreshableListView.this.mOnPullUpUpdateTask != null)
                    RefreshableListView.this.mOnPullUpUpdateTask.updateUI();
                }
              }
              , 0L);
            }
          });
        }
      });
      this.mState = 3;
    } else {
      this.mListBottomView.close(0);
    }
  }

  public void update() {
    if (this.mListHeaderView.isUpdateNeeded()) {
      if (this.mOnUpdateTask != null) {
        this.mOnUpdateTask.onUpdateStart();
      }
      this.mListHeaderView.startUpdate(new Runnable() {
        public void run() {
          long b = System.currentTimeMillis();
          if (RefreshableListView.this.mOnUpdateTask != null) {
            RefreshableListView.this.mOnUpdateTask.updateBackground();
          }
          long delta = 500L - (
            System.currentTimeMillis() - b);
          if (delta > 0L) {
            try {
              Thread.sleep(delta);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          RefreshableListView.this.post(new Runnable()
          {
            public void run()
            {
              if (RefreshableListView.this.mAutoNotifyAdapter != null) {
                RefreshableListView.this.mAutoNotifyAdapter.notifyDataSetChanged();
              }
              RefreshableListView.this.mListHeaderView.close(0);
              if (RefreshableListView.this.mOnUpdateTask != null)
                RefreshableListView.this.mOnUpdateTask.updateUI();
            }
          });
        }
      });
      this.mState = 3;
    } else {
      this.mListHeaderView.close(0);
    }
  }

  public boolean dispatchTouchEvent(MotionEvent ev)
  {
    if (this.mState == 3) {
      return super.dispatchTouchEvent(ev);
    }
    int action = ev.getAction() & 0xFF;
    switch (action) {
    case 0:
      this.mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
      this.mLastY = ev.getY();
      isFirstViewTop();
      isLastViewBottom();
      break;
    case 2:
      if (this.mActivePointerId == -1)
      {
        break;
      }
      if (this.mState == 0) {
        isFirstViewTop();
        isLastViewBottom();
      }

      if (this.mState == 1) {
        int activePointerId = this.mActivePointerId;
        int activePointerIndex = 
          MotionEventCompat.findPointerIndex(ev, activePointerId);
        float y = MotionEventCompat.getY(ev, activePointerIndex);
        int deltaY = (int)(y - this.mLastY);
        this.mLastY = y;
        if ((deltaY <= 0) || (Math.abs(y) < this.mTouchSlop)) {
          this.mState = 0;
        } else {
          this.mState = 2;
          ev.setAction(3);
          super.dispatchTouchEvent(ev);
        }
      } else if (this.mState == 4) {
        int activePointerId = this.mActivePointerId;
        int activePointerIndex = 
          MotionEventCompat.findPointerIndex(ev, activePointerId);
        float y = MotionEventCompat.getY(ev, activePointerIndex);
        int deltaY = (int)(y - this.mLastY);
        this.mLastY = y;
        if ((deltaY >= 0) || (Math.abs(y) < this.mTouchSlop)) {
          this.mState = 0;
        } else {
          this.mState = 5;
          ev.setAction(3);
          super.dispatchTouchEvent(ev);
        }
      }

      if (this.mState == 2) {
        int activePointerId = this.mActivePointerId;
        int activePointerIndex = 
          MotionEventCompat.findPointerIndex(ev, activePointerId);
        float y = MotionEventCompat.getY(ev, activePointerIndex);
        int deltaY = (int)(y - this.mLastY);
        this.mLastY = y;

        int headerHeight = this.mListHeaderView.getHeight();
        setHeaderHeight(headerHeight + deltaY * 5 / 9);
        return true;
      }if (this.mState != 5) break;
      int activePointerId = this.mActivePointerId;
      int activePointerIndex = 
        MotionEventCompat.findPointerIndex(ev, activePointerId);
      float y = MotionEventCompat.getY(ev, activePointerIndex);
      int deltaY = (int)(y - this.mLastY);
      this.mLastY = y;
      int headerHeight = this.mListBottomView.getHeight();
      setBottomHeight(headerHeight - deltaY * 5 / 9);
      return true;
    case 1:
    case 3:
      this.mActivePointerId = -1;
      if (this.mState == 2) {
        update(); } else {
        if (this.mState != 5) break;
        pullUpUpdate();
      }
      break;
    case 5:
      int index = MotionEventCompat.getActionIndex(ev);
      y = MotionEventCompat.getY(ev, index);
      this.mLastY = y;
      this.mActivePointerId = MotionEventCompat.getPointerId(ev, index);
      break;
    case 6:
      onSecondaryPointerUp(ev);
    case 4:
    }
    return super.dispatchTouchEvent(ev);
  }

  private void onSecondaryPointerUp(MotionEvent ev) {
    int pointerIndex = MotionEventCompat.getActionIndex(ev);
    int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
    if (pointerId == this.mActivePointerId) {
      int newPointerIndex = pointerIndex == 0 ? 1 : 0;
      this.mLastY = MotionEventCompat.getY(ev, newPointerIndex);
      this.mActivePointerId = MotionEventCompat.getPointerId(ev, 
        newPointerIndex);
    }
  }

  void setState(int state) {
    this.mState = state;
  }

  private void setHeaderHeight(int height) {
    this.mListHeaderView.setHeaderHeight(height);
  }

  private void setBottomHeight(int height) {
    this.mListBottomView.setBottomHeight(height);
  }

  private boolean isLastViewBottom() {
    int count = getChildCount();
    if ((count == 0) || (!this.mPullUpRefreshEnabled)) {
      return false;
    }

    int lastVisiblePosition = getLastVisiblePosition();
    boolean needs = (lastVisiblePosition == getAdapter().getCount() - 1) && 
      (getChildAt(getChildCount() - 1).getBottom() == getBottom() - getTop() - getPaddingBottom());
    if (needs) {
      this.mState = 4;
    }
    return needs;
  }

  private boolean isFirstViewTop() {
    int count = getChildCount();
    if (count == 0) {
      return true;
    }
    int firstVisiblePosition = getFirstVisiblePosition();
    View firstChildView = getChildAt(0);
    boolean needs = (firstChildView.getTop() == getPaddingTop()) && 
      (firstVisiblePosition == 0);
    if (needs) {
      this.mState = 1;
    }

    return needs;
  }

  public static abstract interface OnBottomViewChangedListener extends RefreshableListView.OnHeaderViewChangedListener
  {
  }

  public static abstract interface OnHeaderViewChangedListener
  {
    public abstract void onViewChanged(View paramView, boolean paramBoolean);

    public abstract void onViewHeightChanged(float paramFloat);

    public abstract void onViewUpdating(View paramView);

    public abstract void onViewUpdateFinish(View paramView);
  }

  public static abstract interface OnPullUpUpdateTask extends RefreshableListView.OnUpdateTask
  {
  }

  public static abstract interface OnUpdateTask
  {
    public abstract void onUpdateStart();

    public abstract void updateBackground();

    public abstract void updateUI();
  }
}