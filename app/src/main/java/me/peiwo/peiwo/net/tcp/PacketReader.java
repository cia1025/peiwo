package me.peiwo.peiwo.net.tcp;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.net.tcp.TcpConnection.ConnectObserver;
import me.peiwo.peiwo.util.CustomLog;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 
 * 消息读取类
 * 
 * @author: Kevin
 * @version: 2012-6-19 下午02:28:02
 */
public class PacketReader {
	private DataInputStream reader;
	private Thread readerThread;
	protected boolean done = false; // 客户端是否已经断线
	private ConnectObserver observer = null;
	
	public PacketReader(DataInputStream reader, ConnectObserver observer) {
		this.reader = reader;
		this.observer = observer;
		readerThread = new Thread(mRunnable);
		// 标识为守护线程
		readerThread.setDaemon(true);
	}

	private Runnable mRunnable = new Runnable() {
		public void run() {
			CustomLog.v(DfineAction.TCP_TAG, "--------------START READER THREAD-----------------");
			while (!done) {
				try {
					
					byte[] tag = new byte[4];
					int length = reader.read(tag);
					if (length == -1) {
						reader.skip(reader.available());
						break;
					}
					int tagInt = ((int) ((tag[0] << 24) & 0xFF000000)) + ((int)((tag[1] << 16) & 0xFF0000))
							+ ((int) ((tag[2] << 8) & 0xFF00)) + (int) (tag[3] & 0xFF);
					if (tagInt != (int)0xFFFEFFFE) {
						if (tagInt > 0) {
							reader.skip(tagInt);
						}
						continue;
					}
					if (reader.available() < 60) {
						reader.skip(reader.available());
						continue;
					}
					byte[] head = new byte[60];
					length = reader.read(head);
					if (length == -1) {
						reader.skip(reader.available());
						break;
					}
					int index = 0;
					int verInt = ((int) ((head[index++] << 24) & 0xFF000000)) + ((int)((head[index++] << 16) & 0xFF0000))
							+ ((int) ((head[index++] << 8) & 0xFF00)) + (int) (head[index++] & 0xFF);
					if (verInt != (int)0x00001000) {
						reader.skip(reader.available());
						continue;
					}
					int compressionInt = ((int) ((head[index++] << 24) & 0xFF000000)) + ((int)((head[index++] << 16) & 0xFF0000))
							+ ((int) ((head[index++] << 8) & 0xFF00)) + (int) (head[index++] & 0xFF);
					
					if (compressionInt != (int)0x00000000 && compressionInt != (int)0x00000001
							&& compressionInt != (int)0x00000002) {
						reader.skip(reader.available());
						continue;
					}
					int bodyLength = ((int) ((head[index++] << 24) & 0xFF000000)) + ((int)((head[index++] << 16) & 0xFF0000))
							+ ((int) ((head[index++] << 8) & 0xFF00)) + (int) (head[index++] & 0xFF);

					int os = ((int) ((head[index++] << 24) & 0xFF000000)) + ((int)((head[index++] << 16) & 0xFF0000))
							+ ((int) ((head[index++] << 8) & 0xFF00)) + (int) (head[index++] & 0xFF);
					if (os != (int) 0x00001001) {
						reader.skip(reader.available());
						continue;
					}
					CustomLog.v(DfineAction.TCP_TAG, "PacketReader read bodyLength = " + bodyLength);
					byte[] body = null;
					if (bodyLength > 0) {// 读报文体
						body = new byte[bodyLength];
						byte[] buf = new byte[1024];
						int resdLength = 0;
						int rl = 0;
						while (resdLength < bodyLength) {
							int lastLength = bodyLength - resdLength;
							if (lastLength > 1024) {
								rl = reader.read(buf);
							} else {
								rl = reader.read(buf, 0, lastLength);
							}
							if (rl >= 0) {
							   System.arraycopy(buf, 0, body, resdLength, rl);
							   resdLength += rl;
							}
							rl = 0;
						}
					}
					if (!done) {
				        try {
				        	String commandStr = "";
							if (compressionInt == (int)0x00000002) {
								commandStr = new String(decompressionGZip(body));
							} else {
								commandStr = new String(body);
							}
				        	CustomLog.v(DfineAction.TCP_TAG, "<--- " + commandStr);
				        	JSONObject command = new JSONObject(commandStr);
				        	observer.handleReceiveMessage(command);
				        } catch (JSONException e) {
				        	CustomLog.e(DfineAction.TCP_TAG, "PacketReader 消息解析异常：" + e.toString());
				        	e.printStackTrace();
				        }
					} else {
						CustomLog.e(DfineAction.TCP_TAG, "PacketReader 消息分发异常");
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					CustomLog.e(DfineAction.TCP_TAG, "PacketReader  消息操作有异常:" + e.toString());
					break;
				}
			}
			CustomLog.e(DfineAction.TCP_TAG, "------done : " + done);
			if (!done) {
				observer.reConnect();
			}
		}
	};

	private byte[] decompressionGZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			GZIPInputStream gzip = new GZIPInputStream(bis);
			
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = gzip.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			b = baos.toByteArray();
			baos.flush();
			baos.close();
			gzip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}
	
	public void startup() {
		readerThread.start();
	}

	public void shutdown() {
		done = true;
		try {
			reader.close();
			reader = null;
			readerThread.interrupt();
			readerThread = null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
