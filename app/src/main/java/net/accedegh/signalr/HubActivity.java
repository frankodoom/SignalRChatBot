package net.accedegh.signalr;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import org.w3c.dom.Text;

import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler;


public class HubActivity extends AppCompatActivity {

    Button sendMessage;
    EditText message;
    TextView messagebody;
    HubConnection connection;
    String chatMessage;
    HubProxy mainHubProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);

        messagebody = (TextView) findViewById(R.id.textView);
        message = (EditText) findViewById(R.id.editText2);
        sendMessage = (Button) findViewById(R.id.send);


        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        //Create A Hub Connection
        String server = "https://qcserviceapi.azurewebsites.net/signalr";
        connection = new HubConnection(server);
        mainHubProxy= connection.createHubProxy("ChatHub");

        //connect To Service
        connectSignalr();

        //Called By Server
        mainHubProxy.subscribe("broadcastMessage").addReceivedHandler(new Action<JsonElement[]>() {
            @Override
            public void run(JsonElement[] jsonElements) throws Exception {
                messagebody.append(jsonElements[0].getAsString() + "\n");
                Log.i("SignalR", "Message From Server: " + jsonElements[0].getAsString());
            }
        });


        //Sennd Message On Click
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatMessage= message.getText().toString();
                new SignalRTestActionWithResultTask().execute(new Object());
                message.setText("");
            }
        });

        connection.stateChanged(new StateChangedCallback() {
            @Override
            public void stateChanged(ConnectionState connectionState, ConnectionState connectionState2) {
                Toast.makeText(HubActivity.this, connectionState.name() + "->" + connectionState2.name(), Toast.LENGTH_SHORT).show();
                Log.i("SignalR", connectionState.name() + "->" + connectionState2.name());
            }
        });


        connection.closed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HubActivity.this, "SignalR Closed", Toast.LENGTH_SHORT).show();
                Log.i("SignalR", "Closed");
                connectSignalr();
            }
        });
    }

        private void connectSignalr() {
            try {
                Toast.makeText(this, "Connecting to SignalR Service", Toast.LENGTH_SHORT).show();
                SignalRConnectTask signalRConnectTask = new SignalRConnectTask();
                signalRConnectTask.execute(connection);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Connection that Retuns Void
        public class SignalRConnectTask extends AsyncTask {
            @Override
            protected Object doInBackground(Object[] objects) {
                HubConnection connection = (HubConnection) objects[0];
                try {
                    Thread.sleep(2000);
                    connection.start().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

    Object result;

    //Connection that returns something
    public class SignalRTestActionWithResultTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            if (connection.getState() == ConnectionState.Connected) {
                 result = null;
                Object param1 = objects[0];
                //Object param2 = objects[1];
                try {
                    //Thread.sleep(2000);
                    result = mainHubProxy.invoke(String.class, "broadcastMessage", chatMessage.toString()).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            // Тут идет обработка результата.
        }
    }
}

