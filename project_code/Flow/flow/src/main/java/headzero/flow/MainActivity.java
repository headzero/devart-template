package headzero.flow;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


public class MainActivity extends Activity {

    private TextView ipAddrText;
    private EditText serverIpAddrEdit;
    private Button clientButton;
    private Button serverButton;
    private String myIpAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        ipAddrText = (TextView) findViewById(R.id.text_ip_addr);
        myIpAddress = getLocalIpAddress();
        ipAddrText.setText(myIpAddress);
        serverIpAddrEdit = (EditText) findViewById(R.id.edit_ip_addr);
        clientButton = (Button) findViewById(R.id.btn_client);
        clientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverIpAddr = serverIpAddrEdit.getText().toString();
                Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                intent.putExtra(ClientActivity.IPADDR, serverIpAddr);
                startActivity(intent);
            }
        });
        serverButton = (Button) findViewById(R.id.btn_server);
        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ServerActivity.class);
                intent.putExtra(ServerActivity.IPADDR, myIpAddress);
                startActivity(intent);
            }
        });
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) {
                            return ((Inet4Address)inetAddress).getHostAddress().toString();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }

}
