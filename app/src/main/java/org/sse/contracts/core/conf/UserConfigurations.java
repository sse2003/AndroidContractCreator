package org.sse.contracts.core.conf;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.sse.contracts.Constants;
import org.sse.contracts.R;
import org.sse.contracts.Utils;

public class UserConfigurations
{
    public static final String KEY_DATE_FORMAT = "date_format";
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_MARGIN_LEFT = "margin_left";
    public static final String KEY_MARGIN_RIGHT = "margin_right";
    public static final String KEY_MARGIN_TOP = "margin_top";
    public static final String KEY_MARGIN_BOTTOM = "margin_bottom";


    private static SharedPreferences getPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(Utils.getWorkingContext());
    }

    public static String getDateFormat()
    {
        SharedPreferences prefs = getPreferences();
        if (prefs == null) return Constants.DEFAULT_PDF_DATE_FORMAT;

        return prefs.getString(KEY_DATE_FORMAT, Constants.DEFAULT_PDF_DATE_FORMAT);
    }

    public static String getFontSize()
    {
        SharedPreferences prefs = getPreferences();
        String defaultValue = Utils.getWorkingContext().getString(R.string.default_font_size_value);
        String result = prefs.getString(KEY_FONT_SIZE, defaultValue);
        if (result.equals(defaultValue)) return null;

        return result;
    }

    private static float getMargin(String key, String defaultValue)
    {
        SharedPreferences prefs = getPreferences();
        float value = Float.parseFloat(prefs.getString(key, defaultValue));
        if (value < 0) value = Float.parseFloat(defaultValue);
        return value;
    }

    public static float getLeftMargin()
    {
        return getMargin(KEY_MARGIN_LEFT, Constants.PDF_DEFAULT_LEFT_MARGIN);
    }

    public static float getRightMargin()
    {
        return getMargin(KEY_MARGIN_RIGHT, Constants.PDF_DEFAULT_RIGHT_MARGIN);
    }

    public static float getTopMargin()
    {
        return getMargin(KEY_MARGIN_TOP, Constants.PDF_DEFAULT_TOP_MARGIN);
    }

    public static float getBottomMargin()
    {
        return getMargin(KEY_MARGIN_BOTTOM, Constants.PDF_DEFAULT_BOTTOM_MARGIN);
    }
}
