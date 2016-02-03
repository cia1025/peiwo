package me.peiwo.peiwo.net.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.service.CoreService;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.UserManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class TcpConnection {
	private Socket socket;
	private PacketReader packetReader;
	private PacketWriter packetWriter;
	private DataInputStream reader;
	private OutputStream writer;
	private Context mContext = null;
	private Handler mHandler;
	
	private int csn = 0;
	// 电话消息监听器
	public TcpConnection(Context mContext, Handler mHandler){
		this.mContext = mContext;
		this.mHandler = mHandler;
	}
	
	public boolean connection(String host, int port) {
		try {
			
			CustomLog.v(DfineAction.TCP_TAG, "TcpConnection connection socket" + " host:port=" + host + ":" + port);
			this.socket = new Socket();
			SocketAddress address = new InetSocketAddress(host, port); 
			socket.connect(address, 6000);
			initReaderAndWriter();
			//当非空时，防止旧的还在使用的情况，未申请新的，出现消息错乱，出现情况比较少
			packetWriter = new PacketWriter(writer, observer);
			packetReader = new PacketReader(reader, observer);
		} catch (UnknownHostException e) {
			shutdown();
			e.printStackTrace();
			CustomLog.e(DfineAction.TCP_TAG, "TcpConnection  " + e.toString());
		} catch (IOException e) {
			shutdown();
			e.printStackTrace();
			CustomLog.e(DfineAction.TCP_TAG, "TcpConnection  " + e.toString());
		} catch (Exception e) {
			shutdown();
			e.printStackTrace();
			CustomLog.e(DfineAction.TCP_TAG, "TcpConnection  " + e.toString());
		} finally {
			if (isConnection()) {
				csn = 1;
				startup();
				return true;
			}
		}
		return false;
	}
	
	
	public boolean isConnection() {
		return socket != null ? socket.isConnected() : false;
	}
	
	public int getLocalPort() {
		return isConnection() ? socket.getLocalPort() : -1;
	}

	/**
	 * 初始化网络数据读写对像
	 * @author: Kevin
	 * @version: 2012-6-19 下午04:27:46
	 */
	private void initReaderAndWriter() {
		try {
			reader = new DataInputStream(socket.getInputStream());
			writer = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendPacket(JSONObject message) {
		if (packetWriter != null) {
			try {
				message.put("uid", UserManager.getUid(mContext));
				if (!message.has("ssn")){
					message.put("csn", csn++);
				}
				if (packetWriter != null) {
					packetWriter.sendPacket(message);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void startup() {
		if (packetReader != null) {
			packetReader.startup();
		}
		if (packetWriter != null) {
			packetWriter.startup();
		}
	}

	/**
	 * 
	 * 网络连接关闭时，回收所有网络对像
	 * 
	 * @author: Kevin
	 * @version: 2012-6-19 下午04:28:13
	 */
	public void shutdown() {
		if (isConnection()) {
			CustomLog.v(DfineAction.TCP_TAG, "TCP Close");
		}
		if (packetReader != null) {
			packetReader.shutdown();
			packetReader = null;
		}
		if (packetWriter != null) {
			packetWriter.shutdown();
			packetWriter = null;
		}
		try {
			if (reader != null) {
				reader.close();
				reader = null;
			}
			if (writer != null) {
				writer.close();
				writer = null;
			}
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.gc();
		}
	}
	
	public ConnectObserver observer = new ConnectObserver() {
		@Override
		public void reConnect() {
			((CoreService)mContext).reConnection(CoreService.SOCKET_HOST, CoreService.SOCKET_PORT);
		}
		
		public void handleReceiveMessage(JSONObject msgObject) {
			/*((CoreService)mContext).handleReceiveMsg(msgObject);*/
	        Message message = mHandler.obtainMessage();
	        message.obj = msgObject;
	        mHandler.sendMessage(message);
		}
	};
	
	public static abstract interface ConnectObserver {
		public abstract void reConnect();
		public abstract void handleReceiveMessage(JSONObject msgObject);
	}
	
}
