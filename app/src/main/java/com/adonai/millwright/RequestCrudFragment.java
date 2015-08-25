package com.adonai.millwright;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.EditText;

import com.adonai.millwright.db.DbProvider;
import com.adonai.millwright.db.entities.Request;
import com.adonai.millwright.ui.DatePickerListener;

/**
 * Fragment for performing CRUD-like operation on requests
 * 
 * @author Adonai
 */
public class RequestCrudFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String REQUEST_ANCHOR = "request.anchor";

    private DatePickerListener mDatePicker;
    private EditText mAddressEdit;
    private EditText mRequestTextEdit;
    private EditText mPhoneNumberEdit;
    
    public static RequestCrudFragment newInstance() {
        RequestCrudFragment rqf = new RequestCrudFragment();
        rqf.setArguments(Bundle.EMPTY);
        return rqf;
    }
    
    public static RequestCrudFragment forExisting(Request request) {
        RequestCrudFragment rqf = new RequestCrudFragment();
        Bundle args = new Bundle();
        args.putLong(REQUEST_ANCHOR, request.getId());
        rqf.setArguments(args);
        return rqf;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = getActivity().getLayoutInflater().inflate(R.layout.fragment_request_crud_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root).setNegativeButton(android.R.string.cancel, null);
        
        EditText dateEdit = (EditText) root.findViewById(R.id.date_edit);
        mDatePicker = DatePickerListener.wrap(dateEdit);
        mAddressEdit = (EditText) root.findViewById(R.id.address_edit);
        mRequestTextEdit = (EditText) root.findViewById(R.id.request_text_edit);
        mPhoneNumberEdit = (EditText) root.findViewById(R.id.phone_number_edit);

        if(getArguments().containsKey(REQUEST_ANCHOR)) { // existing
            builder.setTitle(R.string.edit_request).setPositiveButton(R.string.update, this);
            
            Request request = DbProvider.getHelper().getRequestDao().queryForId(getArguments().getLong(REQUEST_ANCHOR));
            mDatePicker.setCalendar(request.getDate());
            mAddressEdit.setText(request.getAddress());
            mRequestTextEdit.setText(request.getCustomText());
            mPhoneNumberEdit.setText(request.getPhoneNumber());
        } else { // new
            builder.setTitle(R.string.create_new_request).setPositiveButton(R.string.create, this);
        }
        
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Request tmp;
        if(getArguments() != null && getArguments().containsKey(REQUEST_ANCHOR))  { // modifying existing account
            tmp = DbProvider.getHelper().getRequestDao().queryForId(getArguments().getLong(REQUEST_ANCHOR));
        } else // creating new
            tmp = new Request();
        fillRequestFieldsFromGUI(tmp);
        DbProvider.getHelper().getRequestDao().createOrUpdate(tmp);
        
        // refresh list
        Fragment main = getActivity().getSupportFragmentManager().findFragmentById(R.id.requests_fragment);
        main.getLoaderManager().getLoader(Constants.Loaders.REQUEST_LOADER.ordinal()).onContentChanged();
    }

    private void fillRequestFieldsFromGUI(Request tmp) {
        tmp.setDate(mDatePicker.getCalendar().getTime());
        tmp.setAddress(mAddressEdit.getText().toString());
        tmp.setCustomText(mRequestTextEdit.getText().toString());
        tmp.setPhoneNumber(mPhoneNumberEdit.getText().toString());
    }
}
