package com.example.sofina.scrollviewcovertop.scroll;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.ScrollView;
import com.example.sofina.scrollviewcovertop.env.AppEnv;

/**
 * Created by fanxiafei on 2016/9/26.
 */
public class BottomScrollView extends ScrollView {

    MyBottomScrollLayout.OnScrollEvent mOnScrollEvent;

    private State mState;

    private int mMaxY, mMinY;

    public BottomScrollView(Context context) {
        super(context);
    }

    public BottomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(int topPx, int bottomPx, MyBottomScrollLayout.OnScrollEvent onScrollEvent) {
        mMinY = topPx;
        mMaxY = bottomPx;
        mOnScrollEvent = onScrollEvent;
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mFlingVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    enum State {
        MIDDLE_SCROLL_MODE, /**
         * 没有收缩之前，被拖着滑动
         */
        AUTO_SCROLL_MODE, /**
         * 没有收缩之前,自动滑动
         */
        SHRINK_SCROLL_MODE, /**
         * 收缩后的滑动
         */
        SHRINK, /**
         * 收缩状态
         */
        EXPAND,/**展开状态*/
    }

    private float mLastInterceptY;

    private int mTouchSlop;

    private int mFlingVelocity;

    private float mLastY;

    private VelocityTracker mVelocityTracker;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        mLastY = y;
        return super.onInterceptTouchEvent(ev);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float y = ev.getY();
        float deltaY = 0;
        if (AppEnv.bAppdebug) {
            Log.d("BottomScrollView", "[onTouchEvent]: getScrollY()=" + getScrollY() + "  mMaxY=" + mMaxY + "  deltaY=" + deltaY + "   mLastY=" + mLastY + "    y=" + y);
        }
        //获取速度
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            deltaY = y - mLastY;

            if (AppEnv.bAppdebug) {
                Log.d("BottomScrollView", "[onTouchEvent]: deltaY=" + deltaY + "   mLastY=" + mLastY + "    y=" + y + "   getScrollY()=" + getScrollY());
            }
            mVelocityTracker.computeCurrentVelocity(1000);
            float velocityY = mVelocityTracker.getYVelocity();
            if (AppEnv.bAppdebug) {
                Log.d("BottomScrollView", "[onTouchEvent]: velocityY=" + velocityY + "  mFlingVelocity=" + mFlingVelocity);
            }

            if (getScrollY() > mMaxY && deltaY > 0) {
                smoothScroll(true, true);
                return true;
            } else if (getScrollY() < mMaxY && getScrollY() > mMinY) {
                float rate = (float) getScrollY() / (float) mMaxY;
                smoothScroll(false, rate < 0.5);
                return true;
            } else {
                return super.onTouchEvent(ev);
            }
        }
        return super.onTouchEvent(ev);
    }

    boolean mIsSmoothScroll;

    boolean mUp;

    boolean mIsTopInternal;

    private void smoothScroll(boolean isTopInternal, boolean up) {
        mIsSmoothScroll = true;
        mUp = up;
        mIsTopInternal = isTopInternal;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void computeScroll() {

        if ((getScrollY() == mMaxY && mUp) || (getScrollY() == mMinY && !mUp) || (mIsTopInternal && getScrollY() == mMaxY)) {
            mIsSmoothScroll = false;
        }
        if (mIsSmoothScroll) {
            if(mIsTopInternal && getScrollY() > mMaxY){
                smoothScrollBy(0, -10);
            }
            if (getScrollY() < mMaxY && mUp) {
                smoothScrollBy(0, 10);
            } else if (getScrollY() > mMinY && !mUp) {
                smoothScrollBy(0, -10);
            }
            postInvalidateOnAnimation();
        } else {
            super.computeScroll();
        }
    }

}
