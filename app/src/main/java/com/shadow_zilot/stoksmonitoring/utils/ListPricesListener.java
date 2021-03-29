package com.shadow_zilot.stoksmonitoring.utils;

import com.shadow_zilot.stoksmonitoring.data_model.PriceShell;

import java.util.ArrayList;

public interface ListPricesListener {
    void updatePricesList(ArrayList<PriceShell> _newPricesList);
}
