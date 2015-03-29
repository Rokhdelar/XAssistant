package com.x.rokhdelar.xassistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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


public class MainActivity extends Activity {

    private ArrayList<HashMap<String,Object>> recentRequest = new ArrayList<>();
    private ListView lvRecentRequest;
    private EditText etRequestNum;
    private Button btnSend,btnRefresh;
    private String MEID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads().detectDiskWrites().detectNetwork()
                    .penaltyLog().build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                    .build());
        */

        MEID=getMEID();
        btnSend=(Button)findViewById(R.id.btnSend);
        btnSend.setEnabled(false);
        etRequestNum =(EditText)findViewById(R.id.etRequestNum);
        etRequestNum.setEnabled(false);
        lvRecentRequest = (ListView)findViewById(R.id.lvRequest);
        btnRefresh = (Button)findViewById(R.id.btnRefresh);
        btnRefresh.setEnabled(false);

        ValidTask validTask=new ValidTask();
        validTask.execute("validByMEID",MEID);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestNum=etRequestNum.getText().toString();
                if(requestNum.length()>0){  //后续增加帐号有效性的判断。
                    etRequestNum.setText("");
                    AddRequestTask addRequestTask=new AddRequestTask();
                    addRequestTask.execute(requestNum,"请解绑。",MEID,"");
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
        getRequestTask.execute("getRequestByMEID",MEID);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.action_register:
                Intent registerIntent= new Intent(this,RegisterActivity.class);
                startActivity(registerIntent);
                break;


        }

        return super.onOptionsItemSelected(item);
    }

    private class AddRequestTask extends AsyncTask<String,Integer,String>{
        @Override
        protected String doInBackground(String... params) {
            //调用HTTPClient对象发送请求。
            List<BasicNameValuePair> sqlParams = new LinkedList<>();
            sqlParams.add(new BasicNameValuePair("action","addRequest"));
            sqlParams.add(new BasicNameValuePair("requestType","解绑"));
            sqlParams.add(new BasicNameValuePair("requestNum",params[0]));
            sqlParams.add(new BasicNameValuePair("requestInfo",params[1]));
            sqlParams.add(new BasicNameValuePair("MEID",params[2]));
            sqlParams.add(new BasicNameValuePair("memo",params[3]));

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

    private class ValidTask extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... params) {
            List<BasicNameValuePair> sqlParams = new LinkedList<>();
            sqlParams.add(new BasicNameValuePair("action",params[0]));
            sqlParams.add(new BasicNameValuePair("MEID",params[1]));

            String param = URLEncodedUtils.format(sqlParams, "UTF-8");

            String baseUrl = "http://61.128.177.92/webbind/request.php";
            String url = baseUrl+"?"+param;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONObject jsonObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                    if (jsonObject.getInt("code") == 200) {
                        return "valid";
                    }else {
                        return "not valid";
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("valid")){
                btnSend.setEnabled(true);
                btnRefresh.setEnabled(true);
                etRequestNum.setEnabled(true);
            }else{
                btnSend.setEnabled(false);
                btnRefresh.setEnabled(false);
                etRequestNum.setEnabled(false);
                Toast.makeText(getApplicationContext(),"对不起，您的手机号不在有效的使用范围内，请先登记通过。",Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(s);
        }
    }
    private class GetRequestTask extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... params) {
            List<BasicNameValuePair> sqlParams = new LinkedList<>();
            sqlParams.add(new BasicNameValuePair("action",params[0]));
            sqlParams.add(new BasicNameValuePair("MEID",params[1]));

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
                            switch (jsonArray.getJSONObject(i).getString("requestState")) {
                                case "处理中...":
                                    map.put("requestState",R.mipmap.ic_unkown);
                                    break;
                                case "已处理。":
                                    map.put("requestState",R.mipmap.ic_ok);
                                    break;
                                case "账号不存在。":
                                    map.put("requestState",R.mipmap.ic_error);
                                    break;
                                default:
                                    map.put("requestState",R.mipmap.ic_unkown);
                            }
                            map.put("requestType",jsonArray.getJSONObject(i).getString("requestType")+":");
                            map.put("requestNum",jsonArray.getJSONObject(i).getString("requestNum"));
                            map.put("requestTime","请求时间："+jsonArray.getJSONObject(i).getString("requestTime"));
                            map.put("requestResult","处理状态："+jsonArray.getJSONObject(i).getString("requestState"));
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
                    new String[]{"requestState","requestType","requestNum","requestTime","requestResult"},
                    new int[]{R.id.requestState,R.id.requestType,R.id.requestNum,R.id.requestTime,R.id.requestResult});

            lvRecentRequest.setAdapter(simpleAdapter);

            super.onPostExecute(s);
        }
    }

}