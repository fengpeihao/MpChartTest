package com.honglu.mpcharttest;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioGroup;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.honglu.mpcharttest.bean.DataParse;
import com.honglu.mpcharttest.bean.KLineBean;
import com.honglu.mpcharttest.mychart.CoupleChartGestureListener;
import com.honglu.mpcharttest.utils.DealUtils;
import com.honglu.mpcharttest.utils.VolFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class StockActivity extends AppCompatActivity {

    @Bind(R.id.kchart)
    CombinedChart mKchart;
    @Bind(R.id.bchart)
    CombinedChart mBchart;
    @Bind(R.id.group1)
    RadioGroup mGroup1;
    @Bind(R.id.group2)
    RadioGroup mGroup2;
    XAxis xAxisBar, xAxisK;
    YAxis axisLeftBar, axisLeftK;
    YAxis axisRightBar, axisRightK;
    private DataParse mData;
    private ArrayList<KLineBean> kLineDatas;
    private int sum = 0;
    private String choosebar = "";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBchart.setAutoScaleMinMaxEnabled(true);
            mKchart.setAutoScaleMinMaxEnabled(true);

            mKchart.notifyDataSetChanged();
            mBchart.notifyDataSetChanged();

            mKchart.invalidate();
            mBchart.invalidate();

        }
    };
    private ArrayList<CandleEntry> mBarEntries;
    private ArrayList<String> mXVals;
    private LineData mLineData;
    private ArrayList<CandleEntry> mCandleEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
        ButterKnife.bind(this);
        initChart();
        getOffLineData();
        setListener();
    }

    private void setListener() {
        mGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

            }
        });
        mGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.macd:
                        choosebar = "macd";
                        break;
                    case R.id.vol:
                        choosebar = "vol";
                        setVolData();
                        break;
                    case R.id.kdj:
                        choosebar = "kdj";
                        setVolData();
//                        mBchart.moveViewToX(mKchart.getLowestVisibleXIndex());

                        break;
                    case R.id.wr:
                        choosebar = "wr";
                        break;
                    case R.id.rsi:
                        choosebar = "rsi";
                        break;
                }
                mBchart.moveViewToX(mKchart.getLowestVisibleXIndex());
            }
        });
    }

    private void getOffLineData() {

           /*方便测试，加入假数据*/
        mData = new DataParse();
        JSONObject object = null;
        try {
            object = new JSONObject(ConstantTest.KLINEURL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mData.parseKLine(object);

        mData.getKLineDatas();

        setData(mData);
    }

    private void setData(DataParse data) {
        kLineDatas = data.getKLineDatas();

        mXVals = new ArrayList<>();
        mBarEntries = new ArrayList<>();
        mCandleEntries = new ArrayList<>();
        ArrayList<Entry> line5Entries = new ArrayList<>();
        ArrayList<Entry> line10Entries = new ArrayList<>();
        ArrayList<Entry> line30Entries = new ArrayList<>();
        for (int i = 0, j = 0; i < data.getKLineDatas().size(); i++, j++) {
            mXVals.add(data.getKLineDatas().get(i).date + "");
            mBarEntries.add(new CandleEntry(i, data.getKLineDatas().get(i).vol, 0f, 0f, data.getKLineDatas().get(i).vol));
            mCandleEntries.add(new CandleEntry(i, data.getKLineDatas().get(i).high, data.getKLineDatas().get(i).low, data.getKLineDatas().get(i).open, data.getKLineDatas().get(i).close));
//            barEntries.add(new CandleEntry(i, data.getKLineDatas().get(i).high, data.getKLineDatas().get(i).low, data.getKLineDatas().get(i).open, data.getKLineDatas().get(i).close));
            if (i >= 4) {
                sum = 0;
                line5Entries.add(new Entry(getSum(i - 4, i) / 5, i));
            }
            if (i >= 9) {
                sum = 0;
                line10Entries.add(new Entry(getSum(i - 9, i) / 10, i));
            }
            if (i >= 29) {
                sum = 0;
                line30Entries.add(new Entry(getSum(i - 29, i) / 30, i));
            }

        }

        mLineData = getLineData(line5Entries, line10Entries, line30Entries);

        setVolData();
        setCombinedData();

        setOffset();
        handler.sendEmptyMessageDelayed(0, 300);//此处解决k线图y轴显示问题，图表滑动后才能对齐的bug
    }

    private LineData getLineData(ArrayList<Entry> line5Entries, ArrayList<Entry> line10Entries, ArrayList<Entry> line30Entries) {
        ArrayList<ILineDataSet> sets = new ArrayList<>();
        int size = kLineDatas.size();   //点的个数
        /**此处修复如果显示的点的个数达不到MA均线的位置所有的点都从0开始计算最小值的问题******************************/
        if (size >= 30) {
            sets.add(setMaLine(5, mXVals, line5Entries));
            sets.add(setMaLine(10, mXVals, line10Entries));
            sets.add(setMaLine(30, mXVals, line30Entries));
        } else if (size >= 10 && size < 30) {
            sets.add(setMaLine(5, mXVals, line5Entries));
            sets.add(setMaLine(10, mXVals, line10Entries));
        } else if (size >= 5 && size < 10) {
            sets.add(setMaLine(5, mXVals, line5Entries));
        }
        return new LineData(mXVals, sets);
    }

    private void setCombinedData() {
        mKchart.resetTracking();
        CandleData candleData = getCandleData();
        CombinedData combinedData = new CombinedData(mXVals);
        combinedData.setData(candleData);
        combinedData.setData(mLineData);
        mKchart.setData(combinedData);
        mKchart.moveViewToX(mData.getKLineDatas().size() - 1);
        final ViewPortHandler viewPortHandlerCombin = mKchart.getViewPortHandler();
        viewPortHandlerCombin.setMaximumScaleX(culcMaxscale(mXVals.size()));
        Matrix matrixCombin = viewPortHandlerCombin.getMatrixTouch();
        final float xscaleCombin = 3;
        matrixCombin.postScale(xscaleCombin, 1f);
    }

    private void setVolData() {
        mBchart.resetTracking();
        axisLeftBar = mBchart.getAxisLeft();
        // axisLeftBar.setAxisMaxValue(mData.getVolmax());
        String unit = DealUtils.getVolUnit(mData.getVolmax());
        int u = 1;
        if ("万手".equals(unit)) {
            u = 4;
        } else if ("亿手".equals(unit)) {
            u = 8;
        }
        axisLeftBar.setValueFormatter(new VolFormatter((int) Math.pow(10, u)));
        // axisRightBar.setAxisMaxValue(mData.getVolmax());

        CandleData candleDataBar = setBarChartData();
        CombinedData combinedDataBar = new CombinedData(mXVals);
        if ("kdj".equals(choosebar)) {
            combinedDataBar.setData(mLineData);
        } else if ("vol".equals(choosebar)) {
            combinedDataBar.setData(candleDataBar);
        }else{
            combinedDataBar.setData(candleDataBar);
        }
        mBchart.setData(combinedDataBar);
        mBchart.moveViewToX(mData.getKLineDatas().size() - 1);
        final ViewPortHandler viewPortHandlerbar = mBchart.getViewPortHandler();
        viewPortHandlerbar.setMaximumScaleX(culcMaxscale(mXVals.size()));
        Matrix matrixbar = viewPortHandlerbar.getMatrixTouch();
        final float xscalebar = 3;
        matrixbar.postScale(xscalebar, 1f);
    }

    private void initChart() {
        setBarChartStyle();
        setCombinedChartStyle();

        // 将K线控的滑动事件传递给交易量控件
        mKchart.setOnChartGestureListener(new CoupleChartGestureListener(mKchart, new Chart[]{mBchart}));
        // 将交易量控件的滑动事件传递给K线控件
        mBchart.setOnChartGestureListener(new CoupleChartGestureListener(mBchart, new Chart[]{mKchart}));
        mBchart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Log.e("%%%%", h.getXIndex() + "");
                mKchart.highlightValues(new Highlight[]{h});
            }

            @Override
            public void onNothingSelected() {
                mKchart.highlightValue(null);
            }
        });
        mKchart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

                mBchart.highlightValues(new Highlight[]{h});
            }

            @Override
            public void onNothingSelected() {
                mBchart.highlightValue(null);
            }
        });
    }

    private void setCombinedChartStyle() {
        mKchart.setDrawBorders(true);//是否在折线图上添加边框
        mKchart.setBorderWidth(1);
        mKchart.setBorderColor(getResources().getColor(R.color.minute_grayLine));
        mKchart.setDescription("");// 数据描述
        mKchart.setDragEnabled(true);//是否可以拖拽
        mKchart.setScaleYEnabled(true);// 是否可以缩放

        Legend combinedchartLegend = mKchart.getLegend();// 设置比例图标示
        combinedchartLegend.setEnabled(false);
        //bar x y轴
        xAxisK = mKchart.getXAxis();
        xAxisK.setDrawLabels(true);//是否显示刻度标识
        xAxisK.setDrawGridLines(true);//是否显示轴刻度网线
        xAxisK.setDrawAxisLine(false);//是否显示边框
        xAxisK.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        xAxisK.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisK.setGridColor(getResources().getColor(R.color.minute_grayLine));

        axisLeftK = mKchart.getAxisLeft();
        axisLeftK.setDrawLabels(true);//是否显示刻度标识
        axisLeftK.setDrawGridLines(true);
        axisLeftK.setDrawAxisLine(false);
        axisLeftK.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        axisLeftK.setGridColor(getResources().getColor(R.color.minute_grayLine));
        axisLeftK.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisRightK = mKchart.getAxisRight();
        axisRightK.setDrawLabels(false);//是否显示刻度标识
        axisRightK.setDrawGridLines(true);
        axisRightK.setDrawAxisLine(false);
        axisRightK.setGridColor(getResources().getColor(R.color.minute_grayLine));
        mKchart.setDragDecelerationEnabled(true);
        mKchart.setDragDecelerationFrictionCoef(0.2f);
    }

    private void setBarChartStyle() {
        mBchart.setDrawBorders(true);//是否在折线图上添加边框
        mBchart.setBorderWidth(1);
        mBchart.setBorderColor(getResources().getColor(R.color.minute_grayLine));
        mBchart.setDescription("");// 数据描述
        mBchart.setDragEnabled(true);//是否可以拖拽
        mBchart.setScaleYEnabled(true);// 是否可以缩放

        Legend combinedchartLegend = mBchart.getLegend();// 设置比例图标示
        combinedchartLegend.setEnabled(false);
        //bar x y轴
        xAxisK = mBchart.getXAxis();
        xAxisK.setDrawLabels(true);//是否显示刻度标识
        xAxisK.setDrawGridLines(true);//是否显示轴刻度网线
        xAxisK.setDrawAxisLine(false);//是否显示边框
        xAxisK.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        xAxisK.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisK.setGridColor(getResources().getColor(R.color.minute_grayLine));

        axisLeftK = mBchart.getAxisLeft();
        axisLeftK.setDrawLabels(true);//是否显示刻度标识
        axisLeftK.setDrawGridLines(true);
        axisLeftK.setDrawAxisLine(false);
        axisLeftK.setTextColor(getResources().getColor(R.color.minute_zhoutv));
        axisLeftK.setGridColor(getResources().getColor(R.color.minute_grayLine));
        axisLeftK.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisRightK = mBchart.getAxisRight();
        axisRightK.setDrawLabels(false);//是否显示刻度标识
        axisRightK.setDrawGridLines(true);
        axisRightK.setDrawAxisLine(false);
        axisRightK.setGridColor(getResources().getColor(R.color.minute_grayLine));
        mBchart.setDragDecelerationEnabled(true);
        mBchart.setDragDecelerationFrictionCoef(0.2f);

    }

    @NonNull
    private LineDataSet setMaLine(int ma, ArrayList<String> xVals, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + ma);
        if (ma == 5) {
            lineDataSetMa.setHighlightEnabled(true);
            lineDataSetMa.setDrawHorizontalHighlightIndicator(false);
            lineDataSetMa.setHighLightColor(Color.WHITE);
        } else {/*此处必须得写*/
            lineDataSetMa.setHighlightEnabled(false);
        }
        lineDataSetMa.setDrawValues(false);
        if (ma == 5) {
            lineDataSetMa.setColor(Color.GREEN);
        } else if (ma == 10) {
            lineDataSetMa.setColor(Color.GRAY);
        } else {
            lineDataSetMa.setColor(Color.YELLOW);
        }
        lineDataSetMa.setLineWidth(1f);
        lineDataSetMa.setDrawCircles(false);
        lineDataSetMa.setAxisDependency(YAxis.AxisDependency.LEFT);
        return lineDataSetMa;
    }

    /**
     * 获取、设置K线数据
     *
     */
    private CandleData getCandleData() {
        CandleDataSet candleDataSet = new CandleDataSet(mCandleEntries, "KLine");
//        candleDataSet.setDrawHorizontalHighlightIndicator(false);
        candleDataSet.setDecreasingColor(Color.RED);
        candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);//红涨，实体
        candleDataSet.setIncreasingColor(Color.GREEN);
        candleDataSet.setDecreasingPaintStyle(Paint.Style.STROKE);//绿跌，空心
        candleDataSet.setNeutralColor(Color.RED);//当天价格不涨不跌（一字线）颜色
        candleDataSet.setShadowColorSameAsCandle(true);//影线颜色与实体一致
        candleDataSet.setValueTextSize(10f);
        candleDataSet.setDrawValues(false);//在图表中的元素上面是否显示数值
        candleDataSet.setColor(Color.RED);
        candleDataSet.setShadowWidth(1f);//影线宽
        candleDataSet.setHighLightColor(Color.GRAY);
        candleDataSet.setHighlightLineWidth(0.5f);
        candleDataSet.setHighlightEnabled(true);
        candleDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        return new CandleData(mXVals, candleDataSet);
    }

    /**
     * 设置成交量
     *
     */
    private CandleData setBarChartData() {
        CandleDataSet candleDataSet = new CandleDataSet(mBarEntries, "KLine");
        candleDataSet.setDrawHorizontalHighlightIndicator(false);
        candleDataSet.setHighlightEnabled(true);
        candleDataSet.setHighLightColor(Color.WHITE);
        candleDataSet.setDecreasingColor(Color.RED);
        candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);//红涨，实体
        candleDataSet.setIncreasingColor(Color.GREEN);
        candleDataSet.setDecreasingPaintStyle(Paint.Style.STROKE);//绿跌，空心
        candleDataSet.setNeutralColor(Color.RED);//当天价格不涨不跌（一字线）颜色
        candleDataSet.setShadowColorSameAsCandle(true);//影线颜色与实体一致
        candleDataSet.setValueTextSize(10f);
        candleDataSet.setDrawValues(false);//在图表中的元素上面是否显示数值
        candleDataSet.setColor(Color.RED);
        candleDataSet.setShadowWidth(1f);//影线宽
        candleDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        return new CandleData(mXVals, candleDataSet);
    }

    private float getSum(Integer a, Integer b) {

        for (int i = a; i <= b; i++) {
            sum += mData.getKLineDatas().get(i).close;
        }
        return sum;
    }

    private float culcMaxscale(float count) {
        float max = 1;
        max = count / 127 * 5;
        return max;
    }

    /*设置量表对齐*/
    private void setOffset() {
        float lineLeft = mKchart.getViewPortHandler().offsetLeft();
        float barLeft = mBchart.getViewPortHandler().offsetLeft();
        float lineRight = mKchart.getViewPortHandler().offsetRight();
        float barRight = mBchart.getViewPortHandler().offsetRight();
        float barBottom = mBchart.getViewPortHandler().offsetBottom();
        float offsetLeft, offsetRight;
        float transLeft = 0, transRight = 0;
 /*注：setExtraLeft...函数是针对图表相对位置计算，比如A表offLeftA=20dp,B表offLeftB=30dp,则A.setExtraLeftOffset(10),并不是30，还有注意单位转换*/
        if (barLeft < lineLeft) {
           /* offsetLeft = Utils.convertPixelsToDp(lineLeft - barLeft);
            barChart.setExtraLeftOffset(offsetLeft);*/
            transLeft = lineLeft;
        } else {
            offsetLeft = Utils.convertPixelsToDp(barLeft - lineLeft);
            mKchart.setExtraLeftOffset(offsetLeft);
            transLeft = barLeft;
        }
  /*注：setExtraRight...函数是针对图表绝对位置计算，比如A表offRightA=20dp,B表offRightB=30dp,则A.setExtraLeftOffset(30),并不是10，还有注意单位转换*/
        if (barRight < lineRight) {
          /*  offsetRight = Utils.convertPixelsToDp(lineRight);
            barChart.setExtraRightOffset(offsetRight);*/
            transRight = lineRight;
        } else {
            offsetRight = Utils.convertPixelsToDp(barRight);
            mKchart.setExtraRightOffset(offsetRight);
            transRight = barRight;
        }
        mBchart.setViewPortOffsets(transLeft, 15, transRight, barBottom);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
