package com.mark.testanim;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ImageView dragImage;
    private LinearLayout linearLayout, center;
    private RelativeLayout topContainer;
    private TextView title;
    private ImageView bottom,top_mute;
    private FrameLayout top;
    private FrameLayout seek_bar_bg_layout;


    private int width, height;

    private static final String INTENT_DESKTOP_CLIP = "com.smartisanos.desktop.clip";
    private FrameLayout ll_group;
    private TextView one, two, three;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }


    private void initViews() {
        dragImage = findViewById(R.id.img);
        dragImage.measure(0, 0);
        width = dragImage.getMeasuredHeight();
        height = dragImage.getMeasuredWidth();
        topContainer = findViewById(R.id.topContainer);
        linearLayout = findViewById(R.id.container);
        title = findViewById(R.id.title);
        center = findViewById(R.id.center);
        top = findViewById(R.id.top);
        bottom = findViewById(R.id.bottom);
        top_mute = findViewById(R.id.top_mute);
        seek_bar_bg_layout = findViewById(R.id.seek_bar_bg_layout);

        dragImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //创建移动数据
                ClipData.Item item = new ClipData.Item((String) v.getTag());
                ClipData data = new ClipData(INTENT_DESKTOP_CLIP,
                        new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                        item);
                //调用startDrag方法，第二个参数为创建拖放阴影
                v.startDrag(data, new View.DragShadowBuilder(v), null, View.DRAG_FLAG_GLOBAL);
                return true;
            }
        });

        topContainer.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        //拖拽开始事件
                        if (event.getClipDescription().hasMimeType(
                                ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                            return true;
                        }
                        return false;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        //被拖放View进入目标View
                        linearLayout.setBackgroundColor(Color.YELLOW);
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        //被拖放View离开目标View
                        linearLayout.setBackgroundColor(Color.BLUE);
                        title.setText("");
                        return true;
                    case DragEvent.ACTION_DROP:
                        //释放拖放阴影，并获取移动数据
                        title.setText(event.getY() + ":" + event.getX());
                        dragImage.setX(event.getX() - width / 2);
                        dragImage.setY(event.getY() - height / 2);
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        //拖放事件完成
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });

        ll_group = findViewById(R.id.ll_group);

        //one = findViewById(R.id.one);
        two = findViewById(R.id.one);
        three = findViewById(R.id.two);

        bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Trace.beginSection(" Do GroupAnim");
//                doGroupAnim(v);
//                Trace.endSection();
                TransitionSet transition = new TransitionSet();
                transition.setOrdering(TransitionSet.ORDERING_TOGETHER);
                if (!isOpen) {
                    transition.setDuration(300L);
                    transition.addTransition(new Fade(Fade.OUT))
                            .addTransition(new ChangeBounds())
                            .addTransition(new TestAlphaTransition())
                            .addTransition(new Fade(Fade.IN).setStartDelay(200L));
                } else {
                    transition.addTransition(new Fade(Fade.OUT).setDuration(100L))
                            .addTransition(new ChangeBounds().setDuration(300L))
                            .addTransition(new TestAlphaTransition())
                            .addTransition(new Fade(Fade.IN));
                }
                transition.setInterpolator(new DecelerateInterpolator());
                transition.addListener(new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionEnd(Transition transition) {
                        isOpen = !isOpen;
                    }
                });
                TransitionManager.beginDelayedTransition(topContainer, transition);
                changeOpenLayout();
            }
        });

        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Trace.beginSection("Do Group Move Anim");
//                doGroupMoveAnim();
//                Trace.endSection();
                doGroupMoveTransition();
            }
        });

        mCountDownTimer = (VCountDownTimerView) this.findViewById(R.id.count_down_timer);
        mCountDownTimer.setCountDownSeconds(new int[]{0, 20, 30, 40, 50, 60, 80, 90, 100, 110, 120, 130});
        mCountDownTimer.setCountDownListener(new VCountDownTimerView.VCountDownStatusListener() {
            @Override
            public void onStart(int indexFrom) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFinish() {
                doGroupMoveTransition();
            }
        });
    }

    private void doGroupMoveTransition() {
        TransitionSet transition = new AutoTransition();
        transition.setOrdering(TransitionSet.ORDERING_TOGETHER);
        transition.setDuration(300L);
        transition.setInterpolator(new DecelerateInterpolator());
        transition.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                isMoved = !isMoved;
            }
        });

        TransitionManager.beginDelayedTransition(topContainer, transition);
        changeMoveLayout();
    }

    private VCountDownTimerView mCountDownTimer;


    private void changeOpenLayout() {
        Resources res = getResources();
        final ViewGroup.LayoutParams layoutParams = ll_group.getLayoutParams();

        int width;
        if (isOpen) {
            width = res.getDimensionPixelSize(
                    R.dimen.anim_start);
            two.setVisibility(View.GONE);
            three.setVisibility(View.GONE);
//            ((GradientDrawable)ll_group.getBackground()).setColor(0xFFFFFF);
            ll_group.setBackground(getDrawable(R.drawable.active_bg_blue));
        } else {
            width = res.getDimensionPixelSize(
                    R.dimen.anim_end);
            two.setVisibility(View.VISIBLE);
            three.setVisibility(View.VISIBLE);
//            ((GradientDrawable)ll_group.getBackground()).setColor(0x9CC8FF);
            ll_group.setBackground(getDrawable(R.drawable.active_bg));
        }

        layoutParams.width = width;
        ll_group.setLayoutParams(layoutParams);

    }

    private void changeMoveLayout() {
        final int anim_start = getResources().getDimensionPixelSize(
                R.dimen.anim_group_translationX);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ll_group.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                isMoved ? layoutParams.rightMargin - anim_start : layoutParams.rightMargin + anim_start, layoutParams.bottomMargin);
        final int anim_top_start = getResources().getDimensionPixelSize(
                R.dimen.anim_top_translationY);
        final int anim_top_start_h = getResources().getDimensionPixelSize(
                R.dimen.anim_top_start_h);
        final RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) top.getLayoutParams();
        topLayoutParams.setMargins(topLayoutParams.leftMargin,
                isMoved ? topLayoutParams.topMargin - anim_top_start : topLayoutParams.topMargin + anim_top_start, topLayoutParams.rightMargin, topLayoutParams.bottomMargin);
        top.getLayoutParams().height = isMoved ? anim_top_start_h : ll_group.getHeight();
        top_mute.setVisibility(isMoved ? View.VISIBLE : View.GONE);
        seek_bar_bg_layout.setVisibility(isMoved ? View.GONE : View.VISIBLE);


        topContainer.requestLayout();
    }


    private boolean isOpen = false;

    private void doGroupAnim(View v) {
        Resources res = getResources();
        final ViewGroup.LayoutParams layoutParams = ll_group.getLayoutParams();

        final int anim_start = res.getDimensionPixelSize(
                R.dimen.anim_start);
        int anim_end = res.getDimensionPixelSize(
                R.dimen.anim_end);

        ValueAnimator groupOpen = ValueAnimator.ofFloat(anim_start, anim_end);
        groupOpen.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float width = (float) animation.getAnimatedValue();
                if (width < anim_start) {
                    width = anim_start;
                }
                layoutParams.width = (int) width;
                ll_group.setLayoutParams(layoutParams);
            }
        });

        ObjectAnimator twoAlpha;
        ObjectAnimator threeAlpha;
        if (!isOpen) {
            twoAlpha = ObjectAnimator.ofFloat(two, "alpha", 0.5f, 1f);
            threeAlpha = ObjectAnimator.ofFloat(three, "alpha", 0.5f, 1f);
        } else {
            twoAlpha = ObjectAnimator.ofFloat(two, "alpha", 0f, 1f);
            threeAlpha = ObjectAnimator.ofFloat(three, "alpha", 0f, 1f);
        }


//        groupOpen.setDuration(300);
//        groupOpen.setInterpolator(new DecelerateInterpolator());

        AnimatorSet childAnimSet = new AnimatorSet();
        childAnimSet.playTogether(twoAlpha, threeAlpha);
        childAnimSet.setDuration(200L);
        childAnimSet.setStartDelay(200L);


        AnimatorSet animSet = new AnimatorSet();

        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.play(groupOpen).with(childAnimSet);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isOpen = !isOpen;
            }

        });


        if (isOpen) {
            childAnimSet.reverse();
            animSet.reverse();
        } else {
            animSet.start();
        }

    }


    private boolean isMoved = false;

    private void doGroupMoveAnim() {
        Resources res = getResources();
        final int anim_start = res.getDimensionPixelSize(
                R.dimen.anim_group_translationX);


        final ViewGroup.LayoutParams layoutParams = top.getLayoutParams();

        final int anim_top_start = res.getDimensionPixelSize(
                R.dimen.anim_top_start_h);
        int anim_top_end = res.getDimensionPixelSize(
                R.dimen.anim_top_end_h);

        ValueAnimator top_open = ValueAnimator.ofFloat(anim_top_start, anim_top_end);
        top_open.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float h = (float) animation.getAnimatedValue();
                if (h < anim_start) {
                    h = anim_start;
                }
                layoutParams.height = (int) h;
                top.setLayoutParams(layoutParams);
            }
        });
        top_open.setDuration(300L);

        ObjectAnimator groupOpen = ObjectAnimator.ofFloat(ll_group, "translationX", 0f, -anim_start);
        groupOpen.setDuration(300L);
        groupOpen.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isMoved = !isMoved;
            }
        });


        ObjectAnimator top_open_down = ObjectAnimator.ofFloat(top, "translationY", 0f, ll_group.getY());
        top_open_down.setDuration(300L);

        AnimatorSet animSet = new AnimatorSet();

        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setDuration(300L);
        animSet.play(groupOpen).with(top_open).with(top_open_down);

        if (isMoved) {
            animSet.reverse();
        } else {
            animSet.start();
        }
    }



    private long[] mHits = new long[2];
    long doubleTapTimeOut = 1000;
    private long lastDoubleClickTime;


    public void doubleClick(View view) {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (lastDoubleClickTime != 0 && SystemClock.uptimeMillis() - lastDoubleClickTime < doubleTapTimeOut) {
            return;
        }
        if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
            Log.e("--Main--", "------------双击--------");
            lastDoubleClickTime = SystemClock.uptimeMillis();
        }
    }
}
