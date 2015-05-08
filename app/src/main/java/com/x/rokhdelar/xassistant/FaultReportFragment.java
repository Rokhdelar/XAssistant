package com.x.rokhdelar.xassistant;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class FaultReportFragment extends Fragment {
    private EditText editTextNum,editTextContact,editTextContactPhone,editTextInfo;
    private Button buttonSend;
    private Spinner spinnerInfo;
    String requestNum,requestContact,requestContactPhone,requestInfo,MEID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_fault_report, container, false);

        initView(view);
        initEvent();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    private void initView(View view) {
        editTextNum=(EditText)view.findViewById(R.id.editTextNum);
        editTextContact=(EditText)view.findViewById(R.id.editTextContact);
        editTextContactPhone=(EditText)view.findViewById(R.id.editTextContactPhone);
        editTextInfo=(EditText)view.findViewById(R.id.editTextInfo);

        spinnerInfo=(Spinner)view.findViewById(R.id.spinnerInfo);
        ArrayAdapter<CharSequence> arrayAdapter=ArrayAdapter.createFromResource(this.getActivity(), R.array.faultInfo, android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInfo.setAdapter(arrayAdapter);
        spinnerInfo.setPrompt("请选择故障现象。");

        buttonSend=(Button)view.findViewById(R.id.buttonSend);
    }

    private void initEvent() {
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNum=editTextNum.getText().toString().trim();
                requestContact=editTextContact.getText().toString().trim();
                requestContactPhone=editTextContactPhone.getText().toString().trim();
                requestInfo=requestInfo+"\n"+editTextInfo.getText().toString().trim();
                MEID=((TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                //验证各个字段。
                if(requestNum.length()<5){
                    Toast.makeText(getActivity(),"未输入专线号或者专线号过短，请输入专线号，方便故障处理，如果没有专线号，请输入000000000（9个0）",Toast.LENGTH_LONG).show();
                    editTextNum.setFocusable(true);
                    editTextNum.setFocusableInTouchMode(true);
                    editTextNum.requestFocus();
                    return;
                }
                if(requestContact.length()<2){
                    Toast.makeText(getActivity(),"未填写联系人或者联系人名字过短，请输入联系人姓名、称呼，方便故障处理。",Toast.LENGTH_LONG).show();
                    editTextContact.setFocusable(true);
                    editTextContact.setFocusableInTouchMode(true);
                    editTextContact.requestFocus();
                    return;
                }
                if (requestContactPhone.length()<8){
                    Toast.makeText(getActivity(),"未输入联系电话或者联系电话过短，请输入联系电话，方便故障处理。",Toast.LENGTH_LONG).show();
                    editTextContactPhone.setFocusable(true);
                    editTextContactPhone.setFocusableInTouchMode(true);
                    editTextContactPhone.requestFocus();
                    return;
                }

                AddRequestTask addRequestTask=new AddRequestTask();
                addRequestTask.execute(requestNum,requestContact,requestContactPhone,requestInfo,MEID,"");
            }
        });

        spinnerInfo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                requestInfo=adapterView.getItemAtPosition(i).toString().trim();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private class AddRequestTask extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... params) {
            List<BasicNameValuePair> sqlParams = new LinkedList<>();
            sqlParams.add(new BasicNameValuePair("action","addRequest"));
            sqlParams.add(new BasicNameValuePair("requestType","faultReport"));
            sqlParams.add(new BasicNameValuePair("requestNum",params[0]));
            sqlParams.add(new BasicNameValuePair("requestContact",params[1]));
            sqlParams.add(new BasicNameValuePair("requestContactPhone",params[2]));
            sqlParams.add(new BasicNameValuePair("requestInfo",params[3]));
            sqlParams.add(new BasicNameValuePair("MEID",params[4]));
            sqlParams.add(new BasicNameValuePair("memo",params[5]));

            String param = URLEncodedUtils.format(sqlParams, "UTF-8");

            String baseUrl = "http://61.128.177.92/webbind/request.php";

            HttpGet httpGet = new HttpGet(baseUrl+"?"+param);

            HttpClient httpClient = new DefaultHttpClient();
            try{
                HttpResponse httpResponse = httpClient.execute(httpGet);
                if(httpResponse.getStatusLine().getStatusCode()==200){
                    JSONObject jsonObject=new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                    if(jsonObject.getInt("code") == 200){
                        return "故障申告（"+params[0]+"）成功，请等待处理...";
                    }else{
                        return "故障申告（"+params[0]+"）失败，服务器返回的错误为："+
                                jsonObject.getString("message");
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
                return "发生异常，异常信息为："+e.getMessage();
            }

            return null;
        }
        @Override
        protected void onPostExecute(String s){
            Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
            editTextNum.setText("");
            editTextContact.setText("");
            editTextContactPhone.setText("");
        }
    }
}
