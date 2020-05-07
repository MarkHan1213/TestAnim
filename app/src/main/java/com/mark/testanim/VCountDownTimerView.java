package com.mark.testanim;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class VCountDownTimerView extends VTickMarkView {

    private static final String TAG = VCountDownTimerView.class.getSimpleName();

    private Drawable mTimerProgressBridge;
    private Drawable mTimerProgressFullBridge;
    private Paint mCountDownLabelPaint;

    private int mBridgeDrawableWidth;

    private boolean isCountingDown;
    /**
     * time unit: second
     */
    private int[] mCountDownSeconds;
    /**
     * the marker index which count down start from
     */
    private int mCountDownFrom = -1;
    private int mCountLeftSeconds;
    private float mMovedDistancePerSecond;

    private VCountDownStatusListener mCountDownStatusListener;

    private static final int MSG_COUNT_DOWN = 0;

    private final Handler mHandler = new CountingHandler(this);

    private static final class CountingHandler extends Handler {
        private WeakReference<VCountDownTimerView> weakReference;

        CountingHandler(VCountDownTimerView countDownTimerView) {
            weakReference = new WeakReference<>(countDownTimerView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (weakReference != null && weakReference.get() != null) {
                weakReference.get().refresh();
            }
        }
    }

    public interface VCountDownStatusListener {
        void onStart(int indexFrom);

        void onCancel();

        void onFinish();
    }

    ViewOutlineProvider mProvider = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, getWidth(),
                    getHeight() + 20, 24);
        }
    };

    public void setCountDownListener(VCountDownStatusListener listener) {
        mCountDownStatusListener = listener;
    }

    public VCountDownTimerView(Context context) {
        this(context, null);
    }

    public VCountDownTimerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VCountDownTimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    public VCountDownTimerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CountDownTimerView, defStyleAttr, defStyleRes);
        Resources r = getResources();
        mTimerProgressBridge = r.getDrawable(a.getResourceId(
                R.styleable.CountDownTimerView_timer_progress_bridge_src, R.drawable.mute_timer_progress_bridge));
        mTimerProgressFullBridge = r.getDrawable(a.getResourceId(
                R.styleable.CountDownTimerView_timer_progress_full_end_src, R.drawable.mute_timer_progress_full_bridge));

        a.recycle();

        //todo: remove
        if (DBG) {
            setCountDownSeconds(new int[]{0, 10, 20, 30, 40});
            setCountDownListener(new VCountDownStatusListener() {
                Toast toast;

                @Override
                public void onStart(int indexFrom) {
                    showToast("onStart :" + indexFrom);
                }

                @Override
                public void onCancel() {
                    showToast("onCancel :");
                }

                @Override
                public void onFinish() {
                    showToast("onFinish :");
                }

                private void showToast(String text) {
                    if (toast != null) {
                        toast.cancel();
                    }
                    Log.d(TAG, text);
                    toast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }

        setTrackTouchListener(new TrackTouchListener() {
            @Override
            public void onStartTrack(MotionEvent event) {
                if (isCountingDown) {
                    cancelCountDown();
                }
            }

            @Override
            public void onStopTrack(MotionEvent event) {
                int y = getThumbCenterYByTouchEvent(event);
                int index = thumbCenterYToMarkerIndex(y);
                if (index != 0 && !getMaxMode()) {
                    startCountDown(index);
                }
            }
        });

        initCountDownPaint();

        setMarker(0);

        setOutlineProvider(mProvider);
        setClipToOutline(true);
        invalidateOutline();
    }

    private void initCountDownPaint() {
        if (mCountDownLabelPaint == null) {
            mCountDownLabelPaint = new Paint();
        }
        mCountDownLabelPaint.setColor(LABEL_COLOR_SELECTED);
        mCountDownLabelPaint.setTextSize(LABEL_TEXT_SIZE);
        mCountDownLabelPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        mCountDownLabelPaint.setStyle(Paint.Style.FILL);
        mCountDownLabelPaint.setAntiAlias(true);
        mCountDownLabelPaint.setTextAlign(Paint.Align.CENTER);
    }

    private String formatTime(int second) {
        int h = 0, m = 0, s = 0;
        int min = second / 60;
        s = second % 60;
        if (min > 0) {
            h = min / 60;//get hours
            m = min % 60;//get minutes
        }
        String str = String.format("%d:%02d:%02d", h, m, s);
        return str;
    }

    private void startCountDown(int fromIndex) {
        startCountDown(mCountDownSeconds[fromIndex], fromIndex);

        if (mCountDownStatusListener != null) {
            mCountDownStatusListener.onStart(fromIndex);
        }
    }

    private void scheduleCountingPerSecond() {
        mHandler.removeMessages(MSG_COUNT_DOWN);
        mHandler.sendEmptyMessageDelayed(MSG_COUNT_DOWN, 1000);
    }

    private void getDistancePerSecondMove() {
        if (mCountDownSeconds != null && mCountDownFrom > 0) {
            mMovedDistancePerSecond = mCountDownFrom * (getThumbMaxY() - getThumbMinY()) / ((getMarkerSize() - 1) * mCountDownSeconds[mCountDownFrom]);
        }
    }

    public void startCountDown(int leftSecond, int fromIndex) {
        if (mCountDownSeconds == null) {
            throw new NullPointerException("NullPointerException, you have not set mCountDownSeconds through calling setCountDownSeconds()");
        }
        if (fromIndex <= 0 || fromIndex > getMarkerSize() - 1) {
            throw new IllegalArgumentException("illegal fromIndex=" + fromIndex + " the index should between 0 and " + (getMarkerSize() - 1));
        }
        if (leftSecond < 0 || leftSecond > mCountDownSeconds[fromIndex]) {
            throw new IllegalArgumentException("illegal args, leftSecond should between 0 and " + mCountDownSeconds[fromIndex]);
        }
        isCountingDown = true;
        mCountDownFrom = fromIndex;
        mCountLeftSeconds = leftSecond;
        getDistancePerSecondMove();
        updateThumbCenter(getThumbX(), getThumbCountingY());
        scheduleCountingPerSecond();
    }

    private void refresh() {
        --mCountLeftSeconds;
        int y = (int) (getThumbMaxY() - mMovedDistancePerSecond * mCountLeftSeconds);
        boolean keep = true;
        if (mCountLeftSeconds <= 0) {
            keep = false;
        }
        if (DBG) {
            Log.d(TAG, "refresh mCountLeftSeconds:" + mCountLeftSeconds
                    + "  y:" + y + "  maxY:" + getThumbMaxY() + "  keep:" + keep);
        }
        if (y > getThumbMaxY()) {
            y = getThumbMaxY();
        }
        updateThumbCenter(getThumbX(), y);
        if (keep) {
            scheduleCountingPerSecond();
        } else {
            finishCountDown();
            setMarker(0);
        }
    }

    private void finishCountDown() {
        isCountingDown = false;
        mHandler.removeMessages(MSG_COUNT_DOWN);
        if (mCountDownStatusListener != null) {
            mCountDownStatusListener.onFinish();
        }
    }

    private void cancelCountDown() {
        isCountingDown = false;
        mHandler.removeMessages(MSG_COUNT_DOWN);
        if (mCountDownStatusListener != null) {
            mCountDownStatusListener.onCancel();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mBridgeDrawableWidth = getWidth();
    }

    @Override
    void updateThumbPosInnerIfSizeChanged() {
        getDistancePerSecondMove();
        updateThumbCenter(getThumbX(), getThumbCountingY());
    }

    private int getThumbCountingY() {
        return getThumbMaxY() - (int) (mCountLeftSeconds * mMovedDistancePerSecond);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isPressed() || getMaxMode()) {
            drawCountDownProgress(canvas);
            super.onDraw(canvas);
        } else if (!isCountingDown) {
            super.onDraw(canvas);
        } else {
            drawCountDownProgress(canvas);
//            drawThumb(canvas);
            drawCountDownLabel(canvas);

            drawDebugLineIfDebugMode(canvas);
        }
    }

    /**
     * @param secondsArray the element in the array from index 1 to the last should be positive value
     */
    public void setCountDownSeconds(int[] secondsArray) {
        if (secondsArray.length <= 1) {
            throw new IllegalArgumentException("secondsArray lenth should >1");
        }
        setMarkerSize(secondsArray.length);
        mCountDownSeconds = secondsArray;
    }

    private void drawCountDownProgress(Canvas canvas) {
        //progress bridge
        int top = !isDragging() && getMaxMode() ? 0 : getThumbY();
        Drawable bridge = top == 0 ? mTimerProgressFullBridge : mTimerProgressBridge;
        bridge.setBounds(0,
                top,
                mBridgeDrawableWidth,
                getThumbMaxY());
        bridge.draw(canvas);
    }

    private void drawCountDownLabel(Canvas canvas) {
        canvas.drawText(formatTime(mCountLeftSeconds), getThumbX(), getUnfocusedNormalLabelY(), mCountDownLabelPaint);
    }
}
