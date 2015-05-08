package com.x.rokhdelar.xassistant;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;



public class FaultReportActivity extends FragmentActivity implements View.OnClickListener {
    private LinearLayout linearLayoutFaultReport,linearLayoutFaultResult;
    private ImageButton imageButtonFaultReport,imageButtonFaultResult;
    private Fragment fragmentFaultReport,fragmentFaultResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fault_report);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        initView();

        initEvent();

        setSelect(0);

    }

    private void initEvent() {
        linearLayoutFaultReport.setOnClickListener(this);
        linearLayoutFaultResult.setOnClickListener(this);
    }

    private void initView() {
        linearLayoutFaultReport=(LinearLayout)findViewById(R.id.lineLayoutFaultReport);
        linearLayoutFaultResult=(LinearLayout)findViewById(R.id.lineLayoutFaultResult);

        imageButtonFaultReport=(ImageButton)findViewById(R.id.imageButtonFaultReport);
        imageButtonFaultResult=(ImageButton)findViewById(R.id.imageButtonFaultResult);

    }

    //��ȡ�豸MEID��
    private String getMEID() {
        TelephonyManager telephonyManager=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fault_report, menu);
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

    @Override
    public void onClick(View view) {
        resetImage();
        switch (view.getId()){
            case R.id.lineLayoutFaultReport:
                setSelect(0);
                break;
            case R.id.lineLayoutFaultResult:
                setSelect(1);
                break;
        }
    }

    private void setSelect(int i) {
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        hideFragment(fragmentTransaction);
        switch (i){
            case 0:
                imageButtonFaultReport.setImageResource(R.mipmap.ic_selectfaultreport);
                if(fragmentFaultReport==null){
                    fragmentFaultReport=new FaultReportFragment();
                    fragmentTransaction.add(R.id.fragmentContainer,fragmentFaultReport);
                }else{
                    fragmentTransaction.show(fragmentFaultReport);
                }
                break;
            case 1:
                imageButtonFaultResult.setImageResource(R.mipmap.ic_selectfaultresult);
                if(fragmentFaultResult==null){
                    fragmentFaultResult=new FaultResultFragment();
                    fragmentTransaction.add(R.id.fragmentContainer,fragmentFaultResult);
                }else{
                    fragmentTransaction.show(fragmentFaultResult);
                }
                break;
        }
        fragmentTransaction.commit();
    }

    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if(fragmentFaultReport!=null){
            fragmentTransaction.hide(fragmentFaultReport);
        }
        if (fragmentFaultResult!=null){
            fragmentTransaction.hide(fragmentFaultResult);
        }
    }

    private void resetImage() {
        imageButtonFaultReport.setImageResource(R.mipmap.ic_unselectedfaultreport);
        imageButtonFaultResult.setImageResource(R.mipmap.ic_unselectedfaultresult);
    }
}
