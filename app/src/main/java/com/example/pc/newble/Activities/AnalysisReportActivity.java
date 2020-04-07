package com.example.pc.newble.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.pc.newble.R;
import com.example.pc.newble.SQLite.MyDBHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
*该活动用于获取RetrieveData这个活动的数据生成一份分析报告
*
*
*
*
 */
public class AnalysisReportActivity extends AppCompatActivity {
    private TextView textView;
    private MyDBHandler dbHandler;
    public static final String TAG = "AnalysisReportActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysisreport);
        textView=findViewById(R.id.textView1);
        Intent intent = getIntent();//获取Intent对象
        int count70[] = intent.getIntArrayExtra("count70") ; //获取超过70的数据点构成的一个数组，
        int count30[] = intent.getIntArrayExtra("count30") ;
        int count50[] = intent.getIntArrayExtra("count50") ;
        int countall[] = intent.getIntArrayExtra("countall") ;
        String address[] = intent.getStringArrayExtra("address") ;
        String date = intent.getStringExtra("date").substring(0,8);
        ArrayList<Integer> high = new ArrayList<>();//情绪指数高峰
        ArrayList<Integer> low = new ArrayList<>();//情绪指数高峰
        ArrayList<Integer> mid = new ArrayList<>();//情绪紧张时刻
        //Log.e("AnalysitActivity.this", "long:  " +recvDataLength);
        String rowtext;//用于写入textview
        String lowtime = "您这一天的情绪放松的时间段位于";
        String hightime ="您这一天的情绪焦虑抑郁的时间段位于";
        String midtime = "您这一天的情绪紧张的时间段位于";


        // 初始化数据库
        try {
            dbHandler = new MyDBHandler(this, null, null, 1);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.i(TAG, errors.toString());
        }
        //构造数据用于测试
        count30[1]=50;
        countall[1]=60;
        count70[1]=40;
        count50[1]=50;
        count30[2]=50;
        countall[2]=60;
        count70[2]=40;
        count50[2]=50;
        //构造数据用于测试

        for(int i = 0;i < 24; i++){//输出数组的每个值到textview中
            double std;
            if (address[i]== "none") {
              //  j= count70[i]+" "+ count30[i] + " " + countall[i];
            }
            else{
              //  j= count70[i]+" "+ count30[i] + " " + countall[i] +  " " +address[i];
                if ((double)count30[i]/countall[i] > 2.0/3){
                    std = dbHandler.getStdDivInACertainHour(date, i);
                    rowtext = "在" + i + "时到" + (i+1)+ "时之间，您的情绪指数小于30的时间超过40分钟，该时间段您位于" + address[i] +  ",标准差为" + std +"\n请继续保持";
                    addText(textView,rowtext);
                    low.add(i);

                }
                if ((double)count70[i]/countall[i] > 1.0/2){
                    std = dbHandler.getStdDivInACertainHour(date, i);
                    rowtext= "在" + i + "时到" + (i+1)+ "时之间，您的情绪指数大于70的时间超过30分钟，该时间段您位于"+address[i]+ ",标准差为" + std + "\n这段时间是否遇到了什么令您不悦的事情，请放松您的心情";
                    addText(textView,rowtext);
                    high.add(i);
                }
                if ((double)count50[i]/countall[i] > 2.0/3){
                    std = dbHandler.getStdDivInACertainHour(date, i);
                    rowtext= "在" + i + "时到" + (i+1)+ "时之间，您的情绪指数大于50小于70的时间超过40分钟，该时间段您位于"+address[i]+ ",标准差为" + std + "\n这段时间是否比较紧张，请放松您的心情";
                    addText(textView,rowtext);
                    mid.add(i);
                }

            }

          //  addText(textView,"1");
           // textView.append(String.valueOf(recvData[i]));
          //  Log.e("AnalysitActivity.this", "count:  " +recvData[i]);
        }
        addText(textView,"\n");

        //构造数据用于测试
       low.clear();
        Collections.addAll(low,3,5,6,7,8,10,11,15);
        high.clear();
        Collections.addAll(high,4,16,17,18,22,23);
        mid.clear();
        Collections.addAll(mid,9,12,13,20,21);
        //构造数据用于测试
        //用于输出情绪放松时段,情绪紧张，情绪焦虑代码段开始，双指针法
        String text="";
        for (int i=0, j = 0;i<low.size();i=j){    //
            j=i+1;
            while(j<low.size()&&low.get(j)-low.get(j-1)==1){
                ++j;
            }
            if( j-1 == i ){
                text=text+low.get(i)+"时到"+(low.get(i)+1)+"时，";
            }else{
                text=text+low.get(i)+"时到"+(low.get(j-1)+1)+"时，";
            }

        }
        lowtime=lowtime+text;
        addText(textView,lowtime);


        text="";
        for (int i=0, j = 0;i<mid.size();i=j){    //
            j=i+1;
            while(j<mid.size()&&mid.get(j)-mid.get(j-1)==1){
                ++j;
            }
            if( j-1 == i ){
                text=text+mid.get(i)+"时到"+(mid.get(i)+1)+"时，";
            }else{
                text=text+mid.get(i)+"时到"+(mid.get(j-1)+1)+"时，";
            }

        }
        midtime=midtime+text;
        addText(textView,midtime);


        text="";
        for (int i=0;i<high.size();i++){    //
            int j=i+1;
            while(j<high.size()&&high.get(j)-high.get(j-1)==1){
                ++j;
            }
            if( j-1 == i ){
                text=text+high.get(i)+"时到"+(high.get(i)+1)+"时，";
            }else{
                text=text+high.get(i)+"时到"+(high.get(j-1)+1)+"时，";
            }
            i = j-1;

        }
        hightime=hightime+text;
        hightime = hightime + "\n";
        addText(textView,hightime);
        //用于输出情绪放松时段，情绪紧张,情绪焦虑时段代码段结束




        // 提供统计数据
        /*text = "「0：00 ～ 1：00」统计数据：\n";
        // 获得日期以便从数据库中查询
        String date = intent.getStringExtra("date").substring(0,8);

        int a = dbHandler.getCountBiggerThanACertainValue(date, 0, 70);
        text = text + "电位差超过 70 的占比：" + a + "% \n";
        double b = dbHandler.getStdDivInACertainHour(date, 0);
        text = text + "这段时间的电位的标准差：" + b + "\n";
        addText(textView, text);*/

    }

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
