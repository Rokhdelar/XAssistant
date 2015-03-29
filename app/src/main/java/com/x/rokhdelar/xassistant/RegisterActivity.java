package com.x.rokhdelar.xassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;


public class RegisterActivity extends Activity {
    private static final String[] subStations={
            "城东支局",
            "城西支局",
            "李渡支局",
            "南沱支局",
            "白涛支局",
            "马武支局",
            "龙潭支局",
            "珍溪支局",
            "蔺市支局",
            "新妙支局"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        final Spinner spinner=(Spinner)findViewById(R.id.sp_substation);
        final EditText etName=(EditText)findViewById(R.id.et_name);
        final EditText etPhone=(EditText)findViewById(R.id.et_phone);
        final EditText etMemo=(EditText)findViewById(R.id.et_memo);

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                subStations);
        spinner.setAdapter(arrayAdapter);

        Button btnRegister=(Button)findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String MEID = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                String name = etName.getText().toString();
                String phone = etPhone.getText().toString();
                String memo = etMemo.getText().toString();
                String subStation = spinner.getSelectedItem().toString();

                if(MEID.length()>0 && name.length()>0 && phone.length()>10 && subStation.length()>0) {
                    SendRegister sendRegister=new SendRegister();
                    sendRegister.execute(phone,MEID,name,subStation,memo);
                }else{
                    Toast.makeText(getApplicationContext(),"您输入的信息不完整，请检查",Toast.LENGTH_LONG).show();
                }


            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class SendRegister extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... params) {
            List<BasicNameValuePair> sqlParams = new LinkedList<>();
            sqlParams.add(new BasicNameValuePair("action","addRequester"));
            sqlParams.add(new BasicNameValuePair("phone",params[0]));
            sqlParams.add(new BasicNameValuePair("MEID",params[1]));
            sqlParams.add(new BasicNameValuePair("name",params[2]));
            sqlParams.add(new BasicNameValuePair("subStation",params[3]));
            sqlParams.add(new BasicNameValuePair("memo",params[4]));

            String param = URLEncodedUtils.format(sqlParams, "UTF-8");

            String baseUrl = "http://61.128.177.92/webbind/requester.php";

            HttpGet httpGet = new HttpGet(baseUrl+"?"+param);

            HttpClient httpClient = new DefaultHttpClient();
            try{
                HttpResponse httpResponse = httpClient.execute(httpGet);
                if(httpResponse.getStatusLine().getStatusCode()==200){
                    JSONObject jsonObject=new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                    if(jsonObject.getInt("code") == 200){
                        return "注册请求发送成功，等待处理...";
                    }else{
                        return "注册请求发送失败，服务器返回的错误为："+
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
        protected void onPostExecute(String s) {
            AlertDialog.Builder builder=new AlertDialog.Builder(RegisterActivity.this);
            builder.setMessage(s)
                    .setCancelable(false)
                    .setNeutralButton("确定",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RegisterActivity.this.finish();
                        }
                    }).show();

            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            super.onPostExecute(s);
        }
    }

}
