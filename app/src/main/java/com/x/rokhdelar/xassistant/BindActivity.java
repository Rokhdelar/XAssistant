package com.x.rokhdelar.xassistant;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class BindActivity extends Activity {

    private ArrayList<HashMap<String,Object>> recentRequest = new ArrayList<>();
    private ListView lvRecentRequest;
    private EditText etRequestNum;
    private Button btnSend,btnRefresh;
    private String MEID;
    private Handler handler;
    private Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        MEID=getMEID();
        btnSend=(Button)findViewById(R.id.btnSend);
        etRequestNum =(EditText)findViewById(R.id.etRequestNum);
        lvRecentRequest = (ListView)findViewById(R.id.lvRequest);
        btnRefresh = (Button)findViewById(R.id.btnRefresh);


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestNum=etRequestNum.getText().toString();
                if(requestNum.length()>8){  //后续增加帐号有效性的判断。
                    //etRequestNum.setText("");
                    AddRequestTask addRequestTask=new AddRequestTask();
                    addRequestTask.execute(requestNum,"","","请解绑。",MEID,"");
                    new GetRequestTask().execute("getRequestByMEID",MEID);
                }else{
                    Toast.makeText(getApplicationContext(),"请输入有效的、需要解绑的帐号！",Toast.LENGTH_LONG).show();
                }
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetRequestTask getRequestTask=new GetRequestTask();
                getRequestTask.execute("getRequestByMEID",MEID);

            }
        });

        //获取本机号码发出的请求列表。
        GetRequestTask getRequestTask = new GetRequestTask();
        getRequestTask.execute("getRequestByMEID","解绑",MEID);

        //定时刷新的Handler。
        handler=new Handler();
        runnable=new Runnable() {
            @Override
            public void run() {
                new GetRequestTask().execute("getRequestByMEID","解绑",MEID);
                handler.postDelayed(runnable, 60 * 1000);
            }
        };
        handler.postDelayed( runnable,60*1000);

    }

    //获取手机号码。
    private String getMEID() {
        TelephonyManager telephonyManager=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(runnable);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //新增解绑请求
    private class AddRequestTask extends AsyncTask<String,Integer,String>{
        @Override
        protected String doInBackground(String... params) {
            //调用HTTPClient对象发送请求。
            List<BasicNameValuePair> sqlParams = new LinkedList<>();
            sqlParams.add(new BasicNameValuePair("action","addRequest"));
            sqlParams.add(new BasicNameValuePair("requestType","解绑"));
            sqlParams.add(new BasicNameValuePair("requestNum",params[0]));
            sqlParams.add(new BasicNameValuePair("requestContact",params[1]));
            sqlParams.add(new BasicNameValuePair("requestContactPhone",params[2]));
            sqlParams.add(new BasicNameValuePair("requestInfo",params[3]));
            sqlParams.add(new BasicNameValuePair("MEID",params[4]));
            sqlParams.add(new BasicNameValuePair("memo",params[5]));

            String param = URLEncodedUtils.format(sqlParams,"UTF-8");

            String baseUrl = "http://61.128.177.92/webbind/request.php";

            HttpGet httpGet = new HttpGet(baseUrl+"?"+param);

            HttpClient httpClient = new DefaultHttpClient();
            try{
                HttpResponse httpResponse = httpClient.execute(httpGet);
                if(httpResponse.getStatusLine().getStatusCode()==200){
                    JSONObject jsonObject=new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                    if(jsonObject.getInt("code") == 200){
                        return "解绑"+params[0]+"请求发送成功，等待处理...";
                    }else{
                        return "解绑"+params[0]+"请求发送失败，服务器返回的错误为："+
                                jsonObject.getString("message");
                    }
                }

//            Log.i("TAG","resultCode:"+httpResponse.getStatusLine().getStatusCode() );
//            Log.i("TAG","result:"+ EntityUtils.toString(httpResponse.getEntity()));
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(String s){
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
        }
    }

    //通知完成以后更新数据库。
    private class UpdateNotifyTask extends AsyncTask<String,Integer,String>{
        @Override
        protected String doInBackground(String... params) {
            List<BasicNameValuePair> sqlParams=new LinkedList<>();
            sqlParams.add(new BasicNameValuePair("action",params[0]));
            sqlParams.add(new BasicNameValuePair("id",params[1]));

            String param = URLEncodedUtils.format(sqlParams, "UTF-8");

            String baseUrl = "http://61.128.177.92/webbind/request.php";
            String url = baseUrl+"?"+param;
            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                if(httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
                    JSONObject jsonObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                    if (jsonObject.getInt("code")==200){
                        return "success";
                    }else{
                        return "fail:"+jsonObject.getString("message");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s){
            if(s!="success"){
                Toast.makeText(getApplicationContext(),"执行失败，错误信息为:"+s,Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(s);
        }

    }
    //获取对应MEID的8条请求信息。
    private class GetRequestTask extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... params) {
            recentRequest.clear();
            List<BasicNameValuePair> sqlParams = new LinkedList<>();
            sqlParams.add(new BasicNameValuePair("action",params[0]));
            sqlParams.add(new BasicNameValuePair("requestType",params[1]));
            sqlParams.add(new BasicNameValuePair("MEID",params[2]));

            String param = URLEncodedUtils.format(sqlParams, "UTF-8");

            String baseUrl = "http://61.128.177.92/webbind/request.php";
            String url = baseUrl+"?"+param;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    JSONObject jsonObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                    if (jsonObject.getInt("code")==200){
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for(int i=0;i<jsonArray.length();i++){

                            HashMap<String,Object> map=new HashMap<>();
                            map.put("requestState",jsonArray.getJSONObject(i).getInt("requestState"));
                            switch (jsonArray.getJSONObject(i).getInt("requestState")) {
                                case 0:
                                    map.put("requestStateImg",R.mipmap.ic_unkown);
                                    map.put("requestResult","处理状态：等待处理中...");
                                    break;
                                case 1:
                                    map.put("requestStateImg",R.mipmap.ic_ok);
                                    map.put("requestResult","处理状态：已经处理完毕。");
                                    break;
                                case 2:
                                    map.put("requestStateImg",R.mipmap.ic_error);
                                    map.put("requestResult","处理状态：账号错误，请检查。");
                                    break;
                                default:
                                    map.put("requestState",R.mipmap.ic_unkown);
                            }
                            map.put("requestType",jsonArray.getJSONObject(i).getString("requestType")+":");
                            map.put("requestNum",jsonArray.getJSONObject(i).getString("requestNum"));
                            map.put("requestTime","请求时间："+jsonArray.getJSONObject(i).getString("requestTime"));
                            map.put("isNotified",jsonArray.getJSONObject(i).getString("isNotified"));
                            map.put("id",jsonArray.getJSONObject(i).getInt("id"));
                            recentRequest.add(map);
                        }
                    }
                }


            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            SimpleAdapter simpleAdapter= new SimpleAdapter(getApplicationContext(),
                    recentRequest,
                    R.layout.recentrequest_layout,
                    new String[]{"requestStateImg","requestType","requestNum","requestTime","requestResult"},
                    new int[]{R.id.requestStateImg,R.id.requestType,R.id.requestNum,R.id.requestTime,R.id.requestResult});

            lvRecentRequest.setAdapter(simpleAdapter);
            //检查是否已经通知，没有通知的发出通知。
            for(HashMap<String,Object> map:recentRequest){
                if ((map.get("requestState")!=0) && (map.get("isNotified").toString().equals("NO"))){
                    String resultText=map.get("requestState")==1?"解绑助手："+map.get("requestNum").toString()+"解绑已经处理完毕。":"解绑助手："+map.get("requestNum").toString()+"解绑失败，该账号不存在。";
                    //添加通知
                    NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    Notification notification=new Notification.Builder(getApplicationContext())
                            .setAutoCancel(true)
                            .setWhen(System.currentTimeMillis())
                            .setContentTitle("解绑消息-解绑助手")
                            .setContentText(resultText)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentInfo(null)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .build();

                    notificationManager.notify((int)map.get("id"),notification);

                    Toast.makeText(getApplicationContext(),resultText,Toast.LENGTH_LONG).show();

                    //更新数据库的isNotified字段.
                    new UpdateNotifyTask().execute("updateNotified",map.get("id").toString());
                }
            }

            super.onPostExecute(s);
        }
    }

}