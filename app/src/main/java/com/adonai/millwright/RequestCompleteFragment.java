package com.adonai.millwright;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.adonai.millwright.db.DbProvider;
import com.adonai.millwright.db.entities.Request;
import com.adonai.millwright.ui.DatePickerListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.Locale;

/**
 * Fragment for marking requests as completed or moved
 * 
 * @author Adonai
 */
public class RequestCompleteFragment extends DialogFragment implements DialogInterface.OnClickListener, RadioGroup.OnCheckedChangeListener {

    enum RequestStatus {
        COMPLETED,
        DENIED,
        MOVED
    }
    
    private static final String REQUEST_ANCHOR = "request.anchor";

    private TextView mDateText;
    private TextView mAddressText;
    private TextView mRequestCustomText;
    private TextView mPhoneNumberText;
    
    private RadioGroup mRequestCompletionOptions;
    private EditText mDeniedCommentEdit;
    private DatePickerListener mMovedToDatePicker;
    private EditText mMovedCommentEdit;
    
    private Button mOkButton;
    
    private Request mRequest;
    
    public static RequestCompleteFragment newInstance(Request request) {
        RequestCompleteFragment rqf = new RequestCompleteFragment();
        Bundle args = new Bundle();
        args.putLong(REQUEST_ANCHOR, request.getId());
        rqf.setArguments(args);
        return rqf;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = getActivity().getLayoutInflater().inflate(R.layout.fragment_request_complete_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root)
                .setTitle(R.string.close_request)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, this);
        
        mDateText = (TextView) root.findViewById(R.id.date_show_label);
        mAddressText = (TextView) root.findViewById(R.id.address_show_label);
        mPhoneNumberText = (TextView) root.findViewById(R.id.phone_number_show_label);
        mRequestCustomText = (TextView) root.findViewById(R.id.request_text_show_label);
        
        mRequestCompletionOptions = (RadioGroup) root.findViewById(R.id.request_completion_options_group);
        mDeniedCommentEdit = (EditText) root.findViewById(R.id.denied_comment_edit);
        mMovedToDatePicker = DatePickerListener.wrap((EditText) root.findViewById(R.id.move_to_date_edit));
        mMovedCommentEdit = (EditText) root.findViewById(R.id.move_comment_edit);

        mRequest = DbProvider.getHelper().getRequestDao().queryForId(getArguments().getLong(REQUEST_ANCHOR));
        mDateText.setText(DatePickerListener.VIEW_DATE_FORMAT.format(mRequest.getDate()));
        mAddressText.setText(mRequest.getAddress());
        mPhoneNumberText.setText(mRequest.getPhoneNumber());
        mRequestCustomText.setText(mRequest.getCustomText());
        
        mRequestCompletionOptions.setOnCheckedChangeListener(this);
        
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        mOkButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        mOkButton.setEnabled(false);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        try {
            switch (mRequestCompletionOptions.getCheckedRadioButtonId()) {
                case R.id.request_completed_radio: // completed, send sms about completion
                    sendCompletionSms(RequestStatus.COMPLETED);
                    deleteRequest();
                    break;
                case R.id.request_denied_radio: // denied, send sms about denial
                    sendCompletionSms(RequestStatus.DENIED);
                    deleteRequest();
                    break;
                case R.id.request_moved_radio: // move to another date, send sms about movement status
                    sendCompletionSms(RequestStatus.MOVED);
                    updateRequestDate();
                    break;
            }
        } catch (InvalidPropertiesFormatException ipfe) {
            Toast.makeText(getActivity(), R.string.invalid_operator_number, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mOkButton.setEnabled(true);
        mDeniedCommentEdit.setVisibility(View.GONE);
        mMovedToDatePicker.getWrapped().setVisibility(View.GONE);
        mMovedCommentEdit.setVisibility(View.GONE);
        
        switch (checkedId) {
            case R.id.request_denied_radio:
                mDeniedCommentEdit.setVisibility(View.VISIBLE);
                return;
            case R.id.request_moved_radio:
                mMovedToDatePicker.getWrapped().setVisibility(View.VISIBLE);
                mMovedCommentEdit.setVisibility(View.VISIBLE);
                return;
        }
    }

    private void sendCompletionSms(RequestStatus status) throws InvalidPropertiesFormatException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String operatorPhone = preferences.getString(Constants.OPERATOR_PREFERENCE_KEY, "");
        if(!PhoneNumberUtils.isWellFormedSmsAddress(operatorPhone))
            throw new InvalidPropertiesFormatException("Invalid operator phone number!");
        
        PendingIntent sentPI = PendingIntent.getBroadcast(getActivity(), 0, new Intent(Constants.SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(getActivity(), 0, new Intent(Constants.DELIVERED), 0);
        
        SmsManager sms = SmsManager.getDefault();
        String textForOperator;
        switch (status) {
            case COMPLETED: // format: Адрес (берется из входящей смс) / дата закрытия заявки со временем /  "Выполнено"/"Отказ" / комментарий монтажника
                String done = getString(R.string.done);
                textForOperator = String.format("%s/%s/%s/%s", mRequest.getAddress(), sdf.format(new Date()), done, done);
                break;
            case DENIED:
                String denied = getString(R.string.denied);
                String deniedText = mDeniedCommentEdit.getText().toString();
                textForOperator = String.format("%s/%s/%s/%s", mRequest.getAddress(), sdf.format(new Date()), denied, deniedText);
                break;
            case MOVED: // format: Адрес (берется из входящей смс) / дата на которую перенесли заявку со временем / "Заявка перенесена" / комментарий монтажника"
                String moved = getString(R.string.request_moved);
                String movedToDate = sdf.format(mMovedToDatePicker.getCalendar().getTime());
                String movedComment = mMovedCommentEdit.getText().toString();
                textForOperator = String.format("%s/%s/%s/%s", mRequest.getAddress(), movedToDate, moved, movedComment);
                break;
            default: // should not happen
                textForOperator = "";
        }
        sms.sendTextMessage(operatorPhone, null, textForOperator, sentPI, deliveredPI);
    }

    private void updateRequestDate() {
        mRequest.setDate(mMovedToDatePicker.getCalendar().getTime());
        DbProvider.getHelper().getRequestDao().update(mRequest);

        // refresh list
        Fragment main = getActivity().getSupportFragmentManager().findFragmentById(R.id.requests_fragment);
        main.getLoaderManager().getLoader(Constants.Loaders.REQUEST_LOADER.ordinal()).onContentChanged();
    }

    private void deleteRequest() {
        DbProvider.getHelper().getRequestDao().delete(mRequest);

        // refresh list
        Fragment main = getActivity().getSupportFragmentManager().findFragmentById(R.id.requests_fragment);
        main.getLoaderManager().getLoader(Constants.Loaders.REQUEST_LOADER.ordinal()).onContentChanged();
    }

}
