/*
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jakewharton.android.viewpagerindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;

/**
 * A FlowIndicator which draws circles (one for each view). The current view
 * position is filled and others are only stroked.<br/><br/>
 * Available attributes are:<br/>
 * <ul>fillColor: Define the color used to fill a circle (default to white)</ul>
 * <ul>strokeColor: Define the color used to stroke a circle (default to white)</ul>
 * <ul>mRadius: Define the circle mRadius (default to 4.0)</ul>
 */
public class CirclePageIndicator extends View implements PageIndicator {
    private static final float DEFAULT_RADIUS_DP = 3;
    private static final int DEFAULT_FILL_COLOR = 0xFFFFFFFF;
    private static final int DEFAULT_STROKE_COLOR = 0xFFDDDDDD;

    private final float mRadius;
    private final Paint mPaintStroke;
    private final Paint mPaintFill;
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mListener;
    private int mCurrentScroll;
    private int mFlowWidth;

    /**
     * Default constructor
     *
     * @param context
     */
    public CirclePageIndicator(Context context) {
        this(context, null);
    }

    /**
     * The constructor used with an inflater
     *
     * @param context
     * @param attrs
     */
    public CirclePageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        final float defaultRadius = DEFAULT_RADIUS_DP * density;

        // Retrieve styles attributs
        // 获取自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleFlowIndicator);

        // Retrieve the colors to be used for this view and apply them.
        final int fillColor = a.getColor(R.styleable.CircleFlowIndicator_fillColor, DEFAULT_FILL_COLOR);
        final int strokeColor = a.getColor(R.styleable.CircleFlowIndicator_strokeColor, DEFAULT_STROKE_COLOR);
        // Retrieve the mRadius
        mRadius = a.getDimension(R.styleable.CircleFlowIndicator_radius, defaultRadius);

        a.recycle();

        mPaintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintStroke.setStyle(Style.STROKE);
        mPaintStroke.setColor(strokeColor);
        mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintFill.setStyle(Style.FILL);
        mPaintFill.setColor(fillColor);
    }

    /*
     * (non-Javadoc)
     *	主要方法
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int count = (mViewPager != null && mViewPager.getAdapter() != null) ? mViewPager.getAdapter().getCount() : 1;
        // Draw stroked circles
        for (int iLoop = 0; iLoop < count; iLoop++) {
            canvas.drawCircle(getPaddingLeft() + mRadius
                    + (iLoop * (2 * mRadius + mRadius)),
                    getPaddingTop() + mRadius, mRadius, mPaintStroke);
        }
        float cx = 0;
        if (mFlowWidth != 0) {
            // Draw the filled circle according to the current scroll
            cx = (mCurrentScroll * (2 * mRadius + mRadius)) / mFlowWidth;
        }
        // The flow width has been updated yet. Draw the default position
        canvas.drawCircle(getPaddingLeft() + mRadius + cx,
                    getPaddingTop() + mRadius, mRadius, mPaintFill);

    }

    @Override
    public void setViewPager(ViewPager view) {
        mViewPager = view;
        mViewPager.setOnPageChangeListener(this);
        mFlowWidth = mViewPager.getWidth();
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mListener != null) {
            mListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentScroll = (position * mViewPager.getWidth()) + positionOffsetPixels;
        mFlowWidth = mViewPager.getWidth();
        invalidate();

        if (mListener != null) {
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (mListener != null) {
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }

    /*
     * (non-Javadoc)
     *实现onMeasure()方法基本需要完成下面三个方面的事情(最终结果是你自己写相应代码得出测量值并调用view的一个方法进行设置,
     *告诉给你的view安排位置大小的父容器你要多大的空间.):
     * @see android.view.View#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	/**
    	 * 1.传递进来的参数,widthMeasureSpec,和heightMeasureSpec是你对你应该得出来的测量值的限制.
    	 * 
    	 * 2. 你在onMeasure计算出来设置的width和height将被用来渲染组件.应当尽量在传递进来的width和height 声明之间.
虽然你也可以选择你设置的尺寸超过传递进来的声明.但是这样的话,父容器可以选择,如clipping,scrolling,或者抛出异常,或者(也许是用新的声明参数)再次调用onMeasure()
		3.一但width和height计算好了,就应该调用View.setMeasuredDimension(int width,int height)方法,否则将导致抛出异常.
    	 */
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec
     *            A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        // We were told how big to be
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        // Calculate the width according the views count
        else {
            int count = (mViewPager != null && mViewPager.getAdapter() != null) ? mViewPager.getAdapter().getCount() : 1;
            result = (int)(getPaddingLeft() + getPaddingRight()
                    + (count * 2 * mRadius) + (count - 1) * mRadius + 1);
            // Respect AT_MOST value if that was what is called for by
            // measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec
     *            A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
    	//一个MeasureSpec是一个大小跟模式的组合值.一共有三种模式.
    	/**
    	 * (1)UPSPECIFIED :父容器对于子容器没有任何限制,子容器想要多大就多大.
    	 * (2) EXACTLY 父容器已经为子容器设置了尺寸,子容器应当服从这些边界,不论子容器想要多大的空间.
    	 * (3) AT_MOST 子容器可以是声明大小内的任意大小.
    	 */
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        // We were told how big to be
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        // Measure the height
        else {
            result = (int)(2 * mRadius + getPaddingTop() + getPaddingBottom() + 1);
            // Respect AT_MOST value if that was what is called for by
            // measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Sets the fill color
     *
     * @param color
     *            ARGB value for the text
     */
    public void setFillColor(int color) {
        mPaintFill.setColor(color);
        invalidate();
    }

    /**
     * Sets the stroke color
     *
     * @param color
     *            ARGB value for the text
     */
    public void setStrokeColor(int color) {
        mPaintStroke.setColor(color);
        invalidate();
    }
}
