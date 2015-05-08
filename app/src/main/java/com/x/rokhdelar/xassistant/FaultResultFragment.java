package com.x.rokhdelar.xassistant;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class FaultResultFragment extends Fragment {
    private ListView listViewFaultList,listViewFaultDetail;
    private ArrayList<HashMap<String,Object>> arrayListFaultList,arrayListFaultDetail;
    private Handler handler;
    private Runnable runnable;
    private String MEID;
    private SimpleAdapter simpleAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_fault_result, container, false);
        initView(view);
        initEvent();
        return view;
    }

    private void initView(View view) {
        listViewFaultList=(ListView)view.findViewById(R.id.listViewFaultList);
        listViewFaultDetail=(ListView)view.findViewById(R.id.listViewFaultDetail);

        MEID=((TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        arrayListFaultList=new ArrayList<>();
        arrayListFaultDetail=new ArrayList<>();

        GetRequestListTask getRequestListTask=new GetRequestListTask();
        getRequestListTask.execute("getRequestByMEID","faultReport",MEID);
        //1分钟刷新一次。
        handler=new Handler();
        runnable=new Runnable() {
            @Override
            public void run() {
                new GetRequestListTask().execute("getRequestByMEID","faultReport",MEID);
                handler.postDelayed(runnable, 60 * 1000);
            }
        };
        handler.postDelayed( runnable,60*1000);
    }

    private void initEvent() {
        listViewFaultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayList<String> arrayListDetail=new ArrayList<String>();
                HashMap<String,Object> map=(HashMap<String,Object>)adapterView.getItemAtPosition(i);
                String[] faultDetailInfo = new String[8];
                faultDetailInfo[0]="专线号码："+map.get("requestNum").toString();
                faultDetailInfo[1]="处理情况："+map.get("requestState").toString();
                faultDetailInfo[2]="联系人："+map.get("requestContact").toString();
                faultDetailInfo[3]="联系电话："+map.get("requestContactPhone").toString();
                faultDetailInfo[4]="故障现象："+map.get("requestInfo").toString();
                faultDetailInfo[5]="申告时间："+map.get("requestTime").toString();
                faultDetailInfo[6]="处理结果："+map.get("requestResult").toString();
                faultDetailInfo[7]="处理完成时间："+map.get("handleTime").toString();

                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(
                        getActivity(),
                        android.R.layout.simple_list_item_1,
                        faultDetailInfo);
                listViewFaultDetail.setAdapter(arrayAdapter);
            }
        });
    }

    private class GetRequestListTask extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... params) {
            arrayListFaultList.clear();
            arrayListFaultDetail.clear();
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
                                case 0://未确认。
                                    map.put("requestStateImg",R.mipmap.ic_unkown);
                                    break;
                                case 1://已确认，未处理完毕。
                                    map.put("requestStateImg",R.mipmap.ic_processing);
                                    break;
                                case 2://处理完毕。
                                    map.put("requestStateImg",R.mipmap.ic_ok);
                                    break;
                                default:
                                    map.put("requestState",R.mipmap.ic_unkown);
                            }
                            map.put("requestResult",jsonArray.getJSONObject(i).getString("requestResult"));
                            map.put("requestType","政企故障:");
                            map.put("requestNum",jsonArray.getJSONObject(i).getString("requestNum"));
                            map.put("requestContact",jsonArray.getJSONObject(i).getString("requestContact"));
                            map.put("requestContactPhone",jsonArray.getJSONObject(i).getString("requestContactPhone"));
                            map.put("requestTime","请求时间："+jsonArray.getJSONObject(i).getString("requestTime"));
                            map.put("requestInfo",jsonArray.getJSONObject(i).getString("requestInfo"));
                            map.put("isNotified",jsonArray.getJSONObject(i).getString("isNotified"));
                            map.put("id",jsonArray.getJSONObject(i).getInt("id"));
                            map.put("handleTime",jsonArray.getJSONObject(i).getString("handleTime"));
                            arrayListFaultList.add(map);
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
            if(simpleAdapter==null){
                simpleAdapter= new SimpleAdapter(getActivity(),
                        arrayListFaultList,
                        R.layout.faultlist_layout,
                        new String[]{"requestStateImg","requestType","requestNum","requestTime","requestResult"},
                        new int[]{R.id.requestStateImg,R.id.requestType,R.id.requestNum,R.id.requestTime,R.id.requestResult});

                listViewFaultList.setAdapter(simpleAdapter);
            }else{
                simpleAdapter.notifyDataSetChanged();
            }
            //检查是否已经通知，没有通知的发出通知。
            for(HashMap<String,Object> map:arrayListFaultList){
                if ((map.get("requestState")!=0) && (map.get("isNotified").toString().equals("NO"))){
                    String resultText=getString(R.string.app_name)+":"+map.get("requestNum").toString()+"解绑已经处理完毕。";
                    //添加通知
                    NotificationManager notificationManager=(NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notification=new Notification.Builder(getActivity())
                            .setAutoCancel(true)
                            .setWhen(System.currentTimeMillis())
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(resultText)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentInfo(null)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .build();

                    notificationManager.notify((int)map.get("id"),notification);

                    Toast.makeText(getActivity(), resultText, Toast.LENGTH_LONG).show();

                    //更新数据库的isNotified字段.
                    //new UpdateNotifyTask().execute("updateNotified",map.get("id").toString());
                }
            }

            super.onPostExecute(s);
        }
    }
}
