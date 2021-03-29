package com.shadow_zilot.stoksmonitoring.utils;

import java.util.Date;

public interface PricesListener {
    void onPriceSelected(float _priceSelected, float _previousPrice, Date _date);
}
