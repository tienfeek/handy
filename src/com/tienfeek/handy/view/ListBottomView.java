package com.tienfeek.handy.view;

import android.content.Context;
import android.view.View;
import android.widget.ListAdapter;

public class ListBottomView extends ListHeaderView
{
  public ListBottomView(Context context, RefreshableListView list)
  {
    super(context, list);
  }

  protected void onLayout(boolean changed, int l, int t, int r, int b)
  {
    View childView = getChildView();
    if (childView == null) {
      return;
    }

    int childViewWidth = childView.getMeasuredWidth();
    int childViewHeight = childView.getMeasuredHeight();
    childView.layout(0, 0, childViewWidth, childViewHeight);
  }

  public void setBottomHeight(int height) {
    setHeaderHeight(height);
    this.mListView.setSelection(this.mListView.getAdapter().getCount() - 1);
  }

  public void setHeaderHeight(int height)
  {
    super.setHeaderHeight(height);
  }
}