package com.adonai.millwright;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.adonai.millwright.db.DbProvider;
import com.adonai.millwright.db.entities.Request;
import com.adonai.millwright.telephony.DeliveryConfirmReceiver;
import com.adonai.millwright.telephony.SentConfirmReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RequestsActivity extends AppCompatActivity {

    BroadcastReceiver mSentReceiver, mDeliveryReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        
        // init db
        DbProvider.setHelper(this);
        
        // init receivers
        mSentReceiver = new SentConfirmReceiver(this);
        mDeliveryReceiver = new DeliveryConfirmReceiver(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, PreferenceActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        //--- When the SMS has been sent ---
        registerReceiver(mSentReceiver, new IntentFilter(Constants.SENT));
        //--- When the SMS has been delivered. ---
        registerReceiver(mDeliveryReceiver, new IntentFilter(Constants.DELIVERED));
    }

    @Override
    protected void onStop() {
        super.onStop();
        
        unregisterReceiver(mSentReceiver);
        unregisterReceiver(mDeliveryReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // we were notified from service, thus we know that new request was created for us, refresh the list
        Fragment requestList = getSupportFragmentManager().findFragmentById(R.id.requests_fragment);
        requestList.getLoaderManager().getLoader(Constants.Loaders.REQUEST_LOADER.ordinal()).onContentChanged();
    }

    @Override
    protected void onDestroy() {
        // close db
        DbProvider.releaseHelper();
        
        super.onDestroy();
    }
}
