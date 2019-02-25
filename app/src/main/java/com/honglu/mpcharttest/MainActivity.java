package com.honglu.mpcharttest;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String TAG = "qqq";
    private CombinedChart mChart;
    private Button btn;
    private int itemcount;
    private LineData lineData;
    private CandleData candleData;
    private CombinedData combinedData;
    private ArrayList<String> xVals;
    private List<CandleEntry> candleEntries = new ArrayList<>();
    private int colorHomeBg;
    private int colorLine;
    private int colorText;
    private int colorMa5;
    private int colorMa10;
    private int colorMa20;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combine);

        mChart = (CombinedChart) findViewById(R.id.chart);
        initChart();
        loadChartData();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initChart() {
        colorHomeBg = getResources().getColor(R.color.home_page_bg);
        colorLine = getResources().getColor(R.color.common_divider);
        colorText = getResources().getColor(R.color.text_grey_light);
        colorMa5 = getResources().getColor(R.color.ma5);
        colorMa10 = getResources().getColor(R.color.ma10);
        colorMa20 = getResources().getColor(R.color.ma20);

        mChart.setDescription("小牛操盘");//数据描述
        mChart.setNoDataTextDescription("You need to provide data for the mChart.");//如果没有数据的时候，会显示这个
        mChart.setDrawGridBackground(true);//是否显示表格颜色
        mChart.setBackgroundColor(colorHomeBg);//设置背景
        mChart.setGridBackgroundColor(colorHomeBg);//设置表格颜色
        mChart.setScaleYEnabled(false);//if disabled, scaling can be done on x-axis
        mChart.setPinchZoom(false);//if disabled, scaling can be done on x- and y-axis separately
        mChart.setDrawValueAboveBar(false);
        mChart.setNoDataText("加载中...");
        mChart.setAutoScaleMinMaxEnabled(false);

        mChart.setScaleEnabled(true);//是否支持缩放
        mChart.setDragEnabled(true);//是否支持拖拽
        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);//是否显示轴刻度网线
        xAxis.setGridColor(colorLine);
        xAxis.setTextColor(colorText);
//        xAxis.setLabelsToSkip(5);
        xAxis.setSpaceBetweenLabels(12);//轴刻度间的宽度，默认值是4

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setLabelCount(4, false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);//是否显示边框
        leftAxis.setGridColor(colorLine);
        leftAxis.setTextColor(colorText);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);//是否显示右侧Y轴

        int[] colors = {colorMa5, colorMa10, colorMa20};
        String[] labels = {"MA5", "MA10", "MA20"};
        Legend legend = mChart.getLegend();//设置比例图标示
        legend.setCustom(colors, labels);
        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_RIGHT);//显示位置
        legend.setForm(Legend.LegendForm.SQUARE);//样式
        legend.setFormSize(6f);//字号
        legend.setTextColor(Color.WHITE);
        legend.setTypeface(Typeface.SERIF);// 字体


        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {
                CandleEntry candleEntry = (CandleEntry) entry;
                float change = (candleEntry.getClose() - candleEntry.getOpen()) / candleEntry.getOpen();
                NumberFormat nf = NumberFormat.getPercentInstance();
                nf.setMaximumFractionDigits(2);
                String changePercentage = nf.format(Double.valueOf(String.valueOf(change)));
                Log.d("qqq", "最高" + candleEntry.getHigh() + " 最低" + candleEntry.getLow() +
                        " 开盘" + candleEntry.getOpen() + " 收盘" + candleEntry.getClose() +
                        " 涨跌幅" + changePercentage);
            }

            @Override
            public void onNothingSelected() {

            }
        });

    }


    private void loadChartData() {
        mChart.resetTracking();

        candleEntries = Model.getCandleEntries();

        itemcount = candleEntries.size();
        List<StockListBean.StockBean> stockBeans = Model.getData();
        xVals = new ArrayList<>();
        for (int i = 0; i < itemcount; i++) {
            xVals.add(stockBeans.get(i).getDate());
        }

        combinedData = new CombinedData(xVals);

        /*k line*/
        candleData = generateCandleData();
        combinedData.setData(candleData);

        /*ma5*/
        ArrayList<Entry> ma5Entries = new ArrayList<Entry>();
        for (int index = 0; index < itemcount; index++) {
            ma5Entries.add(new Entry(stockBeans.get(index).getMa5(), index));
        }
        /*ma10*/
        ArrayList<Entry> ma10Entries = new ArrayList<Entry>();
        for (int index = 0; index < itemcount; index++) {
            ma10Entries.add(new Entry(stockBeans.get(index).getMa10(), index));
        }
        /*ma20*/
        ArrayList<Entry> ma20Entries = new ArrayList<Entry>();
        for (int index = 0; index < itemcount; index++) {
            ma20Entries.add(new Entry(stockBeans.get(index).getMa20(), index));
        }

        lineData = generateMultiLineData(
                generateLineDataSet(ma5Entries, colorMa5, "ma5"),
                generateLineDataSet(ma10Entries, colorMa10, "ma10"),
                generateLineDataSet(ma20Entries, colorMa20, "ma20"));

        combinedData.setData(lineData);
        mChart.setData(combinedData);//当前屏幕会显示所有的数据
        mChart.invalidate();
    }

    private LineDataSet generateLineDataSet(List<Entry> entries, int color, String label) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(color);
        set.setLineWidth(1f);
        set.setDrawCubic(true);//圆滑曲线
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setHighlightEnabled(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return set;
    }

    private LineData generateMultiLineData(LineDataSet... lineDataSets) {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < lineDataSets.length; i++) {
            dataSets.add(lineDataSets[i]);
        }

        List<String> xVals = new ArrayList<String>();
        for (int i = 0; i < itemcount; i++) {
            xVals.add("" + (1990 + i));
        }

        LineData data = new LineData(xVals, dataSets);

        return data;
    }

    private CandleData generateCandleData() {

        CandleDataSet set = new CandleDataSet(candleEntries, "");

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setShadowColor(Color.DKGRAY);//影线颜色
        set.setShadowColorSameAsCandle(true);//影线颜色与实体一致
        set.setShadowWidth(0.7f);//影线宽
        set.setDecreasingColor(Color.RED);
        set.setDecreasingPaintStyle(Paint.Style.FILL);//红涨，实体
        set.setIncreasingColor(Color.GREEN);
        set.setIncreasingPaintStyle(Paint.Style.STROKE);//绿跌，空心
        set.setNeutralColor(Color.RED);//当天价格不涨不跌（一字线）颜色
        set.setHighlightLineWidth(0.5f);//选中蜡烛时的线宽
        set.setHighLightColor(Color.WHITE);
        set.setDrawValues(false);//在图表中的元素上面是否显示数值
        set.setLabel("label");//图表名称，可以通过mChart.getLegend().setEnable(true)显示在标注上

        CandleData candleData = new CandleData(xVals);
        candleData.addDataSet(set);

        return candleData;
    }
}
