package com.mark.testanim;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class MuteFrameLayout extends FrameLayout {

    public MuteFrameLayout(Context context) {
        super(context);
    }

    public MuteFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MuteFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MuteFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
}
