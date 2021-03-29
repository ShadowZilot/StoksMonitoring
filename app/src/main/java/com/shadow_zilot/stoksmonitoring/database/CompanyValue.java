package com.shadow_zilot.stoksmonitoring.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.shadow_zilot.stoksmonitoring.data_model.Company;

import static com.shadow_zilot.stoksmonitoring.database.DataSchema.Cols.CURRENCY_NAME;
import static com.shadow_zilot.stoksmonitoring.database.DataSchema.Cols.DELTA_PRICE;
import static com.shadow_zilot.stoksmonitoring.database.DataSchema.Cols.IS_FAVORITE;
import static com.shadow_zilot.stoksmonitoring.database.DataSchema.Cols.NAME;
import static com.shadow_zilot.stoksmonitoring.database.DataSchema.Cols.PRICES_LIST;
import static com.shadow_zilot.stoksmonitoring.database.DataSchema.Cols.STOCK_PRICE;
import static com.shadow_zilot.stoksmonitoring.database.DataSchema.Cols.TICKER;

public class CompanyValue {
    private Company mCompany;

    public CompanyValue(Company company) {
        mCompany = company;
    }

    public void changeFavoriteState(boolean _favoriteState) {
        if (mCompany.isFavorite() != _favoriteState) {
            mCompany.setIsFavorite(!mCompany.isFavorite());
        }
    }

    public static Company getCompanyFromCursor(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(NAME));
        String ticker = cursor.getString(cursor.getColumnIndex(TICKER));
        String savedPricesList = cursor.getString(cursor.getColumnIndex(PRICES_LIST));
        float stockPrice = cursor.getFloat(cursor.getColumnIndex(STOCK_PRICE));
        float deltaPrice = cursor.getFloat(cursor.getColumnIndex(DELTA_PRICE));
        String currencyName = cursor.getString(cursor.getColumnIndex(CURRENCY_NAME));
        boolean isFavorite = cursor.getInt(cursor.getColumnIndex(IS_FAVORITE)) > 0;
        return new Company(ticker, name, savedPricesList, stockPrice,
                deltaPrice, currencyName, isFavorite);
    }

    public ContentValues getContentValue() {
        ContentValues value = new ContentValues();
        value.put(TICKER, mCompany.getTicker());
        value.put(NAME, mCompany.getName());
        value.put(PRICES_LIST, mCompany.getPricesSaveString());
        value.put(STOCK_PRICE, mCompany.getStockPrice());
        value.put(DELTA_PRICE, mCompany.getDeltaPrice());
        value.put(CURRENCY_NAME, mCompany.getCurrencyName());
        value.put(IS_FAVORITE, mCompany.isFavorite());
        return value;
    }

    public void setOldPricesList(String _oldPrices) {
        mCompany.setPricesList(_oldPrices);
    }
}
