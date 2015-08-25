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
        
        if(getIntent().hasExtra("number")) {
            createRequestFromSms(getIntent());
        }
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

        if(intent.hasExtra("number")) {
            createRequestFromSms(intent);
        }
    }

    private void createRequestFromSms(Intent intent) {
        String smsText = intent.getStringExtra("text");
        
        // format: дата заявки / адрес / оплачивает услугу или не оплачивает (либо текст заявки) / № телефона
        Request toPersist = new Request();
        String[] elements = smsText.split("/", -1); // include empty ones
        switch (elements.length) {
            case 4: // full
                toPersist.setPhoneNumber(elements[3].trim());
                // fall through
            case 3:
                toPersist.setCustomText(elements[2].trim());
                // fall through
            case 2:
                toPersist.setAddress(elements[1].trim());
                // fall through
            case 1:
                try {
                    toPersist.setDate(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(elements[0].trim()));
                } catch (ParseException e) {
                    toPersist.setDate(new Date());
                }
        }
        
        DbProvider.getHelper().getRequestDao().create(toPersist);
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
