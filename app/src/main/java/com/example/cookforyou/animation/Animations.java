package com.example.cookforyou.animation;

import android.animation.Animator;
import android.view.View;
import android.view.ViewPropertyAnimator;

public final class Animations {

    public static void crossfade(final View from, final View to, final int duration,
                           final Animator.AnimatorListener listener) {
        to.setAlpha(0f);
        to.setVisibility(View.VISIBLE);

        to.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);

        ViewPropertyAnimator animator = from.animate()
                .alpha(0f)
                .setDuration(duration);
        if(listener == null) {
            animator.setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    from.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else {
            animator.setListener(listener);
        }
    }
}
