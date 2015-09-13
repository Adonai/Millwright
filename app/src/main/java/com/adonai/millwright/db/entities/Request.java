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
    
    @DatabaseField(id = true)
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
        long diff = days(date, new Date());
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
            case 5: // full
                tmp.setCustomText(elements[4].trim());
                // fall through
            case 4:
                tmp.setPhoneNumber(elements[3].trim());
                // fall through
            case 3:
                tmp.setAddress(elements[2].trim());
                // fall through
            case 2:
                try {
                    tmp.setDate(new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).parse(elements[1].trim()));
                } catch (ParseException e) {
                    tmp.setDate(new Date());
                }
            case 1:
                try {
                    tmp.setId(Long.valueOf(elements[0]));
                } catch (NumberFormatException nfe) {
                    tmp.setId(0);
                }
        }
        return tmp;
    }

    /**
     * Grabbed from <a href=http://stackoverflow.com/questions/4600034/calculate-number-of-weekdays-between-two-dates-in-java>this SO answer</a>
     * @param start start date
     * @param end end date
     * @return count of workdays between two
     */
    static long days(Date start, Date end) {
        //Ignore argument check

        Calendar c1 = Calendar.getInstance();
        c1.setTime(start);
        int w1 = c1.get(Calendar.DAY_OF_WEEK);
        c1.add(Calendar.DAY_OF_WEEK, -w1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(end);
        int w2 = c2.get(Calendar.DAY_OF_WEEK);
        c2.add(Calendar.DAY_OF_WEEK, -w2);

        //end Saturday to start Saturday 
        long days = (c2.getTimeInMillis()-c1.getTimeInMillis())/(1000*60*60*24);
        long daysWithoutWeekendDays = days-(days*2/7);

        // Adjust w1 or w2 to 0 since we only want a count of *weekdays*
        // to add onto our daysWithoutWeekendDays
        if (w1 == Calendar.SUNDAY) {
            w1 = Calendar.MONDAY;
        }

        if (w2 == Calendar.SUNDAY) {
            w2 = Calendar.MONDAY;
        }

        return daysWithoutWeekendDays-w1+w2;
    }
}
