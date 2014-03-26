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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerActivity extends Activity {
    public static final String IPADDR = "ipAddr";
    private ServerSocket serverSocket;

    Handler updateConversationHandler;

    ServerThread serverThread = null;

    private TextView text;

    public static final int SERVERPORT = 6000;
    private EditText editText;
    private Button sendBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        text = (TextView) findViewById(R.id.text_socket_receive);
        editText = (EditText) findViewById(R.id.edit_send_text);
        sendBtn = (Button) findViewById(R.id.btn_send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText == null || editText.getText().toString() == null || "".equals(editText.getText().toString())){
                    return;
                }

                if(serverThread != null){
                    serverThread.doMultipleSendMessage(editText.getText().toString());
                }
            }
        });

        updateConversationHandler = new Handler();

        this.serverThread = new ServerThread();
        this.serverThread.start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread extends Thread {
        private ArrayList<CommunicationThread> communicationThreads = new ArrayList<CommunicationThread>();

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket);
                    communicationThreads.add(commThread);
                    commThread.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void doMultipleSendMessage(String message) {
            for (CommunicationThread communicationThread : communicationThreads) {
                communicationThread.sendMessage(message);
            }
        }
    }

    public class CommunicationThread extends Thread{

        private Socket clientSocket;

        private BufferedReader input;
        private BufferedWriter output;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                this.output = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();
                    // sendMessage(read); // memo : single return;
                    serverThread.doMultipleSendMessage(read);

                    updateConversationHandler.post(new updateUIThread(read));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String read) {
            try {
                PrintWriter out = new PrintWriter(output, true);
                out.println(read);
            } catch (Exception e) {
                e.printStackTrace();
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
