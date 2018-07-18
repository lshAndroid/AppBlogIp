package lsh.com.blogipset.iputil.android7ip;

import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Preeth on 12/7/17
 */

public class ProxySettings {

    private enum PROXY_TYPE {
        MANUAL,
        PAC_URL
    }

    // set manual proxy
    public static WifiConfiguration setManualProxy(WifiConfiguration conf, String hostname, int port, List<String> bypass) {
        return setProxy(conf, hostname, port, bypass, PROXY_TYPE.MANUAL);
    }

    // set pac url
    public static WifiConfiguration setPacURL(WifiConfiguration conf, String URL) {
        return setProxy(conf, URL, 0, null, PROXY_TYPE.PAC_URL);
    }

    // Proxy Settings For Wifi
    @SuppressWarnings("unchecked")
    private static WifiConfiguration setProxy(WifiConfiguration conf, String hostname, int port, List<String> bypass, PROXY_TYPE type) {
        try {
            //linkProperties is no longer in WifiConfiguration
            Class proxyInfoClass = Class.forName("android.net.ProxyInfo");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyInfoClass;
            Class wifiConfigClass = Class.forName("android.net.wifi.WifiConfiguration");
            Method setHttpProxy = wifiConfigClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            //Get the ENUM ProxySettings in IpConfiguration
            Class ipConfigClass = Class.forName("android.net.IpConfiguration");
            Field f = ipConfigClass.getField("proxySettings");
            Class proxySettingsClass = f.getType();

            Class[] setProxySettingsParams = new Class[1];
            setProxySettingsParams[0] = proxySettingsClass;
            Method setProxySettings = wifiConfigClass.getDeclaredMethod("setProxySettings", setProxySettingsParams);
            setProxySettings.setAccessible(true);


            ProxyInfo pi = null;
            String Type = null;
            switch (type) {
                case MANUAL:
                    Type = "STATIC";
                    pi = ProxyInfo.buildDirectProxy(hostname, port, bypass);
                    break;

                case PAC_URL:
                    Type = "PAC";
                    pi = ProxyInfo.buildPacProxy(Uri.parse(hostname));
            }

            //pass the new object to setHttpProxy
            Object[] params_SetHttpProxy = new Object[1];
            params_SetHttpProxy[0] = pi;
            setHttpProxy.invoke(conf, params_SetHttpProxy);

            //pass the enum to setProxySettings
            Object[] params_setProxySettings = new Object[1];
            params_setProxySettings[0] = Enum.valueOf((Class<Enum>) proxySettingsClass, Type);
            setProxySettings.invoke(conf, params_setProxySettings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conf;
    }

}
