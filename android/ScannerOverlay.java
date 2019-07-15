package org.cloudsky.cordovaPlugins;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
public class ScannerOverlay extends ViewGroup {
    private float left, top, endY;
    private int rectWidth, rectHeight;
    private int frames;
    private boolean revAnimation;
    private int lineWidth;

    public ScannerOverlay(Context context) {
        super(context);
    }

    public ScannerOverlay(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScannerOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        String package_name = context.getPackageName();
        Resources resources = context.getResources();
        resources.getIdentifier("string/ScannerOverlay", null, package_name);
        rectWidth=250;
        rectHeight=250;
        lineWidth=5;
        frames=6;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        left = (w - dpToPx(rectWidth)) / 2;
        top = (h - dpToPx(rectHeight)) / 2;
        endY = top;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw transparent rect
        int cornerRadius = 0;
        Paint eraser = new Paint();
        eraser.setAntiAlias(true);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));


        RectF rect = new RectF(left, top, dpToPx(rectWidth) + left, dpToPx(rectHeight) + top);
        canvas.drawRoundRect(rect, (float) cornerRadius, (float) cornerRadius, eraser);

        // draw horizontal line
        Paint line = new Paint();
        line.setColor(getResources().getColor(android.R.color.holo_orange_dark));
        line.setStrokeWidth(Float.valueOf(lineWidth));

        // draw the line to product animation
        if (endY >= top + dpToPx(rectHeight) + frames) {
            revAnimation = true;
        } else if (endY == top + frames) {
            revAnimation = false;
        }

        // check if the line has reached to bottom
        if (revAnimation) {
            endY -= frames;
        } else {
            endY += frames;
        }
        canvas.drawLine(left, endY, left + dpToPx(rectWidth), endY, line);
        invalidate();
    }
}