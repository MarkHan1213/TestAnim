package com.mark.testanim;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;

public class TestAlphaTransition extends Transition {


    private static final String PROPNAME_BACKGROUND =
            "com.mark.testanim:TestAlphaTransition:alpha";


    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        // Call the convenience method captureValues
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        // Get a reference to the view
        View view = transitionValues.view;
        // Store its background property in the values map
        transitionValues.values.put(PROPNAME_BACKGROUND, view.getBackground());
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }


    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        // Store a convenient reference to the target. Both the starting and ending layout have the
        // same target.
        final View view = endValues.view;
        // Store the object containing the background property for both the starting and ending
        // layouts.
        Drawable startBackground = (Drawable) startValues.values.get(PROPNAME_BACKGROUND);
        Drawable endBackground = (Drawable) endValues.values.get(PROPNAME_BACKGROUND);
        // This transition changes background colors for a target. It doesn't animate any other
        // background changes. If the property isn't a ColorDrawable, ignore the target.
        if (startBackground instanceof GradientDrawable && endBackground instanceof GradientDrawable) {
            GradientDrawable startColor = (GradientDrawable) startBackground;
            GradientDrawable endColor = (GradientDrawable) endBackground;
            // If the background color for the target in the starting and ending layouts is
            // different, create an animation.
            if (startColor.getColor().getColorForState(new int[]{0},0xFFFFFF) != endColor.getColor().getColorForState(new int[]{0},0xFFFFFF)) {
                // Create a new Animator object to apply to the targets as the transitions framework
                // changes from the starting to the ending layout. Use the class ValueAnimator,
                // which provides a timing pulse to change property values provided to it. The
                // animation runs on the UI thread. The Evaluator controls what type of
                // interpolation is done. In this case, an ArgbEvaluator interpolates between two
                // #argb values, which are specified as the 2nd and 3rd input arguments.
                ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(),
                        startColor.getColor().getColorForState(new int[]{0},0xFFFFFF), endColor.getColor().getColorForState(new int[]{0},0xFFFFFF));
                // Add an update listener to the Animator object.
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Object value = animation.getAnimatedValue();
                        // Each time the ValueAnimator produces a new frame in the animation, change
                        // the background color of the target. Ensure that the value isn't null.
                        if (null != value) {
                            ((GradientDrawable) view.getBackground()).setColor(ColorStateList.valueOf((Integer) value));
                        }
                    }
                });
                // Return the Animator object to the transitions framework. As the framework changes
                // between the starting and ending layouts, it applies the animation you've created.
                return animator;
            }
        }
        // For non-ColorDrawable backgrounds, we just return null, and no animation will take place.
        return null;
    }
    // END_INCLUDE (create_animator)
}
