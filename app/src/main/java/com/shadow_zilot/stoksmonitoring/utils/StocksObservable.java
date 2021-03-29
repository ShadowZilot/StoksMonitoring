package com.shadow_zilot.stoksmonitoring.utils;

import com.shadow_zilot.stoksmonitoring.data_model.Company;

import java.util.ArrayList;

public interface StocksObservable {
    void observeUpdates(ArrayList<Company> _companies);
}
