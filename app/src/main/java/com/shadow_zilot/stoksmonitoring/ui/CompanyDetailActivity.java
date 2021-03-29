package com.shadow_zilot.stoksmonitoring.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import com.shadow_zilot.stoksmonitoring.data_model.Company;
import com.shadow_zilot.stoksmonitoring.views.PriceGraph;
import com.shadow_zilot.stoksmonitoring.data_model.PriceShell;
import com.shadow_zilot.stoksmonitoring.R;
import com.shadow_zilot.stoksmonitoring.database.CompaniesLab;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

public class CompanyDetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = CompanyDetailActivity.class.getSimpleName();
    private static final int DAY_ID = 0;
    private static final int WEEK_ID = 1;
    private static final int MONTH_ID = 2;
    public static final String sTICKER_ARG = "arg_ticker";

    private Company mCompany;
    private ImageButton mNavigateBack;
    private ImageButton mFavoriteBtn;
    private TextView mTickerView;
    private TextView mNameView;
    private TextView mPriceView;
    private TextView mDeltaPriceView;
    private TextView mDataView;
    private PriceGraph mPriceGraphic;
    private ViewGroup mDateSelectorContainer;
    private ArrayList<AppCompatButton> mSelectorList = new ArrayList<>();
    private ArrayList<PriceShell> mPriceShells = new ArrayList<>();
    private AppCompatButton mBuyButton;

    private ViewGroup mErrorContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String ticker = getIntent().getStringExtra(sTICKER_ARG);
        CompaniesLab database = CompaniesLab.get(this);
        mCompany = database.getCompanyByTicker(ticker);

        setContentView(R.layout.activity_company_detail);

        mNavigateBack = findViewById(R.id.navigate_back_btn);
        mNavigateBack.setOnClickListener(v -> finish());
        mFavoriteBtn = findViewById(R.id.detail_favorite_btn);
        mFavoriteBtn.setOnClickListener(v -> {
            mCompany.setIsFavorite(!mCompany.isFavorite());
            setFavoriteBack(mCompany.isFavorite());
            CompaniesLab.get(this).changeFavoriteState(mCompany.getTicker());
        });

        mTickerView = findViewById(R.id.ticker_text);
        mNameView = findViewById(R.id.company_name);
        mPriceView = findViewById(R.id.stock_price_view);
        mDeltaPriceView = findViewById(R.id.delta_price_view);
        mDataView = findViewById(R.id.data_view);

        mPriceGraphic = findViewById(R.id.price_graphic_view);
        mPriceGraphic.setPricesListener((_priceSelected, _previousPrice, _date) -> {
            String currencyPrefix = "$";
            initializePriceView(String.format("%1s %2.2f", currencyPrefix, _priceSelected));
            float delta = _priceSelected - _previousPrice;
            initializeDeltaView(delta, getDeltaColor(delta));
            mDataView.setText(getDataText(prepareAndGetCalendar(_date)));
        });

        mDateSelectorContainer = findViewById(R.id.date_selector_container);
        for (int i = 0; i < mDateSelectorContainer.getChildCount(); i++) {
            mSelectorList.add((AppCompatButton) mDateSelectorContainer.getChildAt(i));
            mSelectorList.get(i).setOnClickListener(view -> {
                changeSelectedDate((AppCompatButton) view);
            });
        }

        mBuyButton = findViewById(R.id.buy_button);

        database.requirePricesListByTicker(mCompany.getTicker());
        database.setPricesListListener(_newPricesList -> {
            if (_newPricesList.size() >= 2) {
                mPriceShells = _newPricesList;
                initializeGraphic(
                        prepareListForGraph(packPrimaryShellsList(_newPricesList),
                                getIdSelectedDate())
                );
            }
        });

        mErrorContainer = findViewById(R.id.errors_view_container);

        initializeDetail();
    }

    private GregorianCalendar prepareAndGetCalendar(Date _date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(_date.getTime() * 1000));
        return calendar;
    }

    private void initializeGraphic(ArrayList<PriceShell> listOfPrices) {
        Log.d(LOG_TAG, "Graphic is initialized!");

        mPriceGraphic.setPricesList(listOfPrices);

        mErrorContainer.setVisibility(View.GONE);
        mPriceGraphic.setVisibility(View.VISIBLE);
        mDateSelectorContainer.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofFloat(mPriceGraphic,
                "fadePercent", 1f, 0f);
        animator.setStartDelay(300);
        animator.setDuration(1700);
        animator.setInterpolator(new DecelerateInterpolator(0.7f));
        animator.start();
    }

    private void initializeDetail() {
        setFavoriteBack(mCompany.isFavorite());
        mNameView.setText(mCompany.getName());
        mTickerView.setText(mCompany.getTicker());
        initializePriceView(mCompany.getFormattedStockPrice());
        initializeDeltaView(mCompany.getDeltaPrice(), mCompany.getDeltaColor());
        mBuyButton.setText(String.format(getString(R.string.buy_text_format),
                mCompany.getFormattedStockPrice()));
    }

    private void changeSelectedDate(AppCompatButton _pressedBtn) {
        _pressedBtn.setBackgroundResource(R.drawable.active_date_back);
        _pressedBtn.setTextColor(getResources().getColor(R.color.white));

        for (AppCompatButton button: mSelectorList) {
            if (button != _pressedBtn) {
                button.setTextColor(getResources().getColor(R.color.black));
                button.setBackgroundResource(R.drawable.inactive_date_back);
            }
        }
        if (!mPriceShells.isEmpty()) {
            initializeGraphic(
                    prepareListForGraph(packPrimaryShellsList(mPriceShells),
                            getIdSelectedDate())
            );
        }
    }

    private ArrayList<ArrayList<PriceShell>> packPrimaryShellsList(
            ArrayList<PriceShell> primaryList) {
        ArrayList<ArrayList<PriceShell>> packingList = new ArrayList<>();
        packingList.add(new ArrayList<>());

        for (int i = 0; i < primaryList.size(); i++) {
            GregorianCalendar currentDate = prepareAndGetCalendar(primaryList.get(i).getData());
            if (i != 0) {
                GregorianCalendar previousDate = prepareAndGetCalendar(primaryList.get(i-1).getData());
                if (currentDate.get(Calendar.DAY_OF_YEAR) > previousDate.get(Calendar.DAY_OF_YEAR)) {
                    packingList.add(new ArrayList<>());
                }
            }
            packingList.get(packingList.size()-1).add(primaryList.get(i));
        }
        return packingList;
    }

    private ArrayList<PriceShell> prepareListForGraph(ArrayList<ArrayList<PriceShell>> packedList,
                                                      int selectorId) {
        ArrayList<PriceShell> result = new ArrayList<>();
        switch (selectorId) {
            case DAY_ID: {
                return packedList.get(packedList.size()-1);
            }
            case WEEK_ID: {
                for (int i = packedList.size()-1; i > packedList.size()-7; i--) {
                    result.add(packedList.get(i).get(0));
                    result.add(packedList.get(i).get(packedList.get(i).size()-1));
                }
                Collections.reverse(result);
                return result;
            }
            case MONTH_ID: {
                for (int i = 0; i < packedList.size(); i++) {
                    float pricesSum = 0;
                    float averagePrice;
                    for (int j = 0; j < packedList.get(i).size(); j++) {
                        pricesSum += packedList.get(i).get(j).getPrice();
                    }
                    averagePrice = pricesSum / packedList.get(i).size();
                    result.add(new PriceShell(averagePrice,
                            packedList.get(i).get(0).getData().getTime()));
                }
                return result;
            }
            default: return null;
        }
    }

    private int getIdSelectedDate() {
        for (int i = 0; i < mSelectorList.size(); i++) {
            int selectedColor = getResources().getColor(R.color.white);
            if (mSelectorList.get(i).getCurrentTextColor() == selectedColor) {
                return i;
            }
        }
        return -1;
    }

    private void setFavoriteBack(boolean _isFavorite) {
        if (_isFavorite) {
            mFavoriteBtn.setBackgroundResource(R.drawable.ic_favorite_active);
        } else {
            mFavoriteBtn.setBackgroundResource(R.drawable.ic_favorite);
        }
    }

    private void initializeDeltaView(float delta, int color) {
        String prefix;
        if (delta > 0) {
            prefix = "+$";
        } else if (delta < 0) {
            prefix = "-$";
        } else {
            prefix = "$";
        }
        mDeltaPriceView.setText(String.format("%1s%2.2f", prefix, Math.abs(delta)));
        mDeltaPriceView.setTextColor(getResources().getColor(color));
    }

    private void initializePriceView(String price) {
        mPriceView.setText(price);
    }

    private String getDataText(GregorianCalendar _data) {
        Log.d(LOG_TAG, "Begin time " + _data.getTime().getTime());
        if (getIdSelectedDate() == DAY_ID) {
            String minutes = "";
            if (_data.get(Calendar.MINUTE) == 0) {
                minutes = "00";
            } else {
                minutes = String.valueOf(_data.get(Calendar.MINUTE));
            }
            return String.format("%1d:%2s", _data.get(Calendar.HOUR_OF_DAY),
                    minutes);
        } else {
            String monthName =
                    getResources().getStringArray(R.array.month_names)[_data.get(Calendar.MONTH)];
            return String.format(getString(R.string.data_format),
                    _data.get(Calendar.DAY_OF_MONTH), monthName,
                    _data.get(Calendar.YEAR));
        }
    }

    public int getDeltaColor(float delta) {
        if (delta > 0) {
            return R.color.positive_delta_color;
        } else if (delta < 0) {
            return R.color.negative_delta_color;
        } else {
            return R.color.black;
        }
    }
}