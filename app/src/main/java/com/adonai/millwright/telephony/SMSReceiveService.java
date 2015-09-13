package com.adonai.millwright.telephony;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.widget.Toast;

import com.adonai.millwright.Constants;
import com.adonai.millwright.R;
import com.adonai.millwright.RequestsActivity;
import com.adonai.millwright.db.DbProvider;
import com.adonai.millwright.db.PersistManager;
import com.adonai.millwright.db.entities.Request;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Calendar;
import java.util.Locale;

public class SMSReceiveService extends Service {

    private SharedPreferences mPreferences;
    private ToneGenerator mToneGenerator;
    private Handler mHandler;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mToneGenerator.release();
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("number")) {
            boolean shouldPlaySound = mPreferences.getBoolean(Constants.RING_ON_SMS_KEY, false);
            boolean shouldOpen = mPreferences.getBoolean(Constants.OPEN_ON_SMS_KEY, true);
            String smsText = intent.getStringExtra("text");

            // phone must match the one from settings
            String smsNumber = intent.getStringExtra("number");
            String operatorNumber = mPreferences.getString(Constants.OPERATOR_PREFERENCE_KEY, "");
            
            String countryCode = getResources().getConfiguration().locale.getCountry();
            String normalizedSmsNumber = formatNumberToE164(smsNumber, countryCode);
            String normalizedOperatorNumber = formatNumberToE164(operatorNumber, countryCode);
            if(!PhoneNumberUtils.compare(this, normalizedSmsNumber, normalizedOperatorNumber))
                return START_NOT_STICKY;

            // persist obtained request info
            addRequestEntry(smsText);
            
            if(shouldPlaySound) { // play notify sound
                mToneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 250);
            }
            
            if(shouldOpen) { // open activity
                Intent starter = new Intent(this, RequestsActivity.class);
                starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(starter);
            }
        }
        return START_NOT_STICKY;
    }
    
    public void addRequestEntry(String smsText) {
        PersistManager manager = DbProvider.getTempHelper(this);
        Request toPersist = Request.fromSms(smsText);
        if(manager.getRequestDao().idExists(toPersist.getId())) { // warn
            Toast.makeText(this, R.string.request_already_exists, Toast.LENGTH_LONG).show();
        } else { // create
            manager.getRequestDao().create(toPersist);
        }
        DbProvider.releaseTempHelper(); // it's ref-counted thus will not close if activity uses it...
    }

    /**
     * Format the given phoneNumber to the E.164 representation.
     * <p>
     * The given phone number must have an area code and could have a country
     * code.
     * <p>
     * The defaultCountryIso is used to validate the given number and generate
     * the E.164 phone number if the given number doesn't have a country code.
     *
     * @param phoneNumber
     *            the phone number to format
     * @param defaultCountryIso
     *            the ISO 3166-1 two letters country code
     * @return the E.164 representation, or null if the given phone number is
     *         not valid.
     */
    public static String formatNumberToE164(String phoneNumber, String defaultCountryIso) {
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        String result = null;
        try {
            Phonenumber.PhoneNumber pn = util.parse(phoneNumber, defaultCountryIso);
            if (util.isValidNumber(pn)) {
                result = util.format(pn, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        } catch (NumberParseException ignored) {
        }
        return result;
    }

}
