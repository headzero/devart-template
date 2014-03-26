package headzero.flow;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientActivity extends Activity {
    public static final String IPADDR = "ipAddr";
    private Socket socket;

    private static final int SERVERPORT = 6000;
    private String SERVER_IP;
    private EditText editText;
    private TextView text;
    Handler updateConversationHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        SERVER_IP = getIntent().getStringExtra(IPADDR);

        text = (TextView) findViewById(R.id.text_socket_receive);
        editText = (EditText) findViewById(R.id.edit_send_text);
        Button sendButton = (Button) findViewById(R.id.btn_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClientActivity.this.onClick(view);
            }
        });

        updateConversationHandler = new Handler();
        new Thread(new ClientThread()).start();
    }

    public void onClick(View view) {
        try {
            String str = editText.getText().toString();
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            out.println(str);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVERPORT);

                CommunicationThread commThread = new CommunicationThread(socket);
                new Thread(commThread).start();

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }

    class CommunicationThread implements Runnable {

        private Socket serverSocket;

        private BufferedReader input;

        public CommunicationThread(Socket serverSocket) {

            this.serverSocket = serverSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.serverSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();

                    updateConversationHandler.post(new updateUIThread(read));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
        }
    }
}
