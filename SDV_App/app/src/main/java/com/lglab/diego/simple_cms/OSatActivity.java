package com.lglab.diego.simple_cms;

import android.os.Bundle;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.lglab.diego.simple_cms.create.utility.model.Action;
import com.lglab.diego.simple_cms.create.utility.model.ActionController;
import com.lglab.diego.simple_cms.demo.DemoThread;
import com.lglab.diego.simple_cms.top_bar.TobBarActivity;
import com.lglab.diego.simple_cms.utility.ConstantPrefs;

import java.util.ArrayList;
import java.util.List;

public class OSatActivity extends TobBarActivity {

    private static final String TAG_DEBUG = "OSatActivity";

    private Dialog dialog;
    private DemoThread demoThread = null;
    private Handler handler = new Handler();
    private TextView connectionStatus;
    private List<Action> actionsSaved = new ArrayList<>();
    private Button buttDemo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osatellites);

        View topBar = findViewById(R.id.top_bar);
        buttDemo = topBar.findViewById(R.id.butt_demo2);
        connectionStatus = findViewById(R.id.connection_status);

        SharedPreferences sharedPreferences = getSharedPreferences(ConstantPrefs.SHARED_PREFS.name(), MODE_PRIVATE);
        //loadConnectionStatus(sharedPreferences);
    }

    /**
     * Set the connection status on the view
     */
    /**
    private void loadConnectionStatus(SharedPreferences sharedPreferences) {
        boolean isConnected = sharedPreferences.getBoolean(ConstantPrefs.IS_CONNECTED.name(), false);
        if (isConnected) {
            connectionStatus.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_status_connection_green));
        } else {
            connectionStatus.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_status_connection_red));
        }
    }*/

    public void sendNOAA18Const(View view) {
        ActionController.getInstance().sendNOAA18ConstFile(OSatActivity.this);
    }

    public void sendIridiumConst(View view) {
        ActionController.getInstance().sendIridiumConstFile(OSatActivity.this);
    }
}
