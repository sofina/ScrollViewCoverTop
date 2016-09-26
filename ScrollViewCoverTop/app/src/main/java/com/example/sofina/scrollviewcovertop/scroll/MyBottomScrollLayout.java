package com.example.sofina.scrollviewcovertop.scroll;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.ScrollView;
import android.widget.Scroller;
import com.example.sofina.scrollviewcovertop.env.AppEnv;

/**
 * 上滑可盖住头部滑动类
 * Created by sofina on 2016/9/20.
 */
public class MyBottomScrollLayout extends ScrollView {

    private String TAG = MyBottomScrollLayout.class.getSimpleName();

    private Context mContext;

    private boolean mEnable;

    private int mMinY;

    private int mMaxY;

    private int mTouchSlop;

    private int mFlingVelocity;

    private VelocityTracker mVelocityTracker;

    private OnScrollEvent mOnScrollEvent;

    public MyBottomScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void init(int topPx, int bottomPx, OnScrollEvent onScrollEvent) {
        mMinY = - bottomPx;
        mMaxY = 0;
        mOnScrollEvent = onScrollEvent;
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mScroller = new Scroller(getContext());
    }

    public boolean isEnable() {
        return mEnable;
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
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

    public interface OnScrollEvent {
        void onScroll(float f);

        void onAutoScroll(boolean upOrDown);
    }

    private State mState = State.EXPAND;

    private boolean mValid;

    private float mDownY;

    private int mDownTopMargin;

    private int mDownScrollY;

    private float mLastY;

    private float deltaY;

    private Scroller mScroller;

    int mCurrY, mFinalY;

    boolean mAutoScrolling;

    float mScaleRate = 1.0f;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return super.onInterceptTouchEvent(event);
        }

        float y = event.getRawY();
        parseState();

        //获取速度
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = y;
                mDownTopMargin = ((MarginLayoutParams) this.getLayoutParams()).topMargin;
                mDownScrollY = getScrollY();
                mValid = true;
                if (AppEnv.bAppdebug) {
                    Log.d(TAG, "[onInterceptTouchEvent ACTION_DOWN]: isAutoScrollFinished=" + isAutoScrollFinished());
                }
                if (!isAutoScrollFinished()) {
                    abortAnimation();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(deltaY) > mTouchSlop) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(mState == State.EXPAND && y > mDownY){
                    return true;
                }
                recycleVelocity();
                break;
            default:
                break;
        }
        boolean intercept = super.onInterceptTouchEvent(event);
        if (AppEnv.bAppdebug) {
            Log.d(TAG, "[onInterceptTouchEvent]: intercept=" + intercept);
        }

        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return super.onTouchEvent(event);
        }

        if (!mValid) {
            return true;
        }
        if (AppEnv.bAppdebug) {
            Log.d(TAG, "[onTouchEvent]:   mState=" + mState + "  MotionEvent=" + event.getAction());
        }

        //获取速度
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        float y = event.getRawY();
        parseState();
        parseStateOnTouchEvent(mLastY, y);
        boolean consume;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isAutoScrollFinished()) {
                    abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                deltaY = y - mLastY;
                if (mState == State.SHRINK_SCROLL_MODE) {
                    // 顶部上划和下划
                    try {
                        //防抖动，margin<0 会发生抖动
                        ((MarginLayoutParams) getLayoutParams()).topMargin = Math.max(-getScrollY(), mMinY);
                        mLastY = y;
                        return super.onTouchEvent(event);
                    } catch (Exception e) {
                        mLastY = y;
                        return false;
                    }
                } else {
                    //手指拖着滑动
                    int inc = mDownTopMargin + (int) (y - mDownY) - mDownScrollY;
                    mCurrY = Math.max(mMinY, Math.min(inc, mMaxY));
                    dragSmoothScrollY(mCurrY);
                    mLastY = y;
                    return true;
                }
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityY = mVelocityTracker.getYVelocity();
                deltaY = y - mLastY;
                switch (mState) {
                    case MIDDLE_SCROLL_MODE:
                        //自动滑动
                        if (Math.abs(velocityY) >= mFlingVelocity) {
                            mDirection = (y-mDownY) < 0;//速度快，直接滑到终点
                        } else {
                            mDirection = mScaleRate < 0.5f;//速度慢，滑到离得近的一边
                        }
                        mCurrY = (int) (mScaleRate * (mMaxY - mMinY));
                        if (AppEnv.bAppdebug) {
                            Log.d(TAG, "[onTouchEvent]:  mScaleRate=" + mScaleRate + "  y=" + y + "   mLastY=" + mLastY +"   mDownY=" + mDownY);
                        }
                        smoothScrollY(mDirection, mCurrY);
                        recycleVelocity();
                        mLastY = y;
                        return true;

                    case EXPAND:
                        if (AppEnv.bAppdebug) {
                            Log.d(TAG, "[onTouchEvent]:  mScaleRate=" + mScaleRate + "  y=" + y + "   mDownY=" + mDownY);
                        }
                        if (Math.abs(velocityY) >= mFlingVelocity && y < mDownY) {
                            mCurrY = mMaxY;
                            smoothScrollY(true, mCurrY);
                            recycleVelocity();
                            mLastY = y;
                            return true;
                        }else {
                            return true;
                        }
                    default:
                        break;
                }
                break;

            default:
                break;
        }
        mLastY = y;
        consume = super.onTouchEvent(event);
        if (AppEnv.bAppdebug) {
            Log.d(TAG, "[onTouchEvent]:  mScaleRate=" + mScaleRate + "  consume=" + consume);
        }
        return super.onTouchEvent(event);
    }

    private void abortAnimation() {
        if (AppEnv.bAppdebug) {
            Log.d(TAG, "[abortAnimation]: ");
        }
        mCurrY = mFinalY;
        mAutoScrolling = false;
        parseState();
    }

    private boolean isAutoScrollFinished() {
        return !mAutoScrolling;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            if (mScaleRate < 1 && mScaleRate > 0) {
                smoothScrollY(mScaleRate <= 0.5, mCurrY);
            }
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != VISIBLE) {
            clearAnimation();
        }
    }

    private void smoothScrollY(boolean direction, int currentY) {

        mState = State.AUTO_SCROLL_MODE;
        mAutoScrolling = true;

        int targetY = direction ? mMinY : mMaxY;
        ValueAnimator animator = ValueAnimator.ofFloat(currentY, targetY);
        animator.setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator(1.5f));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                ((MarginLayoutParams) getLayoutParams()).topMargin = (int) (value / 1);
                requestLayout();
                if (mOnScrollEvent != null) {
                    mScaleRate = value / mMaxY - mMinY;
                    mOnScrollEvent.onScroll(mScaleRate);
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAutoScrolling = false;
            }
        });

        animator.start();

    }

    private void dragSmoothScrollY(int currentY) {
        ((MarginLayoutParams) getLayoutParams()).topMargin = currentY;
        requestLayout();

        if (mOnScrollEvent != null) {
            float diff = currentY - mMinY;
            float total = mMaxY - mMinY;
            mScaleRate = diff / total;
            mOnScrollEvent.onScroll(mScaleRate);
        }

    }

    private void recycleVelocity() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private boolean mDirection;

    private void parseState() {
        int margin = ((MarginLayoutParams) getLayoutParams()).topMargin;
        if (AppEnv.bAppdebug) {
            Log.d(TAG, "[parseState]:   mScaleRate=" + mScaleRate + "  mState=" + mState + "   getScrollY()=" + getScrollY() + "  margin=" + margin);
        }

        if (margin == mMinY && mScaleRate == 0) {
            if (getScrollY() > 0) {
                mState = State.SHRINK_SCROLL_MODE;
            } else {
                mState = State.SHRINK;
            }
        } else if (margin == mMaxY && mScaleRate == 1) {
            mState = State.EXPAND;
        } else if (mScaleRate > 0 && mScaleRate < 1 && !mAutoScrolling) {
            mState = State.MIDDLE_SCROLL_MODE;
        } else {
            mState = State.AUTO_SCROLL_MODE;
        }
        if (AppEnv.bAppdebug) {
            Log.d(TAG, "[parseState]:   mScaleRate=" + mScaleRate + "  mState=" + mState);
        }
    }

    private void parseStateOnTouchEvent(float fromY, float toY) {
        //在top时，如果这时候还往上滑，那么就是顶部上滑，如果是往下滑，并且getScrollY() > 0， 那么是顶部下滑
        if (AppEnv.bAppdebug) {
            Log.d(TAG, "[parseStateOnTouchEvent]:   getScrollY()=" + getScrollY() + "  mState=" + mState);
        }
        if (mState == State.SHRINK) {
            if (toY < fromY || toY >= fromY && getScrollY() > 0) {
                mState = State.SHRINK_SCROLL_MODE;
            }
        }
    }
}
