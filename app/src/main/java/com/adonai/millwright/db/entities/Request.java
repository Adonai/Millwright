package com.adonai.millwright.db.entities;

import android.support.annotation.NonNull;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Class representing request entity.
 * 
 * @author Oleg Chernovskiy
 */
@DatabaseTable(tableName = "requests")
public class Request {

    /**
     * If today is 26th and request was for 25th
     * 26 - green
     * 27 - yellow
     * 28 - red
     */
    public enum Urgency {
        GREEN,
        YELLOW,
        RED
    }
    
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(index = true, dataType = DataType.DATE_LONG)
    private Date date;
    
    @DatabaseField
    private String address;
    
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

    public boolean shouldPay() {
        return true;
    }

    public Urgency getUrgency() {
        if(date == null) // shouldn't happen
            return Urgency.GREEN;
        
        // calculate the diff
        Calendar forDate = Calendar.getInstance();
        forDate.setTime(date);
        
        int forDayInYear = forDate.get(Calendar.DAY_OF_YEAR);
        int currentDayInYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        
        int diff = currentDayInYear - forDayInYear;
        if(diff <= 1) {
            return Urgency.GREEN;
        } else if (diff <= 2) {
            return Urgency.YELLOW;
        } else {
            return Urgency.RED;
        }
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
    
    public static Request fromSms(String smsText) {
        // format: дата заявки / адрес / оплачивает услугу или не оплачивает (либо текст заявки) / № телефона
        Request tmp = new Request();
        String[] elements = smsText.split("/", -1); // include empty ones
        switch (elements.length) {
            case 4: // full
                tmp.setCustomText(elements[3].trim());
                // fall through
            case 3:
                tmp.setPhoneNumber(elements[2].trim());
                // fall through
            case 2:
                tmp.setAddress(elements[1].trim());
                // fall through
            case 1:
                try {
                    tmp.setDate(new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).parse(elements[0].trim()));
                } catch (ParseException e) {
                    tmp.setDate(new Date());
                }
        }
        return tmp;
    }
}
