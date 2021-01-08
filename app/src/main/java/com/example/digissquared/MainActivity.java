package com.example.digissquared;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.api.ApiClient;
import com.example.api.ApiServices;
import com.example.models.DataModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    ProgressBar mRSRPProgress, mRSRQProgress, mSNRProgress;
    TextView mTvRSRPValue, mTvRSRQValue, mTvSNRValue;
    ArrayList<HashMap<String, String>> SNIR, RSRP, RSRQ;
    int receivedRSRP, receivedRSRQ, receivedSNIR;
    TextView tvRSRP, tvRSRQ, tvSNIR;
    String rsrpColor, rsrqColor, snirColor;
    private LineChart lineChart;
    ArrayList<Entry> rsrpArray, rsrqArray, snirArray;
    Float rsrp, rsrq, snir, timer = 0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initEvents();
        initViews();


    }

    public void GetData() {
        ApiServices apiServices = ApiClient.getClientSSL().create(ApiServices.class);
        Call<DataModel> mGetData = apiServices.getData();
        mGetData.enqueue(new Callback<DataModel>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<DataModel> call, Response<DataModel> response) {
                Log.d("TAG", "onResponse: " + response.body().getRSRP());
                receivedRSRP = response.body().getRSRP();
                receivedRSRQ = response.body().getRSRQ();
                receivedSNIR = response.body().getSINR();
                rsrpColor = setColor(receivedRSRP, RSRP);
                rsrqColor = setColor(receivedRSRQ, RSRQ);
                snirColor = setColor(receivedSNIR, SNIR);

                Drawable mDrawable = mRSRPProgress.getProgressDrawable();
                mDrawable.setColorFilter(Color.parseColor(rsrpColor), PorterDuff.Mode.MULTIPLY);

                mRSRPProgress.setProgressDrawable(mDrawable);
                tvRSRP.setText(String.valueOf(receivedRSRP));
                mRSRPProgress.setProgress(receivedRSRP);


                Drawable mDrawable2 = mRSRQProgress.getProgressDrawable();
                mDrawable2.setColorFilter(Color.parseColor(rsrqColor), PorterDuff.Mode.MULTIPLY);
                mRSRQProgress.setProgressDrawable(mDrawable2);

                tvRSRQ.setText(String.valueOf(receivedRSRQ));
                mRSRQProgress.setProgress(receivedRSRQ);

                Drawable mDrawable3 = mSNRProgress.getProgressDrawable();
                mDrawable3.setColorFilter(Color.parseColor(snirColor), PorterDuff.Mode.MULTIPLY);
                mSNRProgress.setProgressDrawable(mDrawable3);
                tvSNIR.setText(String.valueOf(receivedSNIR));
                mSNRProgress.setProgress(receivedSNIR);

                rsrp = (float) response.body().getRSRP();
                rsrq = (float) response.body().getRSRQ();
                snir = (float) response.body().getSINR();
                Log.d("size", "onResponse: " + snir + ":" + rsrq + ":" + rsrp);
                drawLine(rsrp, rsrq, snir);


            }

            @Override
            public void onFailure(Call<DataModel> call, Throwable t) {
                Log.d("TAG", "onResponse: " + t.getMessage());
            }
        });

    }

    private void initViews() {
        rsrpArray = new ArrayList<>();
        rsrqArray = new ArrayList<>();
        snirArray = new ArrayList<>();

        lineChart = findViewById(R.id.activity_main_linechart);
        XAxis xAxis = lineChart.getXAxis();
        XAxis.XAxisPosition position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setPosition(position);
        xAxis.setDrawLimitLinesBehindData(true);

        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("mm ss", Locale.ENGLISH);

            @Override
            public String getFormattedValue(float value) {
//                long millis = (long) value * 2000L;
                return mFormat.format(new Date());
            }
        });



        tvRSRP = findViewById(R.id.rsrp_value);
        tvRSRQ = findViewById(R.id.rsrq_value);
        tvSNIR = findViewById(R.id.snr_value);
        SNIR = new ArrayList<>();
        RSRP = new ArrayList<>();
        RSRQ = new ArrayList<>();
        mRSRPProgress = findViewById(R.id.rsrp_progress);
        mRSRQProgress = findViewById(R.id.rsrq_progress);
        mSNRProgress = findViewById(R.id.snr_progress);

        mTvRSRPValue = findViewById(R.id.rsrp_value);
        mTvRSRQValue = findViewById(R.id.rsrq_value);
        mTvSNRValue = findViewById(R.id.snr_value);
        generateArray("SINR", SNIR);
        generateArray("RSRP", RSRP);
        generateArray("RSRQ", RSRQ);


    }

    private void initEvents() {

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GetData();
                timer++;
                configureLineChart();
                ;
            }
        }, 0, 2000);

    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("Legend.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void generateArray(String name, ArrayList<HashMap<String, String>> array) {
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONArray m_jArry = obj.getJSONArray(name);
            HashMap<String, String> arrayData;

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                String from = jo_inside.getString("From");
                String to = jo_inside.getString("To");
                String color = jo_inside.getString("Color");

                //Add your values in your `ArrayList` as below:
                arrayData = new HashMap<String, String>();
                arrayData.put("From", from);
                arrayData.put("To", to);
                arrayData.put("Color", color);

                array.add(arrayData);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public String setColor(Integer value, ArrayList<HashMap<String, String>> valuesArr) {
        String color = null;
        for (int i = 0; i < valuesArr.size(); i++) {
            if (i != 0 && i != valuesArr.size() - 1) {
                Double from = Double.parseDouble(valuesArr.get(i).get("From"));
                Double to = Double.parseDouble(valuesArr.get(i).get("To"));
                color = valuesArr.get(i).get("Color");
                if (value >= from && value < to) {
                    break;
                }

            } else if (i == 0 && value < Double.parseDouble(valuesArr.get(i).get("To"))) {
                color = valuesArr.get(i).get("Color");
                break;
            } else if (i == valuesArr.size() - 1 && value > Double.parseDouble(valuesArr.get(i).get("From"))) {
                color = valuesArr.get(i).get("Color");
                break;
            }
        }

        return color;
    }

    private void configureLineChart() {
        Description desc = new Description();
        desc.setText("DIGIS Squared");
        desc.setTextSize(28);
        lineChart.setDescription(desc);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);

            }

    private void setLineChartData(ArrayList<Entry> rsrpArr, ArrayList<Entry> rsrqArr, ArrayList<Entry> snirArr) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        LineDataSet rsrpLine = new LineDataSet(rsrpArr, "RSRP");
        rsrpLine.setDrawCircles(true);
        rsrpLine.setCircleRadius(2);
        rsrpLine.setDrawValues(false);
        rsrpLine.setLineWidth(1);
        rsrpLine.setColor(Color.GREEN);
        rsrpLine.setCircleColor(Color.GREEN);
        rsrpLine.notifyDataSetChanged();
        dataSets.add(rsrpLine);

        LineDataSet rsrqLine = new LineDataSet(rsrqArr, " RSRQ");
        rsrqLine.setDrawCircles(true);
        rsrqLine.setCircleRadius(2);
        rsrqLine.setDrawValues(false);
        rsrqLine.setLineWidth(1);
        rsrqLine.setColor(Color.RED);
        rsrqLine.setCircleColor(Color.RED);
        rsrqLine.notifyDataSetChanged();
        dataSets.add(rsrqLine);

        LineDataSet snirLine = new LineDataSet(snirArr, " SNIR");
        snirLine.setDrawCircles(true);
        snirLine.setCircleRadius(2);
        snirLine.setDrawValues(false);
        snirLine.setLineWidth(1);
        snirLine.setColor(Color.BLUE);
        snirLine.setCircleColor(Color.BLUE);
        snirLine.notifyDataSetChanged();
        dataSets.add(snirLine);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void drawLine(float rsrp, float rsrq, float snir) {

        rsrpArray.add(new Entry(timer, rsrp));
        rsrqArray.add(new Entry(timer, rsrq));
        snirArray.add(new Entry(timer, snir));

        Comparator<Entry> comparator = new Comparator<Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                return Float.compare(o1.getX(), o2.getX());
            }
        };

        rsrqArray.sort(comparator);
        rsrqArray.sort(comparator);
        snirArray.sort(comparator);

        setLineChartData(rsrpArray, rsrqArray, snirArray);


    }

}

