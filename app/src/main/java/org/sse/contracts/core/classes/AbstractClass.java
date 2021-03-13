package org.sse.contracts.core.classes;

import android.content.Context;
import android.view.View;

import org.sse.contracts.R;
import org.sse.contracts.Constants;

public abstract class AbstractClass
{
    public static final String TAG = "AbstractClass";

    public String getTemplateLength()
    {
        return "0";
    }

    public String getNormalTextMinimumLength()
    {
        return "0";
    }

    public String getDefaultSymbolString()
    {
        return Constants.TEMPLATE_SYMBOL;
    }

    public String getRequest()
    {
        return null;
    }

    public String getDefaultResponse()
    {
        return null;
    }

    public String getResponse()
    {
        return null;
    }

    public String getTemplate()
    {
        return null;
    }

    public Integer getInputType()
    {
        return null;
    }

    public String[] getChoices()
    {
        return null;
    }

    public int getValueViewId()
    {
        return R.layout.expression_value_default;
    }

    public View.OnFocusChangeListener getOnValueFocusChangeListener(final Context parent, final View valueView)
    {
        return null;
    }
}