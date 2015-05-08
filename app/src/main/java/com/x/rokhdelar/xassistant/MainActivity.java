package com.x.rokhdelar.xassistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends Activity {
    private boolean isValid=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lvMainModule=(ListView)findViewById(R.id.lvMainModule);
        ArrayList<HashMap<String,Object>> listModule=new ArrayList<HashMap<String, Object>>();
        HashMap<String,Object> map=new HashMap<String,Object>();
        map.put("moduleImage", R.mipmap.ic_chat);
        map.put("moduleTitle", getString(R.string.stringModuleBind));
        map.put("moduleDesc", getString(R.string.stringModuleBindDesc));
        listModule.add(map);

        map=new HashMap<String,Object>();
        map.put("moduleImage", R.mipmap.ic_edit);
        map.put("moduleTitle", getString(R.string.stringModuleFaultDeclaration));
        map.put("moduleDesc", getString(R.string.stringModuleFaultDeclarationDesc));
        listModule.add(map);

        map=new HashMap<String,Object>();
        map.put("moduleImage", R.mipmap.ic_register);
        map.put("moduleTitle", getString(R.string.stringRegister));
        map.put("moduleDesc", getString(R.string.stringRegisterDesc));
        listModule.add(map);

        SimpleAdapter simpleAdapter=new SimpleAdapter(this,
                listModule,
                R.layout.item_main_list,
                new String[]{"moduleImage","moduleTitle","moduleDesc"},
                new int[]{R.id.moduleImage,R.id.moduleTitle,R.id.moduleDesc}
                );

        lvMainModule.setAdapter(simpleAdapter);

        lvMainModule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent;
                switch (i){
                    case 0:
                        if(isValid){
                            intent=new Intent(getApplicationContext(),BindActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getApplicationContext(),getString(R.string.warnUnRegister),Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 1:
                        if(isValid){
                            intent=new Intent(getApplicationContext(),FaultReportActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getApplicationContext(),getString(R.string.warnUnRegister),Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 2:
                        if(isValid){
                            Toast.makeText(getApplicationContext(),getString(R.string.warnRepeatRegister),Toast.LENGTH_LONG).show();
                            break;
                        }
                        intent=new Intent(getApplicationContext(),RegisterActivity.class);
                        startActivity(intent);
                        break;
                    case 3:
                        break;
                }
            }
        });

        String MEID=getMEID();
        ValidTask validTask=new ValidTask();
        validTask.execute("validByMEID",MEID);

    }

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //��֤MEID��Ч�ԡ�
    private class ValidTask extends AsyncTask<String,Integer,String> {

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
                isValid=true;
            }else{
                isValid=false;
                Toast.makeText(getApplicationContext(), "�Բ�������ֻ�Ų�����Ч��ʹ�÷�Χ�ڣ����ȵǼ�ͨ��", Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(s);
        }
    }
}
