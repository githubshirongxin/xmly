package com.ximalaya.ting.android.opensdk.test.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by le.xin on 2017/3/23.
 */

public class MyPlayerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "通知栏按钮接收 == " + intent.getAction(), Toast.LENGTH_SHORT).show();
    }
}
