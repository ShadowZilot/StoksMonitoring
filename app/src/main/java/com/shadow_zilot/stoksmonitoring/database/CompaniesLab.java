package com.shadow_zilot.stoksmonitoring.database;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.shadow_zilot.stoksmonitoring.data_model.Company;
import com.shadow_zilot.stoksmonitoring.utils.ListPricesListener;
import com.shadow_zilot.stoksmonitoring.data_model.PriceShell;
import com.shadow_zilot.stoksmonitoring.R;
import com.shadow_zilot.stoksmonitoring.utils.StocksObservable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static com.shadow_zilot.stoksmonitoring.database.DataSchema.*;
import static com.shadow_zilot.stoksmonitoring.database.DataSchema.NAME;

public class CompaniesLab {
    public static final int OK_RESULT = 0;
    public static final int EMPTY_RESULT = 1;

    private static final String sApiKey = "2xFvfhoFk3HsLXSkZmdvtXKW9l1xsLaZftZdJ9bQUoheIyOHjg0Btfba2imB";
    private static final String sStringTickerList =
            "AAPL,MSFT,AMZN,FB,JPM,GOOG,GOOGL,XOM,BAC,WFC,INTC," +
                    "T,V,CSCO,CVX,UNH,PFE,HD,PG,VZ,C,ABBV," +
                    "BA,KO,CMCSA,MA,PM,DWDP,PEP,ORCL,DIS,MRK," +
                    "NVDA,MMM,AMGN,IBM,NFLX,WMT,MO,MCD,GE,HON,MDT," +
                    "ABT,TXN,BMY,ADBE,UNP";

    private static final String LOG_TAG = CompaniesLab.class.getSimpleName();
    private static CompaniesLab sInstance = null;
    private Context mContext = null;
    private SQLiteDatabase mDataBase;

    private String mCurrentSearchQuery;

    private StocksObservable mFavoriteObserver;
    private StocksObservable mCommonObserver;
    private ListPricesListener mPricesListener;

    private ArrayList<Company> mListOfCompanies = new ArrayList<>();

    private CompaniesLab(Context _context) {
        mContext = _context.getApplicationContext();
        mDataBase = new DataBaseHelper(_context).getWritableDatabase();
        executeRequest();
    }

    private void executeRequest() {
        AsyncRequest request = new AsyncRequest();
        request.execute(sStringTickerList, sApiKey);
    }

    public void updateUI() {
        try {
            mCommonObserver.observeUpdates(getCommonList(mCurrentSearchQuery));
            mFavoriteObserver.observeUpdates(getFavoriteList(mCurrentSearchQuery));
        } catch (NullPointerException e) {
            Log.d(LOG_TAG, "Observers not set!");
        }
    }

    public static CompaniesLab get(Context _context) {
        if (sInstance == null) {
            sInstance = new CompaniesLab(_context);
        }
        return sInstance;
    }

    public void addFavoriteObserver(StocksObservable _observer) {
        mFavoriteObserver = _observer;
    }

    public void addCommonObserver(StocksObservable _observer) {
        mCommonObserver = _observer;
    }

    public void setPricesListListener(ListPricesListener _listener) {
        mPricesListener = _listener;
    }

    public void resetSearchQuery() {
        mCurrentSearchQuery = null;
    }

    public void performSearch(String searchQuery) {
        mCurrentSearchQuery = searchQuery;
        mFavoriteObserver.observeUpdates(getFavoriteList(searchQuery));
        mCommonObserver.observeUpdates(getCommonList(searchQuery));
    }

    /**
     * This method load companies data from JSON string
     * or if it null load old data about companies
     *
     * @param _json string of JSON file which contains data about companies
     */
    private void requireCompanies(String _json) {
        Company[] array = new Gson().fromJson(_json, Company[].class);
        if (array == null) {
            Toast.makeText(mContext,
                    mContext.getString(R.string.old_data_message), Toast.LENGTH_LONG).show();
            mListOfCompanies = loadFromDataBase();
        } else {
            Log.d(LOG_TAG, array[0].toString());
            updateAll(array);
        }

        updateUI();
    }

    /**
     * @return List of companies loaded from database
     */
    private ArrayList<Company> loadFromDataBase() {
        return loadFromDataBase(null);
    }

    private ArrayList<Company> loadFromDataBase(String searchQuery) {
        Cursor cursor;
        if (searchQuery == null || searchQuery.equals("")) {
            cursor = mDataBase.query(NAME,
                    null,
                    null,
                    null,
                    null,
                    null, null);
        } else {
            searchQuery += "*";
            cursor = mDataBase.query(NAME,
                    null,
                    Cols.NAME + " MATCH ?",
                    new String[]{searchQuery},
                    null,
                    null, null);
        }

        ArrayList<Company> list = new ArrayList<>();
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                while (!cursor.isLast()) {
                    list.add(CompanyValue.getCompanyFromCursor(cursor));
                    cursor.moveToNext();
                }
                list.add(CompanyValue.getCompanyFromCursor(cursor));
                cursor.close();
                return list;
            } catch (CursorIndexOutOfBoundsException | IllegalStateException e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    private void updateAll(Company[] _listOfCompanies) {
        for (int i = 0; i < _listOfCompanies.length; i++) {
            CompanyValue value = new CompanyValue(_listOfCompanies[i]);
            String ticker = _listOfCompanies[i].getTicker();
            try {
                Company primaryCompany = getCompanyByTicker(ticker);
                value.changeFavoriteState(primaryCompany.isFavorite());
                value.setOldPricesList(primaryCompany.getPricesSaveString());
            } catch (CursorIndexOutOfBoundsException e) {
                e.printStackTrace();
            } finally {
                updateCompanyData(value, ticker);
                mListOfCompanies.add(_listOfCompanies[i]);
            }
        }
    }

    private void updateCompanyData(CompanyValue value, String _ticker) {
        int resultCode = mDataBase.update(NAME, value.getContentValue(),
                Cols.TICKER + " = ?", new String[]{_ticker});
        if (resultCode == 0) {
            mDataBase.insert(NAME, null, value.getContentValue());
        }
    }

    public Company getCompanyByTicker(String _ticker) {
        Cursor cursor = mDataBase.query(NAME,
                null,
                Cols.TICKER + " = ?",
                new String[]{_ticker},
                null,
                null,
                null);
        cursor.moveToFirst();
        Company result = CompanyValue.getCompanyFromCursor(cursor);
        cursor.close();
        return result;
    }

    public void requirePricesListByTicker(String ticker) {
        HistoryRequest request = new HistoryRequest(ticker);
        request.execute(ticker, sApiKey, "30m");
    }

    private ArrayList<Company> getFavoriteList() {
        return getFavoriteList(null);
    }

    private ArrayList<Company> getFavoriteList(String searchQuery) {
        Cursor cursor;
        if (searchQuery == null || searchQuery.equals("")) {
            cursor = mDataBase.query(NAME,
                    null,
                    null,
                    null,
                    null,
                    null, null);
        } else {
            searchQuery += "*";
            cursor = mDataBase.query(NAME,
                    null,
                    Cols.NAME + " MATCH ?",
                    new String[]{searchQuery},
                    null,
                    null, null);
        }

        ArrayList<Company> list = new ArrayList<>();
        if (cursor != null) {
            Company tmp;
            try {
                cursor.moveToFirst();
                while (!cursor.isLast()) {
                    tmp = CompanyValue.getCompanyFromCursor(cursor);
                    if (tmp.isFavorite()) {
                        list.add(tmp);
                    }
                    cursor.moveToNext();
                }
                tmp = CompanyValue.getCompanyFromCursor(cursor);
                if (tmp.isFavorite()) {
                    list.add(tmp);
                }
                cursor.close();
                return list;
            } catch (CursorIndexOutOfBoundsException e) {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    private ArrayList<Company> getCommonList(String searchQuery) {
        return loadFromDataBase(searchQuery);
    }

    private ArrayList<Company> getCommonList() {
        return getCommonList(null);
    }

    public void changeFavoriteState(String _ticker) {
        Cursor cursor = mDataBase.query(NAME,
                null,
                Cols.TICKER + " = ?",
                new String[]{_ticker},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        Company company = CompanyValue.getCompanyFromCursor(cursor);
        CompanyValue value = new CompanyValue(company);
        company.setIsFavorite(!company.isFavorite());
        mDataBase.update(NAME, value.getContentValue(), Cols.TICKER + " LIKE ?",
                new String[]{_ticker});
        cursor.close();

        updateUI();
    }

    private class HistoryRequest extends AsyncTask<String, Integer, String> {
        String mTicker;

        public HistoryRequest(String ticker) {
            super();
            mTicker = ticker;
        }

        @Override
        protected String doInBackground(String... args) {
            return doRequest(args);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ArrayList<PriceShell> pricesList = new ArrayList<>();
            if (s != null) {
                try {
                    JSONObject object = new JSONObject(s);
                    JSONObject items = object.getJSONObject("items");
                    JSONArray array = items.names();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject priceData = items.getJSONObject(array.getString(i));
                        long data = Integer.parseInt(array.getString(i));
                        float highPrice = (float) priceData.getDouble("high");
                        float lowPrice = (float) priceData.getDouble("low");
                        pricesList.add(new PriceShell(highPrice, lowPrice, data));
                    }

                    Company oldCompany = getCompanyByTicker(mTicker);
                    oldCompany.setPricesList(pricesList);
                    updateCompanyData(new CompanyValue(oldCompany), mTicker);

                } catch (JSONException _e) {
                    _e.printStackTrace();
                }
            } else {
                Company currentCompany = getCompanyByTicker(mTicker);
                if (currentCompany.getPricesList().size() >= 2) {
             /*       Toast.makeText(mContext,
                            R.string.old_graph_data_message, Toast.LENGTH_LONG).show();*/
                }
                mPricesListener.updatePricesList(currentCompany.getPricesList());
            }

            mPricesListener.updatePricesList(pricesList);
        }
    }

    private String doRequest(String... args) {
        try {
            String query = null;
            if (args.length == 2) {
                query = String.format(String.format("https://mboum.com/api/v1/qu/quote/?symbol=%1s&apikey=%2s",
                        args[0], args[1]));
            } else if (args.length == 3) {
                query = String.format(String.format("https://mboum.com/api/v1/hi/history/?symbol=%1s&interval=%2s&apikey=%3s",
                        args[0], args[2], args[1]));
            }
            URL url = new URL(query);
            URLConnection connection = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            StringBuilder urlString = new StringBuilder();
            String current;

            while ((current = in.readLine()) != null) {
                urlString.append(current);
            }

            return urlString.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class AsyncRequest extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... args) {
            return doRequest(args);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            requireCompanies(s);
        }
    }
}
