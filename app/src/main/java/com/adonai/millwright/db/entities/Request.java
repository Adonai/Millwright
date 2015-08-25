package com.adonai.millwright.db.entities;

import android.support.annotation.NonNull;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Calendar;
import java.util.Date;

/**
 * Class representing request entity.
 * 
 * @author Oleg Chernovskiy
 */
@DatabaseTable(tableName = "requests")
public class Request {
    
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(index = true, dataType = DataType.DATE_LONG)
    private Date date;
    
    @DatabaseField
    private String address;
    
    private boolean shouldPay;
    
    @DatabaseField
    private String customText;
    
    @DatabaseField
    private String phoneNumber;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @NonNull
    public String getAddress() {
        return address != null ? address : "";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isShouldPay() {
        return shouldPay;
    }

    public void setShouldPay(boolean shouldPay) {
        this.shouldPay = shouldPay;
    }

    @NonNull
    public String getCustomText() {
        return customText != null ? customText : "";
    }

    public void setCustomText(String customText) {
        this.customText = customText;
    }

    @NonNull
    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber : "";
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
