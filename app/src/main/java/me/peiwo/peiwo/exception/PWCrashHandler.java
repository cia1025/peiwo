package me.peiwo.peiwo.exception;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.util.PWUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fuhaidong on 14-9-19.
 */
public class PWCrashHandler implements Thread.UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler mDefaultHandler;

	private PWCrashHandler() {
	}
	
	
	private static PWCrashHandler mPWCrashHandler;

	public synchronized static PWCrashHandler getInstance() {
		if (mPWCrashHandler == null)
			mPWCrashHandler = new PWCrashHandler();
		return mPWCrashHandler;
	}

	public void init(Context context) {
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			// 退出程序
			if (ex != null)
				ex.printStackTrace();
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(-1);
		}
	}

	private boolean handleException(Throwable ex) {
		System.err.println("handleException");
		collectDeviceInfo(mContext);
		saveCatchInfo2File(ex);
		return ex != null;
	}

	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {

		}

		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				// System.err.println(field.getName() + " : " +
				// field.get(null));
			} catch (Exception e) {
				System.err.println("an error occured when collect crash info: "
						+ e);
			}
		}
	}

	private void saveCatchInfo2File(Throwable ex) {
		System.out.println("saveCatchInfo2File");
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}

		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		try {
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		sb.append(result);
		try {
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String fileName = "crash-" + time + "-" + timestamp + ".log";

			File dirBase = new File(Environment.getExternalStorageDirectory(), "crash");
			if (!dirBase.exists()) {
				dirBase.mkdirs();
			}

			File pathCrashFile = new File(dirBase, fileName);
			pathCrashFile.createNewFile();
			
			FileOutputStream fos = new FileOutputStream(pathCrashFile);
			fos.write(sb.toString().getBytes());

			fos.close();
	        PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					PWUtils.upLoadToServer();				
				}
			});
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// 程序的Context对象
	private Context mContext;
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();

	// 用于格式化日期,作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");


}
