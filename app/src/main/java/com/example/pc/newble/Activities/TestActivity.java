package com.example.pc.newble.Activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pc.newble.R;
import com.example.pc.newble.SQLite.*;
import com.example.pc.newble.TheUtils.FileUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

//import junit.framework.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import com.example.pc.newble.TheUtils.Complex;

/**
 * 此 Activity 作为绘图显示数据并提供特征计算和分析功能
 * */

public class TestActivity extends AppCompatActivity {

    private final String TAG = "TestActivity";

    private MyDBHandler dbHandler;
    private TextView textViewOutput;
    private LineChart mChart;
    private List<String> xval = new ArrayList<>();//x轴数据
    private List<Double> yval =  new ArrayList<>();//y轴数据
    private List<Double> yfft =  new ArrayList<>();//fft后的数据
    private String path ;
    private String startTime = "";
    private String endTime = "";
    private LineData lineData;//保存mchart的作图数据
    private ProgressDialog pd;//运算费时间时弹出的进度框
    private String str; //记录结果的字符串
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        textViewOutput = findViewById(R.id.text_test_activity);
        textViewOutput.setMovementMethod(ScrollingMovementMethod.getInstance());
        Button button = findViewById(R.id.button_plot);
        final Button button1 = findViewById(R.id.button_getResult);
        mChart =  (LineChart) findViewById(R.id.chart);

        final EditText year_text = findViewById(R.id.year_text);
        final EditText month_text = findViewById(R.id.month_text);
        final EditText day_text = findViewById(R.id.day_text);
        final EditText hour_text = findViewById(R.id.hour_text);
        final EditText minute_text = findViewById(R.id.minute_text);
        final EditText second_text = findViewById(R.id.second_text);
        final EditText ehour_text = findViewById(R.id.ehour_text);
        final EditText eminute_text = findViewById(R.id.eminute_text);
        final EditText esecond_text = findViewById(R.id.esecond_text);
        button1.setEnabled(false);//保证button1只能在button后点一次
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button1.setEnabled(true);//保证button1只能在button后点一次
                path = FileUtils.getSDCardPath() + "/bletest/";
                path = path + year_text.getText().toString() + month_text.getText().toString()
                         + day_text.getText().toString() + ".csv";
                startTime = hour_text.getText().toString() + ":" + minute_text.getText().toString() + ":" + second_text.getText().toString();
                endTime = ehour_text.getText().toString() + ":" + eminute_text.getText().toString() + ":" + esecond_text.getText().toString();
                xval.clear();
                yval.clear();
                getdata(xval,yval,path,startTime,endTime);
                initChart(mChart);
                lineData = getLineData(xval,yval);
                mChart.clear();
                Log.e(TAG, "onClick: 清理完毕");
                showChart(lineData);
               // Log.e(TAG,xval.size()+"");
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(TestActivity.this, "hello", "数据分析中，请稍后……");//开启弹窗
                new Thread(){//开启新线程做计算

                    @Override
                    public void run() {
                        //需要花时间计算的方法
                        double sum,mean,std,var,median,rms,diff1Mean,
                                diff1Median,diff1Std,diff2Mean,
                                diff2Median,diff2Std,minRatio,maxRatio;

                        List<Double> diff1 = new ArrayList<>();
                        List<Double> diff2 = new ArrayList<>();
                        sum = getSum(yval);
                        mean = getMean(yval,sum);
                        var = getVar(yval,mean);
                        std = getStd(yval,mean);
                        median = getMedian(yval);
                        rms = getRms(yval);
                        diff1 = getDiff(yval);
                        diff2 = getDiff(diff1);
                        diff1Mean = getMean(diff1,getSum(diff1));
                        diff1Median = getMedian(diff1);
                        diff1Std = getStd(diff1,diff1Mean);
                        diff2Mean = getMean(diff2,getSum(diff2));
                        diff2Median = getMedian(diff2);
                        diff2Std = getStd(diff2,diff2Mean);
                        double[] minmax = getMinMax(yval);
                        double min_ratio =minmax[0]/yval.size();
                        double max_ratio = minmax[1]/yval.size();

                        yfft = getYfft(yval);
                        double sumf = getSum(yfft);
                        double meanf= getMean(yfft,sum);
                        double medianf = getMedian(yfft);
                        double stdf = getStd(yfft,mean);
                        double varf = getVar(yfft,mean);
                        double rmsf = getRms(yfft);
                        List<Double> diff1f = getDiff(yfft);
                        List<Double> diff2f = getDiff(diff1);
                        double diff1Meanf = getMean(diff1,getSum(diff1));
                        double diff1Medianf = getMedian(diff1);
                        double diff1Stdf = getStd(diff1,diff1Mean);
                        double diff2Meanf = getMean(diff2,getSum(diff2));
                        double diff2Medianf = getMedian(diff2);
                        double diff2Stdf = getStd(diff2,diff2Mean);
                        double[] minmaxf = getMinMax(yfft);
                        double min_ratiof =minmaxf[0]/yfft.size();
                        double max_ratiof = minmaxf[1]/yfft.size();
                        str = "均值: "+ String.format("%.2f",mean) +" 方差: "+String.format("%.2f",var)+
                                " 标准差: "+String.format("%.2f",std)+" 中值: "+String.format("%.2f",median)+" 均方根:"+String.format("%.2f",rms)
                                +"\n一阶微分的均值: "+ String.format("%.2f",diff1Mean) +
                                " 一阶微分的中值 " +String.format("%.2f",diff1Median)  +
                                "\n一阶微分的方差: "+String.format("%.2f",diff1Std)
                                +"\n二阶微分的均值: "+ String.format("%.2f",diff2Mean)  +
                                " 二阶微分的中值 " +String.format("%.2f",diff2Median)  +
                                "\n二阶微分的方差: "+String.format("%.2f",diff2Std) +
                                "\n最大值比: " + String.format("%.2f",max_ratio) + " 最小值比: " + String.format("%.2f",min_ratio)
                                +"\n" +String.format("%.2f",meanf) + " " + String.format("%.2f",medianf) + " "+ String.format("%.2f",stdf) + " "
                                +"\n" +String.format("%.2f",varf) + " " + String.format("%.2f",rmsf) + " "
                                +"\n" +String.format("%.2f",diff1Meanf) + " " + String.format("%.2f",diff1Median) + " "+ String.format("%.2f",diff1Stdf) + " "
                                +"\n" +String.format("%.2f",diff2Meanf) + " " + String.format("%.2f",diff2Median) + " "+ String.format("%.2f",diff2Stdf) + " "
                                +"\n" +String.format("%.2f",min_ratiof) + " " + String.format("%.2f",min_ratiof) + " "
                        ;

                        //结束计算向handler发消息
                        handler.sendEmptyMessage(0);
                    }}.start();
                button1.setEnabled(false);//保证button1只能在button后点一次
            }
        });


    }
    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            //关闭ProgressDialog
            pd.dismiss();

            //更新UI
            textViewOutput.setText(str);
        }};
    private void initChart(LineChart mChart){
        // 设置描述
        mChart.setDescription("今日情绪指数回顾");
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
        //yAxisLeft.setAxisMaxValue(100f);
        yAxisLeft.setTextSize(12f);

        // 右边的坐标轴。未来可以拓展为健康百分比之类的东西
        YAxis yAxisRight = mChart.getAxisRight();
        yAxisRight.setStartAtZero(true);
        yAxisRight.setAxisMaxValue(100f);
        yAxisRight.setEnabled(false);

        //
        //设置X轴位置
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        // 警戒线
//        LimitLine ll = new LimitLine(70f, "警戒线");//修改警戒线为70
//        ll.setLineColor(Color.RED);
//        ll.setLineWidth(2f);
//        ll.setTextColor(Color.BLACK);
//        ll.setTextSize(12f);
//        yAxisLeft.addLimitLine(ll);
    }
    private void showChart(LineData lineData) {
        // 设置图表数据
        mChart.setData(lineData);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getdata(List<String> xval,List<Double> yval,String path,String startTime,String endTime){//读取CSV文件数据
        String line;

        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            while((line = br.readLine())!=null){
                List<String> column = Arrays.asList(line.split(","));
                if(timeCompare(column.get(2),startTime)&&
                        timeCompare(endTime,column.get(2))){
                    xval.add(column.get(2));
                    yval.add(Double.parseDouble(column.get(3)));
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    public boolean timeCompare(String str1,String str2){ //比较两个时间的大小
        Boolean res = false;
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        try{
            Date date1 = df.parse(str1);
            Date date2 = df.parse(str2);
            res = date1.getTime()>= date2.getTime();
        }catch (ParseException e){
            e.printStackTrace();
        }
        return res;
    }

    public LineData getLineData(List<String> xVals,List<Double> yvals){ //制作LineData
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < yvals.size(); i++) {
            float val = yvals.get(i).floatValue();//获取数据
            yVals.add(new Entry(val, i));
        }

        // 创建数据集
        LineDataSet set = new LineDataSet(yVals, "情绪指数");
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
    public List<Double> getYfft(List<Double> y ){
        if(y.size()==0){
            return new  ArrayList<Double>();
        }
        Complex[] num = new Complex[y.size()];
        for(int i = 0;i<y.size();i++) {
            num[i]= new Complex(y.get(i),0);
            //System.out.println(num[i]);
        }
        //System.out.println("After fft");
        Complex[] res = Complex.fft(num);
        List<Double> normalizeY = new ArrayList<>();//单边频谱只取一半
        double N = res.length/2.0;
        for(int i = 0;i<(int)N;i++) {
            if(i==0) {
                normalizeY.add(res[i].abs()/(N*2));
                continue;
            }
            normalizeY.add(res[i].abs()/N);
        }
        return normalizeY;
    }
    public  double[] getMinMax(List<Double> y) {
        if(y.size()==0) {
            return new double[] {-1,-1};
        }
        double[] res = new double[2];
        res[0] = y.get(0);
        res[1] = res[0];
        for(int i = 0;i<y.size();i++) {
            if(y.get(i)<res[0]) {
                res[0] = y.get(i);
            }else if(y.get(i)>res[1]) {
                res[1] = y.get(i);
            }
        }
        return res;
    }
    public double getSum(List<Double> y){
        double sum= 0;
        for(int i = 0;i<y.size();i++){
            sum += y.get(i);
        }
        return sum;
    }
    public double getMean(List<Double> y,double sum){
        if(y.size()==0){
            return -1;
        }
        return sum/y.size();
    }
    public double getVar(List<Double> y,double mean){
        if(y.size()==0){
            return -1;
        }
        double res = 0;
        for(double i:y){
            res += (i-mean)*(i-mean);
        }
        return res/y.size();
    }
    public double getStd(List<Double> y,double mean){
        if(y.size()==0){
            return -1;
        }
        return Math.sqrt(getVar(y,mean));
    }
    public double getMedian(List<Double> y){
        if(y.size()==0){
            return -1;
        }
        List<Double> res = new ArrayList<>(y);
        Collections.sort(res);
        if(res.size()%2==0){
            return (res.get(res.size()/2)+res.get(res.size()/2-1))/2;
        }
        return res.get(res.size()/2);
    }
    public double getRms(List<Double> y){
        if(y.size()==0){
            return -1;
        }
        double sum = 0;
        for(double i:y){
            sum+=i*i;
        }
        sum = Math.sqrt(sum/y.size());
        return sum;
    }
    public List<Double> getDiff(List<Double> y){
        List<Double> diff = new ArrayList<>();
        if(y.size()==0||y.size()==1){
            return y;
        }
        for(int i = 1;i<y.size();i++){
            diff.add(y.get(i)-y.get(i-1));
        }
        return diff;
    }

}
