package com.erwin.sunflower;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MainService extends Service
{
	private static final String TAG = "MainService";
	
	@Override
	public IBinder onBind(Intent arg0) 
	{
		// TODO Auto-generated method stub
		Log.i(TAG, "Service onBind");
		return null;
	}
	
	@Override
	public  int onStartCommand(Intent intent, int flags, int startId)
	{
		Toast.makeText(this, "onStartCommand: Service started", Toast.LENGTH_LONG).show();
		return startId;
		
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Toast.makeText(this, "onDestroy(): Service destroyed", Toast.LENGTH_LONG).show();
	}

}
