package com.shadow_zilot.stoksmonitoring.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.shadow_zilot.stoksmonitoring.data_model.PriceCoordinate;
import com.shadow_zilot.stoksmonitoring.data_model.PriceShell;
import com.shadow_zilot.stoksmonitoring.R;
import com.shadow_zilot.stoksmonitoring.utils.PricesListener;

import java.util.ArrayList;
import java.util.Collections;

public class PriceGraph extends View {
    private static final String LOG_TAG = PriceGraph.class.getSimpleName();

    private static final int sDEFAULT_STROKE_COLOR = Color.BLACK;
    private static final int sDEFAULT_STROKE_WIDTH = 5;
    public static final int sBIG_POINTER_RADIUS = 10;
    private static final int sSMALL_POINTER_RADIUS = 8;

    private final int mStrokeColor;
    private final int mStrokeWidth;
    private final int mFadeColor;
    private final int mShadowColor;
    private final int mPointerColor;

    private PricesListener mPricesListener;

    private float mPointerX = 10;
    private float mFadePercent;
    private boolean mIsPointerVisible = false;

    private final ArrayList<PriceCoordinate> mGraphPoints = new ArrayList<>();
    private final ArrayList<Float> mGraphY = new ArrayList<>();

    private Paint mGraphPaint;
    private Paint mFadePaint;
    private Paint mMaskPaint;
    private Paint mPointerPaint;
    private Paint mCirclePointerPaint;

    private Bitmap mShadowBitmap;
    private Bitmap mMaskBitmap;
    private Bitmap mGraphBitmap;
    private Path mGraphPath;
    private Path mPointerPath;
    private RectF mFadeRect;

    public PriceGraph(Context context) {
        this(context, null);
    }

    public PriceGraph(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray attrs = getContext().getTheme().obtainStyledAttributes(attributeSet,
                R.styleable.PriceGraph, 0, 0);

        try {
            mStrokeWidth = attrs.getDimensionPixelSize(R.styleable.PriceGraph_pg_strokeWidth,
                    sDEFAULT_STROKE_WIDTH);
            mStrokeColor = attrs.getColor(R.styleable.PriceGraph_pg_strokeColor,
                    sDEFAULT_STROKE_COLOR);
            mFadePercent = attrs.getFloat(R.styleable.PriceGraph_pg_fadePercent, 1f);
            mFadeColor = attrs.getColor(R.styleable.PriceGraph_pg_fadeColor, Color.WHITE);
            mShadowColor = attrs.getColor(R.styleable.PriceGraph_pg_shadowColor, Color.BLACK);
            mPointerColor = attrs.getColor(R.styleable.PriceGraph_pg_pointerColor, Color.YELLOW);
        } finally {
            initPainters();
            attrs.recycle();
        }
    }

    private void initPainters() {
        mGraphPaint = new Paint();
        mGraphPaint.setColor(mStrokeColor);
        mGraphPaint.setStyle(Paint.Style.STROKE);
        mGraphPaint.setStrokeWidth(mStrokeWidth);
        mGraphPaint.setPathEffect(new CornerPathEffect(64));
        mGraphPaint.setAntiAlias(true);

        mPointerPaint = new Paint();
        mPointerPaint.setColor(mPointerColor);
        mPointerPaint.setStyle(Paint.Style.FILL);
        mPointerPaint.setPathEffect(new CornerPathEffect(4));
        mPointerPaint.setAntiAlias(true);

        mCirclePointerPaint = new Paint();
        mCirclePointerPaint.setStyle(Paint.Style.FILL);
        mCirclePointerPaint.setColor(mPointerColor);
        mCirclePointerPaint.setAntiAlias(true);

        mFadePaint = new Paint();
        mFadePaint.setStyle(Paint.Style.FILL);
        mFadePaint.setColor(mFadeColor);

        mMaskPaint = new Paint();
        mMaskPaint.setPathEffect(new CornerPathEffect(64));
        mMaskPaint.setStyle(Paint.Style.FILL);
        mMaskPaint.setColor(mFadeColor);
        mMaskPaint.setAntiAlias(true);

        mGraphPath = new Path();
        mPointerPath = new Path();
        mFadeRect = new RectF();
    }

    public void setPricesListener(PricesListener _listener) {
        mPricesListener = _listener;
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        Log.d(LOG_TAG, "Graphic is measured!");
        int width = resolveSize(250, widthSpec);
        int height = resolveSize(100, heightSpec);

        setMeasuredDimension(width, height);
        if (mShadowBitmap == null) {
            initializeBackBitmap(width, height);
        }
        mPointerX = width - (sBIG_POINTER_RADIUS/2f);
    }

    private void initializeMaskBitmap(int width, int height) {
        if (mMaskBitmap == null && !mGraphPoints.isEmpty()) {
            mMaskBitmap = Bitmap.createBitmap(width + 48, height + 48, null);
            Canvas canvas = new Canvas();
            canvas.setBitmap(mMaskBitmap);

            float maxPrice = Collections.max(mGraphPoints).getPrice();
            float minPrice = Collections.min(mGraphPoints).getPrice();
            int listSize = mGraphPoints.size();

            mGraphPath.reset();
            float tmpX = mGraphPoints.get(0).getX(width, listSize, 0);
            float tmpY = mGraphPoints.get(0).getY(height, maxPrice, minPrice);

            mGraphPath.moveTo(tmpX, tmpY);
            for (int i = 0; i < mGraphPoints.size(); i++) {
                tmpX = mGraphPoints.get(i).getX(width, listSize, i);
                tmpY = mGraphPoints.get(i).getY(height, maxPrice, minPrice);
                mGraphPath.lineTo(tmpX, tmpY);
            }
            initializeGraphBitmap(mGraphPath, width, height);

            mGraphPath.lineTo(width + 48, -48);
            mGraphPath.lineTo(0, -48);
            tmpX = mGraphPoints.get(0).getX(width, listSize, 0);
            tmpY = mGraphPoints.get(0).getY(height, maxPrice, minPrice);
            mGraphPath.lineTo(tmpX, tmpY);

            canvas.drawPath(mGraphPath, mMaskPaint);
        }
    }

    private void initializeGraphBitmap(Path _graphPath, int _width, int _height) {
        if (mGraphBitmap == null) {
            mGraphY.clear();
            mGraphBitmap = Bitmap.createBitmap(_width, _height, null);
            Canvas canvas = new Canvas();
            canvas.setBitmap(mGraphBitmap);

            canvas.drawPath(_graphPath, mGraphPaint);

            for (int x = 0; x < _width; x++) {
                for (int y = 0; y < _height; y++) {
                    if (mGraphBitmap.getPixel(x, y) == mStrokeColor) {
                        mGraphY.add((float) y);
                        break;
                    }
                }
            }
            Log.d(LOG_TAG, String.valueOf(mGraphY.size()));
        }
    }

    private void initializeBackBitmap(int width, int height) {
        mShadowBitmap = Bitmap.createBitmap(width, height, null);
        int[] colors = new int[width*height];

        int alpha = 150;
        int color = setAlphaColor(mShadowColor, alpha);
        int delta = (int) Math.ceil(width / (float) alpha) + 1;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = (x*height) + y;
                colors[index] = color;
            }
            if (x % delta == 0) {
                alpha--;
                color = setAlphaColor(mShadowColor, alpha);
            }
        }
        mShadowBitmap.setPixels(colors, 0, width, 0,0, width, height);
    }

    private int setAlphaColor(int color, int alpha) {
        StringBuilder hexColorString = new StringBuilder(Integer.toHexString(color));
        if (alpha < 0) {
            alpha = 0;
        }
        String hexAlphaString = Integer.toHexString(alpha);
        if (hexAlphaString.length() == 1) {
            hexColorString.replace(0, 2, "#0" + hexAlphaString);
        } else {
            hexColorString.replace(0, 2, "#" + hexAlphaString);
        }
        return Color.parseColor(hexColorString.toString());
    }

    @Override
    public void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        initializeMaskBitmap(width, height);

        canvas.drawBitmap(mShadowBitmap, 0, 0, null);
        if (!mGraphPoints.isEmpty()) {
            canvas.drawBitmap(mMaskBitmap, 0, 0, null);
            canvas.drawBitmap(mGraphBitmap, 0, 0, null);

            if (mIsPointerVisible) {
                mPointerPath.reset();
                mPointerPath.moveTo(mPointerX - 10, 0);
                mPointerPath.lineTo(mPointerX - 2, 10);
                mPointerPath.lineTo(mPointerX - 2, height - 10);
                mPointerPath.lineTo(mPointerX - 10, height);
                mPointerPath.lineTo(mPointerX + 10, height);
                mPointerPath.lineTo(mPointerX + 2, height - 10);
                mPointerPath.lineTo(mPointerX + 2, 10);
                mPointerPath.lineTo(mPointerX + 10, 0);
                mPointerPath.lineTo(mPointerX - 10, 0);
                canvas.drawPath(mPointerPath, mPointerPaint);
            }
            if ((int) mPointerX >= mGraphY.size()) {
                mPointerX = mGraphY.size()-1;
            }
            mCirclePointerPaint.setColor(mFadeColor);
            canvas.drawCircle(mPointerX, mGraphY.get((int) mPointerX),
                    sBIG_POINTER_RADIUS, mCirclePointerPaint);
            mCirclePointerPaint.setColor(mPointerColor);
            canvas.drawCircle(mPointerX, mGraphY.get((int) mPointerX),
                    sSMALL_POINTER_RADIUS, mCirclePointerPaint);
        }

        mFadeRect.set(getWidth()-(getWidth()*mFadePercent), 0, getWidth(), getHeight());
        canvas.drawRect(mFadeRect, mFadePaint);
    }

    private float determinePointerPosition(float x) {
        float hXRange = 0;
        float lXRange = 0;
        int size = mGraphPoints.size();
        for (int i = 0; i < mGraphPoints.size()-1; i++) {
            lXRange = mGraphPoints.get(i).getX(getWidth(), size, i);
            hXRange = mGraphPoints.get(i+1).getX(getWidth(), size, i+1);
            if ((x < hXRange) && (x > lXRange)) {
                break;
            }
        }
        float middle = ((hXRange - lXRange)/2)+lXRange;
        if (x >= middle) {
            return hXRange;
        } else {
            return lXRange;
        }
    }

    private int getPriceIndexByX(float x) {
        x = (int) x;
        float searchX;
        int size = mGraphPoints.size();
        for (int i = 0; i < mGraphPoints.size(); i++) {
            searchX = (int) mGraphPoints.get(i).getX(getWidth(), size, i);
            if (x == searchX) {
                return i;
            }
        }
        return -1;
    }

    private void noticePriceListener(float _x) {
        int index = getPriceIndexByX(_x);
        float selectedPrice = mGraphPoints.get(index).getPrice();
        float previousPrice = 0;
        if (index != 0) {
            previousPrice = mGraphPoints.get(index - 1).getPrice();
        }
        mPricesListener.onPriceSelected(selectedPrice, previousPrice,
                mGraphPoints.get(index).getData());
    }

    @Override
    public boolean onTouchEvent(MotionEvent _event) {
        float x = _event.getX();
        if (_event.getAction() == MotionEvent.ACTION_DOWN) {
            if ((x <= mPointerX + 30) && (x >= mPointerX - 30)) {
                mIsPointerVisible = true;
            }
        }
        if (mIsPointerVisible && _event.getAction() == MotionEvent.ACTION_MOVE) {
            mPointerX = x;
            float tmpX = determinePointerPosition(x);
            noticePriceListener(tmpX);
        }
        if (_event.getAction() == MotionEvent.ACTION_UP && mIsPointerVisible) {
            mIsPointerVisible = false;
            mPointerX = determinePointerPosition(x);
            noticePriceListener(mPointerX);
        }

        if (mPointerX == getWidth()) {
            mPointerX -= (sBIG_POINTER_RADIUS / 2f);
        }
        if (mPointerX == 0) {
            mPointerX += (sBIG_POINTER_RADIUS / 2f);
        }

        invalidate();

        return true;
    }

    public void setPricesList(ArrayList<PriceShell> _listOfPrices) {
        mGraphPoints.clear();
        for (int i = 0; i < _listOfPrices.size(); i++) {
            mGraphPoints.add(new PriceCoordinate(_listOfPrices.get(i)));
        }
        mMaskBitmap = null;
        mGraphBitmap = null;
        mPointerX = getWidth() - (sBIG_POINTER_RADIUS / 2f);
        mFadePercent = 1;
        mPricesListener.onPriceSelected(
                _listOfPrices.get(_listOfPrices.size()-1).getPrice(),
                _listOfPrices.get(_listOfPrices.size()-2).getPrice(),
                _listOfPrices.get(_listOfPrices.size()-1).getData());
        invalidate();
    }

    public void setFadePercent(float _fadePercent) {
        mFadePercent = _fadePercent;
        invalidate();
    }
}
