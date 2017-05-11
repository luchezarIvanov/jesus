package com.led_on_off.led;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.led_on_off.led.R;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;


public class JesusControlCenter extends ActionBarActivity {

   // Button btnOn, btnOff, btnDis;
    ImageButton On, Off, Discnt;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        setContentView(R.layout.activity_jesus_control_center);

        On = (ImageButton)findViewById(R.id.on);
        Off = (ImageButton)findViewById(R.id.off);
        Discnt = (ImageButton)findViewById(R.id.discnt);

        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        On.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                turnOnLed();
            }
        });

        Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                turnOffLed();
            }
        });

        Discnt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect();
            }
        });

        try {
            Toast.makeText(this,readBT(),Toast.LENGTH_LONG);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"ni staa",Toast.LENGTH_SHORT);
        }


    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish();

    }

    private String readBT() throws IOException {
        if (btSocket != null){
           InputStream inputStream = btSocket.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            while (line != null){
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            String input = stringBuilder.toString();
            return input;

        }
        else{return "greshka mreshka";}
    }

    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("0".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("1".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }

    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(JesusControlCenter.this, "1 sekunda...", "Piq vino!!!");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                 myBluetooth = BluetoothAdapter.getDefaultAdapter();//getting the mobile bluetooth device
                 BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connecting to the device's address and checks if it's available
                 btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//establishing SPP connection
                 BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                 btSocket.connect();//starting connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Ne stana, ako bluetooth-a ne e SPP ne me zanimavai");
                finish();
            }
            else
            {
                msg("Gotov si");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
