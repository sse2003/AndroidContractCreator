package org.sse.contracts.core.classes;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputType;
import trikita.log.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import org.sse.contracts.Constants;
import org.sse.contracts.Utils;
import org.sse.contracts.core.conf.UserConfigurations;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Class_дата extends AbstractClass
{
    public static String TEMPLATE_STRING = "<span style=\"text-decoration: none;\">" + "\"___\" __________ 20___г" + "</span>";
    public static String REQUEST = "Дата";

    final Calendar CALENDAR = Calendar.getInstance();

    @Override
    public String getRequest() {
        return REQUEST;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE_STRING;
    }

    @Override
    public Integer getInputType() {
        return InputType.TYPE_DATETIME_VARIATION_DATE;
    }

    public SimpleDateFormat getDateFormat()
    {
        return new SimpleDateFormat(UserConfigurations.getDateFormat());
    }

    @Override
    public View.OnFocusChangeListener getOnValueFocusChangeListener(final Context parent, final View valueView) {
        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                CALENDAR.set(Calendar.YEAR, year);
                CALENDAR.set(Calendar.MONTH, monthOfYear);
                CALENDAR.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(valueView);
            }

            private void updateLabel(View value) {
                if (value instanceof TextView)
                {
                    ((TextView) value).setText(getDateFormat().format(CALENDAR.getTime()));
                }
                else
				{
                    Log.e(TAG, "Объект value имеет несовместимый тип");
                }
            }
        };

        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    new DatePickerDialog(parent, dateSetListener, CALENDAR.get(Calendar.YEAR), CALENDAR.get(Calendar.MONTH), CALENDAR.get(Calendar.DAY_OF_MONTH)).show();
            }
        };
    }
}
