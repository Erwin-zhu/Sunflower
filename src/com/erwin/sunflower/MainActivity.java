package com.erwin.sunflower;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	private static String TAG = MainActivity.class.getSimpleName();
	private String IP = "192.168.1.102";
	private int PORT = 7289;

	public static final int MAX_WIDTH = 640;
	public static final int MAX_HEIGHT = 480;
	private SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	private Camera camera;
	boolean isPreview = false;
	
	// ʵ���Զ��۽�����
	private Camera.AutoFocusCallback mAutoFocusCallBack;
	private Camera.PreviewCallback previewCallback;

	/** call when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//���ô���Ϊȫ��
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		surfaceView = (SurfaceView) findViewById(R.id.sv_image);
		surfaceHolder = surfaceView.getHolder();

		//���������ز�������ʾԤ��
		surfaceHolder.addCallback(new Callback() // surfaceHolder���һ���ص�������
		{

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int widht, int height) 
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) 
			{
				// TODO Auto-generated method stub
				if (!isPreview) 
				{
					camera = Camera.open();
				}

				if (camera != null && !isPreview) 
				{
					try 
					{
						Camera.Parameters parameters = camera.getParameters();

						parameters.setPreviewSize(MAX_WIDTH, MAX_HEIGHT); // ��ƬԤ����С
						parameters.setPreviewFpsRange(20, 30); // ÿ����ʾ֡��
						parameters.setPictureFormat(ImageFormat.NV21); // ͼ���ʽ
						parameters.setPictureSize(MAX_WIDTH, MAX_HEIGHT); // ͼƬ��С

						camera.setPreviewDisplay(surfaceHolder); // ����ȡ������
						previewCallback = new StreamIt(IP, PORT);
						camera.setPreviewCallback(previewCallback); // ���ûص�����
						camera.startPreview(); // ��ʼԤ��
						camera.autoFocus(null); // �Զ��Խ�
					} catch (Exception e) 
					{
						e.printStackTrace();
					}
					isPreview = true;
				}

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) 
			{
				// TODO Auto-generated method stub
				if (camera != null) 
				{
					if (isPreview)
						camera.stopPreview();
					camera.release();
					camera = null;
				}
				System.exit(0);
			}

		});
		// ���ø�SurfaceView�Լ���ά��������
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		//menu.add(Menu.NONE, Menu.FIRST + 1, 5, "ɾ��").setIcon(android.R.drawable.ic_menu_delete);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}	
	//�˵�ע���¼�
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) 
		{
		case R.id.menu_help:
			Toast.makeText(this, "�����˵��������", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_set:
			Toast.makeText(this, "���ò˵��������", Toast.LENGTH_SHORT).show();
			showSettings();
			break;
		}
		return isPreview;
		
	}
	//��Ӧ�˵�������ѡ��
	private void showSettings()
	{
		/*
		//��ϵͳ����
		Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
		settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		startActivity(settings);
		*/
		Intent settings = new Intent();
		settings.setClass(MainActivity.this, AppPreferenceActivity.class);
		MainActivity.this.startActivity(settings);
	}
	
	//��������
	public void startService(View View)
	{
		startService(new Intent(getBaseContext(), MainService.class));
	}
	
	//ֹͣ����
	public void stopService(View view)
	{
		stopService(new Intent(getBaseContext(), MainService.class));
	}

	
	//�����¼�����
	public boolean onTouchEvent(MotionEvent event)
	{
		//����ʱ�Զ��Խ�
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			camera.autoFocus(null);
		}
		return true;
	}
}

class StreamIt implements Camera.PreviewCallback 
{
	private String ip;
	private int port;

	public StreamIt(String ip, int port) 
	{
		this.ip = ip;
		this.port = port;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) 
	{
		// TODO Auto-generated method stub
		// ԭʼͼͼƬת����jpeg���������ݵ�������
		Size size = camera.getParameters().getPreviewSize();
		try 
		{
			YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
					size.height, null);
			if (image != null) 
			{
				ByteArrayOutputStream ba_Outstream = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, size.width, size.height),
						80, ba_Outstream);
				ba_Outstream.flush();

				Thread th = new SocketThread(ba_Outstream, ip, port);
				th.start();
			}
		} catch (Exception e) 
		{
			Log.e("ex", "send picture to server failed" + e.getMessage());
		}
	}
}

//����ͼ�����ݲ����͵���������
class SocketThread extends Thread 
{
	private byte byteBuffer[] = new byte[1024];
	private OutputStream outStream;
	private ByteArrayOutputStream ba_OutputStream;
	private String ip;
	private int port;

	public SocketThread(ByteArrayOutputStream myOutputStream, String ip, int port) 
	{
		this.ba_OutputStream = myOutputStream;
		this.ip = ip;
		this.port = port;
		try 
		{
			myOutputStream.close();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void run() 
	{
		try 
		{
			Socket tmpSocket = new Socket(ip, port);
			outStream = tmpSocket.getOutputStream();
			ByteArrayInputStream inputStream = new ByteArrayInputStream(
					ba_OutputStream.toByteArray());

			int numRead = 0;
			while ((numRead = inputStream.read(byteBuffer)) != -1) {
				outStream.write(byteBuffer, 0, numRead);
			}
			ba_OutputStream.flush();
			ba_OutputStream.close();
			tmpSocket.close();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	
}
