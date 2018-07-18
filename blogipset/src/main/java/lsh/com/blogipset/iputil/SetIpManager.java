package lsh.com.blogipset.iputil;

import android.content.ContentResolver;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.LinkAddress;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;


public class SetIpManager {
    private Context mContext;
    public SetIpManager(Context context) {
        mContext = context;
    }



    public String getIpStr(){
        WifiManager wifiManager = (WifiManager)mContext.getSystemService(WIFI_SERVICE);
        WifiInfo w=wifiManager.getConnectionInfo();
        return intToIp(w.getIpAddress());
    }
    public String intToIp(int ipAddress) {
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));

    }
    private int netmaskIpL;
    public String getWifiSetting(){
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        DhcpInfo dhcpInfo=wifiManager.getDhcpInfo();
        netmaskIpL=dhcpInfo.netmask;
        if(dhcpInfo.leaseDuration==0){
            return "StaticIP";
        }else{
            return "DHCP";
        }
    }
//    /**
//     * 网关 。
//     *
//     * @param ip
//     * @return
//     */
//    public String long2ip(long ip) {
//        StringBuffer sb = new StringBuffer();
//        sb.append(String.valueOf((int) (ip & 0xff)));
//        sb.append('.');
//        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
//        sb.append('.');
//        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
//        sb.append('.');
//        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
//        return sb.toString();
//    }
//
//    public String intToIp(int ipAddress) {
//        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
//                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
//    }

    /**
     * 设置静态ip地址的方法
     */
    public boolean setIpWithTfiStaticIp(boolean dhcp, String ip, int prefix, String dns1, String gateway) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        boolean flag=false;
        if (!wifiManager.isWifiEnabled()) {
            // wifi is disabled
            return flag;
        }
        // get the current wifi configuration
        WifiConfiguration wifiConfig = null;
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration conf : configuredNetworks) {
                if (conf.networkId == connectionInfo.getNetworkId()) {
                    wifiConfig = conf;
                    break;
                }
            }
        }
        if (wifiConfig == null) {
            // wifi is not connected
            return flag;
        }
        if (Build.VERSION.SDK_INT < 11) { // 如果是android2.x版本的话
            ContentResolver ctRes = mContext.getContentResolver();
            Settings.System.putInt(ctRes,
                    Settings.System.WIFI_USE_STATIC_IP, 1);
            Settings.System.putString(ctRes,
                    Settings.System.WIFI_STATIC_IP, ip);
            flag=true;
            return flag;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // 如果是android3.x版本及以上的话
            try {
                IPSetUtils.setIpAssignment("STATIC", wifiConfig);
                IPSetUtils.setIpAddress(InetAddress.getByName(ip), prefix, wifiConfig);
                IPSetUtils.setGateway(InetAddress.getByName(gateway), wifiConfig);
                IPSetUtils.setDNS(InetAddress.getByName(dns1), wifiConfig);
                int netId = wifiManager.updateNetwork(wifiConfig);
                boolean result =  netId!= -1; //apply the setting
                if(result){
                    boolean isDisconnected =  wifiManager.disconnect();
                    boolean configSaved = wifiManager.saveConfiguration(); //Save it
                    boolean isEnabled = wifiManager.enableNetwork(wifiConfig.networkId, true);
                    // reconnect with the new static IP
                    boolean isReconnected = wifiManager.reconnect();
                }
             /*   wifiManager.updateNetwork(wifiConfig); // apply the setting
                wifiManager.saveConfiguration(); //Save it*/
                System.out.println("静态ip设置成功！");
                flag=true;
                return flag;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("静态ip设置失败！");
                flag=false;
                return flag;
            }
        } else {//如果是android5.x版本及以上的话
            try {
                Class<?> ipAssignment = wifiConfig.getClass().getMethod("getIpAssignment").invoke(wifiConfig).getClass();
                Object staticConf = wifiConfig.getClass().getMethod("getStaticIpConfiguration").invoke(wifiConfig);
                if (dhcp) {
                    wifiConfig.getClass().getMethod("setIpAssignment", ipAssignment).invoke(wifiConfig, Enum.valueOf((Class<Enum>) ipAssignment, "DHCP"));
                    if (staticConf != null) {
                        staticConf.getClass().getMethod("clear").invoke(staticConf);
                    }
                } else {
                    wifiConfig.getClass().getMethod("setIpAssignment", ipAssignment).invoke(wifiConfig, Enum.valueOf((Class<Enum>) ipAssignment, "STATIC"));
                    if (staticConf == null) {
                        Class<?> staticConfigClass = Class.forName("android.net.StaticIpConfiguration");
                        staticConf = staticConfigClass.newInstance();
                    }
                    // STATIC IP AND MASK PREFIX
                    Constructor<?> laConstructor = LinkAddress.class.getConstructor(InetAddress.class, int.class);
                    LinkAddress linkAddress = (LinkAddress) laConstructor.newInstance(
                            InetAddress.getByName(ip),
                            prefix);
                    staticConf.getClass().getField("ipAddress").set(staticConf, linkAddress);
                    // GATEWAY
                    staticConf.getClass().getField("gateway").set(staticConf, InetAddress.getByName(gateway));
                    // DNS
                    List<InetAddress> dnsServers = (List<InetAddress>) staticConf.getClass().getField("dnsServers").get(staticConf);
                    dnsServers.clear();
                    dnsServers.add(InetAddress.getByName(dns1));
//                    dnsServers.add(InetAddress.getByName(Constant.dns2)); // Google DNS as DNS2 for safety
                    // apply the new static configuration
                    wifiConfig.getClass().getMethod("setStaticIpConfiguration", staticConf.getClass()).invoke(wifiConfig, staticConf);
                }

                // apply the configuration change
                boolean result = wifiManager.updateNetwork(wifiConfig) != -1; //apply the setting

                if (result) result = wifiManager.saveConfiguration(); //Save it
                if (result) wifiManager.reassociate(); // reconnect with the new static IP

                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disableNetwork(netId);
                flag  = wifiManager.enableNetwork(netId, true);


            } catch (Exception e) {
                e.printStackTrace();
                flag=false;
            }

        }
        return flag;
    }





}

