package me.peiwo.peiwo.net.tcp;

import com.alibaba.fastjson.JSON;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.GZIPOutputStream;

import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.net.tcp.TcpConnection.ConnectObserver;
import me.peiwo.peiwo.util.CustomLog;

import org.json.JSONObject;


/**
 * 
 * 消息写入类
 * 
 * @author: Kevin
 * @version: 2012-6-19 下午02:28:15
 */
public class PacketWriter {
	public static int pingCount = 0 ;
	
	//private Queue<JSONObject> queue = new LinkedList<JSONObject>();
	private Queue<JSONObject> queue = new ConcurrentLinkedQueue<JSONObject>();
	private Thread writerThread;
	protected boolean done = false; // 客户端是否已经断线
	private OutputStream writer;
	private ConnectObserver observer = null;
	
	public PacketWriter(OutputStream writer, ConnectObserver observer) {
		this.observer = observer;
		done = false;
		this.writer = writer;
		writerThread = new Thread() {
			@Override
			public void run() {
				writePackets(this);
			}
		};
		// 标识为守护线程
		writerThread.setDaemon(true);
	}

	/**
	 * 
	 * 发送数据包
	 * 
	 * @param thisThread
	 * @author: Kevin
	 * @version: 2012-6-21 下午02:48:40
	 */
	private void writePackets(Thread thisThread) {
		while (!done && thisThread == writerThread) {
			JSONObject jsonObject = nextPacket();
			if (jsonObject != null) {
				try {
					byte[] commandData = null;
					String jsonCMD = jsonObject.toString();
					CustomLog.i(DfineAction.TCP_TAG, "---> " + jsonCMD);
					boolean needCompression = false;
					if (jsonObject.optInt("msg_type") == DfineAction.MSG_ExchangeInfo){
						needCompression = true;
					}
					if (needCompression) {
						commandData = compressionGZip(jsonCMD.getBytes());
					} else {
						commandData = jsonCMD.getBytes();
					}
			        int commandLen = commandData.length;
			        int headLength = 64;
			        byte[] send_data = new byte[commandLen + headLength];
			        int index = 0;
			        {
			            send_data[index++] = (byte)0xFF;
			            send_data[index++] = (byte)0xFE;
			            send_data[index++] = (byte)0xFF;
			            send_data[index++] = (byte)0xFE;
			        }
			        {
			        	int ver = (int)0x00001000;
			        	send_data[index++] = (byte) ((ver >> 24) & 0xFF);
			            send_data[index++] = (byte) ((ver >> 16) & 0xFF);
			            send_data[index++] = (byte) ((ver >> 8) & 0xFF);
			            send_data[index++] = (byte) (ver & 0xFF);
			        }
			        {
			        	int compression = 0x00000001;
			        	if (needCompression) {
			        		compression = 0x00000002;
			        	}
			        	send_data[index++] = (byte) ((compression >> 24) & 0xFF);
			            send_data[index++] = (byte) ((compression >> 16) & 0xFF);
			            send_data[index++] = (byte) ((compression >> 8) & 0xFF);
			            send_data[index++] = (byte) (compression & 0xFF);
			        }
			        {
			        	send_data[index++] = (byte) ((commandLen >> 24) & 0xFF);
			            send_data[index++] = (byte) ((commandLen >> 16) & 0xFF);
			            send_data[index++] = (byte) ((commandLen >> 8) & 0xFF);
			            send_data[index++] = (byte) (commandLen & 0xFF);
			        }
			        {
			        	int os = 0x00001001;
			        	send_data[index++] = (byte) ((os >> 24) & 0xFF);
			            send_data[index++] = (byte) ((os >> 16) & 0xFF);
			            send_data[index++] = (byte) ((os >> 8) & 0xFF);
			            send_data[index++] = (byte) (os & 0xFF);
			        }
			        for (; index < headLength; index++) {
			        	send_data[index] = (byte)0x0;
					}
			        
			        System.arraycopy(commandData, 0, send_data, headLength, commandData.length);
					writer.write(send_data);
					writer.flush();
				} catch (Exception e) {
					CustomLog.e(DfineAction.TCP_TAG, "PacketWriter 发送信息异常:" + e.getMessage());
					e.printStackTrace();
					break;
				}
			}
		}
		queue.clear();
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!done) {
			observer.reConnect();
		}
	}

	public void sendPacket(JSONObject message) {
		if (!done) {
			synchronized (queue) {
				queue.add(message);
				queue.notifyAll();
			}
		}
	}
	
	/**
	 * 
	 * 获取下一个要发送的数据包
	 * 
	 * @return
	 * @author: Kevin
	 * @version: 2012-6-27 下午05:23:10
	 */
	public JSONObject nextPacket() {
		JSONObject packet = null;
		if (!done && (queue.size() == 0)) {
			synchronized (queue) {
				try {
					queue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
			packet = queue.poll();

		return packet;
	}

	private byte[] compressionGZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bis = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(bis);
			gzip.write(data);
			gzip.flush();
			bis.flush();
			gzip.close();
			b = bis.toByteArray();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}
	
	public void startup() {
		writerThread.start();
	}

	public void shutdown() {
		done = true;
		synchronized (queue) {
			queue.clear();
			queue.notifyAll();
		}
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
