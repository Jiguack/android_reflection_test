package com.spacepalm.simple2;

import android.os.Bundle;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    public static final String NETWORKMANAGEMENT_SERVICE = "network_management";
    public TextView logText;
    public static Method toggleRadioOnOff;
    public static Method isRinging;
    public static Method getITelephony;
    private TelephonyManager tm;

    private void displayLog(String text) {
        logText.setText(text);
    }

    public void setMobileDataState(boolean mobileDataEnabled)
    {
        try
        {
            TelephonyManager telephonyService = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod)
            {
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
            }
        }
        catch (Exception ex)
        {
            Log.e("PHONE", "Error setting mobile data state", ex);
        }
    }

    public boolean getMobileDataState()
    {
        try
        {
            TelephonyManager telephonyService = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
            if (null != getMobileDataEnabledMethod)
            {
                boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);
                return mobileDataEnabled;
            }
        }
        catch (Exception ex)
        {
            Log.e("PHONE", "Error getting mobile data state", ex);
        }
        return false;
    }

    public void setRadioPower(boolean radioPowerEnabled) {
        boolean ret;
        try {
            Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{TELEPHONY_SERVICE});
            ITelephony telephony = ITelephony.Stub.asInterface(binder);
            ret = telephony.setRadioPower(radioPowerEnabled);
            displayLog("Good " + String.valueOf(ret));
        } catch (Exception e) {
            displayLog("Error " + e.toString());
        }
    }

    public void setDataQuota(int size) {
        String all = "";
        try {

            INetworkManagementService mNMService;

            all = "Size :" + String.valueOf(size) + "MB\n";

            Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{NETWORKMANAGEMENT_SERVICE});
            mNMService = INetworkManagementService.Stub.asInterface(binder);

            final String[] ifaces = mNMService.listInterfaces();

            for (int i = 0; i < ifaces.length; i++) {
                all+= "[" + String.valueOf(i) + "]" + ifaces[i] +"\n";
                mNMService.removeInterfaceQuota(ifaces[i]);
                mNMService.setInterfaceQuota(ifaces[i], size*1024*1024);
            }
            mNMService.setGlobalAlert(size*1024*1024);
            displayLog(String.valueOf(ifaces.length) +" " + all);

        } catch (Exception e) {
            displayLog(all + "Error :" + e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tm =  (TelephonyManager)getSystemService(TELEPHONY_SERVICE);

        logText = (TextView) findViewById(R.id.textLog);
        logText.setText("Ready");

        // lookup methods and fields not defined publicly in the SDK.
        /*
        Class<?> cls = TelephonyManager.class;
        for (Method method : cls.getDeclaredMethods()) {
            String methodName = method.getName();
            if (methodName.equals("toggleRadioOnOff")) {
                toggleRadioOnOff = method;
                method.setAccessible(true);
            } else if  (methodName.equals("isRinging")) {
                isRinging = method;
                method.setAccessible(true);
            } else if (methodName.equals("getITelephony")) {
                getITelephony = method;
                method.setAccessible(true);
            }
        }
        */

        //MODEM OFF
        findViewById(R.id.btnOff).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        setRadioPower(false);
                    }
                }
        );

        //MODEM ON
        findViewById(R.id.btnOn).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        setRadioPower(true);
                    }
                }
        );



        //DATA ON
        findViewById(R.id.btnDataOn).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        setMobileDataState(true);
                        if (getMobileDataState())
                            displayLog("MOBILE DATA ON");
                    }
                }
        );

        //DATA OFF
        findViewById(R.id.btnDataOff).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        setMobileDataState(false);
                        if (!getMobileDataState())
                            displayLog("MOBILE DATA OFF");
                    }
                }
        );

        //SET QUOTA
        findViewById(R.id.btnSetQuota).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        int qSize = 0;
                        String log;
                        String limitMb = ((TextView)findViewById(R.id.textQuota)).getText().toString();
                        qSize = Integer.valueOf(limitMb).intValue();
                        displayLog("LIMIT SIZE: " + limitMb + "MB" );
                        if (qSize > 0)
                            setDataQuota(qSize);
                    }
                }
        );

    }
}
