package com.example.sofina.scrollviewcovertop;

import android.app.Activity;
import android.os.Bundle;
import com.example.sofina.scrollviewcovertop.scroll.MyBottomScrollLayout;
import com.example.sofina.scrollviewcovertop.scroll.ScaleTopView;
import com.example.sofina.scrollviewcovertop.utils.CommonUIUtils;

/**
 * Created by sofina on 2016/9/22.
 */
public class ScrollActivity extends Activity {

    private ScaleTopView mTopView;

    private MyBottomScrollLayout mBottomScrollLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scroll_layout);
        mTopView = (ScaleTopView) findViewById(R.id.top);
        mBottomScrollLayout = (MyBottomScrollLayout) findViewById(R.id.bottom);
        mBottomScrollLayout.setEnable(true);
        mBottomScrollLayout.init(0, CommonUIUtils.dip2px(this, 250), mScrollEvent);
    }

    private final MyBottomScrollLayout.OnScrollEvent mScrollEvent = new MyBottomScrollLayout.OnScrollEvent() {

        @Override
        public void onScroll(float f) {
            mTopView.scale(f);
        }

        @Override
        public void onAutoScroll(boolean upOrDown) {
            mTopView.autoScale(upOrDown ? 0 : 1);
        }
    };

}
