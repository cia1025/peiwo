package me.peiwo.peiwo.db;

import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.SharedPreferencesUtil;

public class PWConfig {

    protected PWHTTPSvrInfo m_oHTTPSvr = null;
    protected PWSvrInfo m_oTCPSvr = null;
    private List<UsedDomain> domains = new ArrayList<UsedDomain>();

    public List<UsedDomain> getDomains() {
        return domains;
    }

    public final class UsedDomain {
        public boolean isused;
        public String domain;

        public UsedDomain(boolean isused, String domain) {
            this.domain = domain;
            this.isused = isused;
        }
    }

    public PWConfig() {
        PeiwoApp app = PeiwoApp.getApplication();
        UsedDomain domain = new UsedDomain(false, "https://api.raybo.com:2443");
        domains.add(domain);
        domain = new UsedDomain(true, "https://api.peiwoapi.com:443");
        domains.add(domain);
        m_oHTTPSvr = new PWHTTPSvrInfo();
        String httpServers = SharedPreferencesUtil.getStringExtra(PeiwoApp.getApplication(), AsynHttpClient.HTTP_SERVERS, "");
        CustomLog.d("peiwo app instance is : " + app);
        if(app != null) {
            if(app.isDebuggable()){
                httpServers = "";
            }
        }

        if (TextUtils.isEmpty(httpServers) && domains.size() > 0) {
            UsedDomain defaultDomain = domains.get(0);
            m_oHTTPSvr.m_strHostName = defaultDomain.domain;
        } else {
            try {
                JSONArray array = new JSONArray(httpServers);
                m_oHTTPSvr.m_strHostName = array.getString(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Uri uri=Uri.parse(m_oHTTPSvr.m_strHostName);

        m_oHTTPSvr.m_nPort = uri.getPort();
        m_oHTTPSvr.m_strHTTPType = uri.getScheme();
        m_oHTTPSvr.m_strHostName = uri.getHost();

        m_oTCPSvr = new PWSvrInfo();
        m_oTCPSvr.m_strHostName = "hub-re.peiwoapi.com";
        m_oTCPSvr.m_nPort = 8900;

    }

    public PWHTTPSvrInfo GetHTTPSvr() {
        return m_oHTTPSvr;
    }

    public PWSvrInfo GetTCPSvr() {
        return m_oTCPSvr;
    }

    public void setHostName(String HTTPSvr) {
        Uri uri=Uri.parse(HTTPSvr);

        m_oHTTPSvr.m_nPort = uri.getPort();
        m_oHTTPSvr.m_strHTTPType = uri.getScheme();
        m_oHTTPSvr.m_strHostName = uri.getHost();
    }

    public void setHostPort(int port) {
        m_oHTTPSvr.m_nPort = port;
    }

    public void setHostType(String type) {
        m_oHTTPSvr.m_strHTTPType = type;
    }

    public class PWSvrInfo {
        public String m_strHostName = "";
        public int m_nPort = 80;
    }

    public class PWHTTPSvrInfo extends PWSvrInfo {
        public String m_strHTTPType = "https";
    }

    public String toString() {
        return "\n HttpServer is " +
                "\n host : "+m_oHTTPSvr.m_strHostName+
                "\n port : "+m_oHTTPSvr.m_nPort+
                "\n TCPServer is "+
                "\n host : "+m_oTCPSvr.m_strHostName+
                "\n port : "+m_oTCPSvr.m_nPort;
    }
}
