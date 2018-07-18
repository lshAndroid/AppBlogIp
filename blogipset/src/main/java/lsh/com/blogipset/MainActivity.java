package lsh.com.blogipset;

import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lsh.com.blogipset.iputil.SetIpManager;
import lsh.com.blogipset.iputil.android7ip.IPSettings;

public class MainActivity extends AppCompatActivity {
    private Button mButton, mButton2, mButton3, mButton4, mButton5;
    private EditText mEtTextIp,mEtTextGateWay;
    private SetIpManager IpConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IpConfig = new SetIpManager(MainActivity.this);//封装，获取ip设置对象
        initBindView();
        initOnclick();
    }

    private void initOnclick() {
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipText=mEtTextIp.getText().toString().trim();
                String gateWayText=mEtTextGateWay.getText().toString().trim();
                //IP 网络前缀长度24 DNS1域名1 网关
                Boolean isSetSuccess = IpConfig.setIpWithTfiStaticIp(false, ipText, 24, "255.255.255.0", gateWayText);
                Toast.makeText(MainActivity.this, "" + isSetSuccess, Toast.LENGTH_SHORT).show();

            }
        });
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //IP 网络前缀长度24 DNS1域名1 网关
                String ipText=mEtTextIp.getText().toString().trim();
                String gateWayText=mEtTextGateWay.getText().toString().trim();
                Boolean isSetSuccess = IpConfig.setIpWithTfiStaticIp(true, ipText, 24, "255.255.255.0", gateWayText);
                Toast.makeText(MainActivity.this, "" + isSetSuccess, Toast.LENGTH_SHORT).show();

            }
        });
        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = IpConfig.getIpStr();
                Toast.makeText(MainActivity.this, ip, Toast.LENGTH_SHORT).show();
            }
        });
        mButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, IpConfig.getWifiSetting(), Toast.LENGTH_SHORT).show();
            }
        });


        conf = new WifiConfiguration();
        mButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不一定能用，Android6.0后的有些系统厂商对这个敏感功能进行限制
                String IP_ADDRESS = "192.168.0.193";
                String GATEWAY = "192.168.0.1";
                int PREFIX_LENGTH = 24;
                InetAddress[] dns_servers = new InetAddress[0];
                try {
                    dns_servers = new InetAddress[]{InetAddress.getByName("8.8.8.8"), InetAddress.getByName("8.8.4.4")};
                    conf = IPSettings.setStaticIpConfiguration(conf,InetAddress.getByName(IP_ADDRESS),PREFIX_LENGTH,InetAddress.getByName(GATEWAY),dns_servers);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    WifiConfiguration conf;//不一定能用，Android6.0后的有些系统厂商对这个敏感功能进行限制
    private void initBindView() {
        mButton = (Button) findViewById(R.id.bt_change_ip1);
        mButton2 = (Button) findViewById(R.id.bt_change_ip2);
        mButton3 = (Button) findViewById(R.id.bt_change_ip3);
        mButton4 = (Button) findViewById(R.id.bt_change_ip4);
        mButton5 = (Button) findViewById(R.id.bt_change_ip5);
        mEtTextIp = (EditText) findViewById(R.id.ip_et);
        mEtTextGateWay = (EditText) findViewById(R.id.gateway_et);
    }

}
