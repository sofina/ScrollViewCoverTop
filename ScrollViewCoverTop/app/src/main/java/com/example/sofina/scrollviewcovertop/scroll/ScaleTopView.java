
package com.example.sofina.scrollviewcovertop.scroll;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ScaleTopView extends LinearLayout {

    private float mScale = 1;

    private float mAutoTarget = -1;

    public ScaleTopView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public ScaleTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(getWidth() * (1 - mScale) / 2, 0);
        canvas.scale(mScale, mScale);
        if (mAutoTarget != -1) {
            if (mAutoTarget < mScale) {
                mScale -= 0.1f;
                if (mScale < mAutoTarget) {
                    mScale = mAutoTarget;
                    mAutoTarget = -1;
                }
                invalidate();
            } else if (mAutoTarget == 1) {
                mScale += 0.1f;
                if (mScale > mAutoTarget) {
                    mScale = mAutoTarget;
                    mAutoTarget = -1;
                }
                invalidate();
            }
        }
        super.onDraw(canvas);
    }

    public void scale(float f) {
        mScale = f;
        invalidate();
    }

    public void clearScale() {
        mScale = 1;
        invalidate();
    }

    public void autoScale(float target) {
        mAutoTarget = target;
        invalidate();
    }

}
