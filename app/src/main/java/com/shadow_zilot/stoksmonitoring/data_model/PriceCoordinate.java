package com.shadow_zilot.stoksmonitoring.data_model;

import java.util.Date;

import static com.shadow_zilot.stoksmonitoring.views.PriceGraph.sBIG_POINTER_RADIUS;

public class PriceCoordinate implements Comparable<PriceCoordinate> {
    private float mX;
    private float mY;
    private float mPrice;
    private Date mData;

    public PriceCoordinate(PriceShell _priceShell) {
        this(-1, -1, _priceShell.getPrice(), _priceShell.getData().getTime());
    }

    public PriceCoordinate(float _x, float _y, float _price, long _data) {
        mX = _x;
        mY = _y;
        mPrice = _price;
        mData = new Date(_data);
    }

    public Date getData() {
        return mData;
    }

    public float getX(int width, int listSize, int index) {
        if (mX != -1) {
            return mX;
        } else {
            float deltaX = (float) width / (listSize-1);
            mX = (deltaX*index);
        }
        return mX;
    }

    public float getY(int height, float maxPrice, float minPrice) {
        if (mY != -1) {
            return mY;
        } else {
            float deltaPointer = sBIG_POINTER_RADIUS / 2f;
            float priceHeightPercent =
                    ((mPrice - minPrice) * 100) / (maxPrice - minPrice) / 100f;
            height -= deltaPointer;
            mY = (height) - ((height * priceHeightPercent) - deltaPointer);
        }
        return mY;
    }

    public float getPrice() {
        return mPrice;
    }

    @Override
    public int compareTo(PriceCoordinate o) {
        if (mPrice < o.mPrice) {
            return -1;
        }
        if (mPrice > o.mPrice) {
            return 1;
        } else {
            return 0;
        }
    }
}
