package com.example.irtools;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by HuynhHuu on 21-Feb-18.
 */

public class ProgressBarAnimation extends Animation {

    private float from;
    private float to;
    private CircularProgressBar mProgressBar;
    private TextView brightnessTextView;

    public ProgressBarAnimation(CircularProgressBar progressBar, TextView brightnessTextView, float from, float to)
    {
        super();
        this.from = from;
        this.to = to;
        mProgressBar = progressBar;
        mProgressBar.setProgress(0);
        this.brightnessTextView = brightnessTextView;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        mProgressBar.setProgress((int) value);
        brightnessTextView.setText("Loading\n" + (int)(value/100)+ "%" );
    }
}