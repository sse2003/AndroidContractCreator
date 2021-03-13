package org.sse.contracts.view;

import android.graphics.Typeface;
import trikita.log.Log;
import android.view.View;

import org.sse.contracts.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PurchaseWrapper
{
    private String title;
    private String price;
    private String sku;
    private View.OnClickListener onClickListener;

    public PurchaseWrapper(String title, String price, String sku)
    {
        this.title = title;
        this.price = price;
        this.sku = sku;
    }

    public PurchaseWrapper(String title, String price, View.OnClickListener listener)
    {
        this.title = title;
        this.price = price;
        this.onClickListener = listener;
    }

    public String getPrice()
    {
        return price;
    }

    public String getTitle()
    {
        return title;
    }

    public String getSku()
    {
        return sku;
    }

    public View.OnClickListener getOnClickListener()
    {
        return onClickListener;
    }

    public int getSortIndex()
    {
        if (getPrice() != null)
        {
            Matcher m = Pattern.compile(Constants.PATTERN_NUMBERS).matcher(getPrice().replaceAll("\\s", ""));
            try
            {
                if (m.find())
                {
                    int result = Integer.parseInt(m.group(Constants.GROUP_PATTERN_NUMBERS));
                    Log.d( "Sort index for '" + title + "', price: " + price + ": " + result);
                    return result;
                }
            } catch (NumberFormatException ex)
            {
                Log.w(getClass().getName(), ex.getMessage());
            }
        }
        return 0;
    }

    public Typeface getTypeFace()
    {
        return Typeface.DEFAULT;
    }

    @Override
    public String toString()
    {
        return "PurchaseWrapper{" +
                "title='" + title + '\'' +
                ", price='" + price + '\'' +
                ", sku='" + sku + '\'' +
                ", onClickListener=" + onClickListener +
                '}';
    }
}
