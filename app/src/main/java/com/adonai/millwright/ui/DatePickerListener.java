package com.adonai.millwright.ui;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Wrapper class that allows using EditText as date picker
 * 
 * @author Adonai
 */
public class DatePickerListener implements View.OnClickListener, View.OnFocusChangeListener {

    public final static SimpleDateFormat VIEW_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    
    private Calendar mSelectedTime = Calendar.getInstance();
    private final EditText mText;

    public static DatePickerListener wrap(EditText text) {
        DatePickerListener dpl = new DatePickerListener(text);
        text.setOnFocusChangeListener(dpl);
        text.setOnClickListener(dpl);

        return dpl;
    }

    private DatePickerListener(EditText text) {
        mText = text;
        mText.setText(VIEW_DATE_FORMAT.format(mSelectedTime.getTime())); // current time
    }

    @Override
    public void onClick(View v) {
        handleClick();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!hasFocus)
            return;

        handleClick();
    }

    private void handleClick() {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mSelectedTime.set(year, monthOfYear, dayOfMonth);
                mText.setText(VIEW_DATE_FORMAT.format(mSelectedTime.getTime()));
            }
        };
        new DatePickerDialog(mText.getContext(), listener, mSelectedTime.get(Calendar.YEAR), mSelectedTime.get(Calendar.MONTH), mSelectedTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    public Calendar getCalendar() {
        return mSelectedTime;
    }

    public void setCalendar(Date mNow) {
        this.mSelectedTime.setTime(mNow);
        mText.setText(VIEW_DATE_FORMAT.format(mSelectedTime.getTime()));
    }

    public EditText getWrapped() {
        return mText;
    }
}