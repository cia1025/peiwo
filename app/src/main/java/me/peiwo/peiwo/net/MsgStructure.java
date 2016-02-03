package me.peiwo.peiwo.net;

import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

public abstract class MsgStructure {

	public int type;
	public List<NameValuePair> paramList;
	public String requestUrl;
	public String errorMessage;
	public long id;

	public abstract void onReceive(JSONObject data);

	public abstract void onError(int error, Object ret);

	public boolean onInterceptRawData(String rawStr) {
		return false;
	}
}
