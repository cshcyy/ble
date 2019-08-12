package com.example.pc.newble.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pc.newble.R;
import com.example.pc.newble.TheUtils.FileUtils;
import com.example.pc.newble.SQLite.*;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;


import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import android.os.Handler;

import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.Vector;

import static com.github.mikephil.charting.components.Legend.LegendPosition.RIGHT_OF_CHART_INSIDE;

public class RetrieveData extends AppCompatActivity {

    public static final String TAG = "RetrieveData.this";

    private LineChart mChart;
    private boolean isRunning;
    private Thread thread;
    private TextView textView;


    // 在 onCreate 里取消了 handler 转而直接调用 onClick，由于这个过程耗时不长，没有必要用handler
    private Handler handler;
    private MyDBHandler dbHandler;

    //
    private String file;

    /**
    * 对./bletest/目录下的假设
    * - 应该有一个命名为 "DataList.txt" 的文件用以储存各 entry 的信息
    * - 每一个 entry，都应该有一个独立的存储文档，例："/bletest/20190710025510.txt"
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_data);

        // 实例化 dbHandler。
        try {
            dbHandler = new MyDBHandler(this, null, null, 1);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.i(TAG, errors.toString());
        }

        textView =  findViewById(R.id.text_retrieved_data);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());//滚动

        mChart =  (LineChart) findViewById(R.id.chart);

        // 设置描述
        mChart.setDescription("5号（红色）的电压");
        //是否展示网格线
        mChart.setDrawGridBackground(false);
        //是否显示边界
        mChart.setDrawBorders(true);
        //是否可以拖动
        mChart.setDragEnabled(true);
        // 设置触摸模式
        mChart.setTouchEnabled(true);
        //设置XY轴动画效果
        mChart.animateY(600);
        mChart.animateX(1500);
        //
        // y坐标轴的设定。需要改y轴最大值的话可以在这里改
        YAxis yAxisLeft = mChart.getAxisLeft();
        yAxisLeft.setStartAtZero(true);
        yAxisLeft.setAxisMaxValue(100f);
        // 右边的坐标轴。未来可以拓展为健康百分比之类的东西
        YAxis yAxisRight = mChart.getAxisRight();
        yAxisRight.setStartAtZero(true);
        yAxisRight.setAxisMaxValue(100f);
        yAxisRight.setEnabled(false);

        // 警戒线
        LimitLine ll = new LimitLine(40f, "警戒线");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(2f);
        ll.setTextColor(Color.BLACK);
        ll.setTextSize(12f);
        yAxisLeft.addLimitLine(ll);



        // 得到所要的日期
        Intent intent = getIntent();
        String date = intent.getStringExtra("file_to_read").substring(0,8);
        Vector<Double> todayData = GetTodayData(date);
   //     getLineData(todayData);
        Log.e(TAG, "onCreate: " + todayData );

        // 得到所要日期的数据
        Log.e(TAG, "onClick: 数据从vector读取结束");
        LineData lineData = RetrieveDataFromVector(todayData);

        mChart.clear();

        Log.e(TAG, "onClick: 清理完毕");
        showChart(lineData);
    }



    private void showChart(LineData lineData) {
        // 设置图表数据
        mChart.setData(lineData);
    }


  /*
    public void doStart(View view) {
        isRunning = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        handler.sendEmptyMessage(0x001);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public void doStop(View view) {
        isRunning = false;
        thread = null;
    }
  */




    public LineData RetrieveDataFromVector(Vector<Double> vector){

        Log.e(TAG, "RetrieveDataFromVector: 进入了retrieve from vector函数" );
        int count = vector.size();

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            // 进行处理，以便让时间显示为 "HH：MM" 的形式
            int TIME_INTERVAL = MainActivity.TIME_INTERVAL;
            String string = new String();
            int SecondPastSinceTheDayBegin = TIME_INTERVAL * i;  //过去了多少秒
            int hour = (int) Math.floor(SecondPastSinceTheDayBegin / 3600.0);
            int minute = (int) Math.floor((SecondPastSinceTheDayBegin % 3600.0) / 60.0);
            // 如果小时/分钟数小于10，则加0补全
            if (hour < 10){
                string = "0" + hour;
            } else {
                string = Integer.toString(hour);
            }
            string = string + ":";
            if (minute < 10){
                string = string + "0" + minute;
            } else {
                string = string + minute;
            }


            xVals.add(string);
        }

        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float val = (float) vector.get(i).floatValue();//获取数据
            yVals.add(new Entry(val, i));
        }


        // 创建数据集
        LineDataSet set = new LineDataSet(yVals, "数据集");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.YELLOW);
        set.setLineWidth(0f);
        set.setCircleSize(0f);
        set.setFillAlpha(45);


        //设置曲线值的圆点是实心还是空心
        set.setDrawCircleHole(false);
        set.setValueTextSize(10f);
        //设置折线图填充
        set.setDrawFilled(true);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setDrawCircleHole(false);


        // 创建数据集列表
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set);

        // 创建折线数据对象（第二个参数可以是set）
        LineData lineData = new LineData(xVals, dataSets);
        lineData.setDrawValues(false);
        lineData.setValueTextColor(Color.BLACK);
        lineData.setValueTextSize(9f);

        return lineData;
    }


    /**
     * 获得某一天的电压数据，并将其保存到一个Vector
     * date：YYYYMMDD
     * */
    public Vector<Double> GetTodayData(String date){

        Log.e(TAG, "GetTodayData: 今天的日期是"  + date );
        Vector<Double> doubleVector = new Vector<>();
        int TIME_INTERVAL = MainActivity.TIME_INTERVAL;
        // 一天内所有数据点
        for (int i=0; i<(int)86400/TIME_INTERVAL; i++){    //获取电压数据
            String a = dbHandler.getDataOfOneCertainTime(date, i); //将一天变成86400/60个点，获取那个点对应的电压值
            Log.e(TAG, "GetTodayData: 哈哈哈哈 + a " +i + "  "+ a );
            if (a == "none") {
                // 如果数据库里没有记录，默认是 0.0
                doubleVector.add(0.0);
            } else {
                doubleVector.add(Double.parseDouble(a));
            }
            // 获取每个整点时刻的地理位置
            // 注：此处的60是基于TIME_INTERVAL = 60 而来的，如果修改了TIME_INTERVAL，须对此60作出修改
            if (i % 60 == 0){
                String j = Integer.toString(i/60)+"点：" + dbHandler.getaddrOfOneCertainTime(date, i);
                addText(textView, j);
            }
        }
        return doubleVector;
    }





    // 添加数据到textview中
    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n");
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        //  int offset = textView.getLineCount() * textView.getLineHeight();
        //  if (offset > textView.getHeight()) {
        //      textView.scrollTo(0, offset - textView.getHeight());
        //  }
    }






}


