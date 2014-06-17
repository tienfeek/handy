package com.tienfeek.handy.view;

import android.os.Build;
import android.view.MotionEvent;

public class MotionEventCompat
{
  static final MotionEventVersionImpl IMPL;
  public static final int ACTION_MASK = 255;
  public static final int ACTION_POINTER_DOWN = 5;
  public static final int ACTION_POINTER_UP = 6;
  public static final int ACTION_HOVER_MOVE = 7;
  public static final int ACTION_SCROLL = 8;
  public static final int ACTION_POINTER_INDEX_MASK = 65280;
  public static final int ACTION_POINTER_INDEX_SHIFT = 8;
  public static final int ACTION_HOVER_ENTER = 9;
  public static final int ACTION_HOVER_EXIT = 10;

  static
  {
    if (Build.VERSION.SDK_INT >= 5)
      IMPL = new EclairMotionEventVersionImpl();
    else
      IMPL = new BaseMotionEventVersionImpl();
  }

  public static int getActionMasked(MotionEvent event)
  {
    return event.getAction() & 0xFF;
  }

  public static int getActionIndex(MotionEvent event)
  {
    return (event.getAction() & 0xFF00) >> 
      8;
  }

  public static int findPointerIndex(MotionEvent event, int pointerId)
  {
    return IMPL.findPointerIndex(event, pointerId);
  }

  public static int getPointerId(MotionEvent event, int pointerIndex)
  {
    return IMPL.getPointerId(event, pointerIndex);
  }

  public static float getX(MotionEvent event, int pointerIndex)
  {
    return IMPL.getX(event, pointerIndex);
  }

  public static float getY(MotionEvent event, int pointerIndex)
  {
    return IMPL.getY(event, pointerIndex);
  }

  public static int getPointerCount(MotionEvent event)
  {
    return IMPL.getPointerCount(event);
  }

  static class BaseMotionEventVersionImpl
    implements MotionEventCompat.MotionEventVersionImpl
  {
    public int findPointerIndex(MotionEvent event, int pointerId)
    {
      if (pointerId == 0)
      {
        return 0;
      }
      return -1;
    }

    public int getPointerId(MotionEvent event, int pointerIndex) {
      if (pointerIndex == 0)
      {
        return 0;
      }
      throw new IndexOutOfBoundsException("Pre-Eclair does not support multiple pointers");
    }

    public float getX(MotionEvent event, int pointerIndex) {
      if (pointerIndex == 0) {
        return event.getX();
      }
      throw new IndexOutOfBoundsException("Pre-Eclair does not support multiple pointers");
    }

    public float getY(MotionEvent event, int pointerIndex) {
      if (pointerIndex == 0) {
        return event.getY();
      }
      throw new IndexOutOfBoundsException("Pre-Eclair does not support multiple pointers");
    }

    public int getPointerCount(MotionEvent event) {
      return 1;
    }
  }

  static class EclairMotionEventVersionImpl
    implements MotionEventCompat.MotionEventVersionImpl
  {
    public int findPointerIndex(MotionEvent event, int pointerId)
    {
      return MotionEventCompatEclair.findPointerIndex(event, pointerId);
    }

    public int getPointerId(MotionEvent event, int pointerIndex) {
      return MotionEventCompatEclair.getPointerId(event, pointerIndex);
    }

    public float getX(MotionEvent event, int pointerIndex) {
      return MotionEventCompatEclair.getX(event, pointerIndex);
    }

    public float getY(MotionEvent event, int pointerIndex) {
      return MotionEventCompatEclair.getY(event, pointerIndex);
    }

    public int getPointerCount(MotionEvent event) {
      return MotionEventCompatEclair.getPointerCount(event);
    }
  }

  static abstract interface MotionEventVersionImpl
  {
    public abstract int findPointerIndex(MotionEvent paramMotionEvent, int paramInt);

    public abstract int getPointerId(MotionEvent paramMotionEvent, int paramInt);

    public abstract float getX(MotionEvent paramMotionEvent, int paramInt);

    public abstract float getY(MotionEvent paramMotionEvent, int paramInt);

    public abstract int getPointerCount(MotionEvent paramMotionEvent);
  }
}