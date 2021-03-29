package com.shadow_zilot.stoksmonitoring.database;

public class DataSchema {
    public static final String NAME = "companies_base";

    public static class Cols {
        public static final String TICKER = "ticker";
        public static final String NAME = "name";
        public static final String PRICES_LIST = "pricesList";
        public static final String STOCK_PRICE = "stockPrice";
        public static final String DELTA_PRICE = "deltaPrice";
        public static final String CURRENCY_NAME = "currencyName";
        public static final String IS_FAVORITE = "isFavorite";
    }
}
