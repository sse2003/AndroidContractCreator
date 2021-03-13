package org.sse.contracts.core;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.sse.contracts.Constants;
import org.sse.contracts.FontUtils;
import org.sse.contracts.R;
import org.sse.contracts.Utils;
import org.sse.contracts.core.classes.AbstractClass;
import org.sse.contracts.core.classes.ExpressionClassesFactory;
import org.sse.contracts.core.exceptions.ExpressionException;

import java.util.regex.Matcher;

public class Expression
{
    private String expression;

    private Parameters parameters = new Parameters();
    private String request = null;
    private String response = "";
    private AbstractClass expressionClass = null;
    private HtmlString group = null;
    private boolean priorityGroupEnabled = false;

    Expression(String expression) throws ExpressionException
    {
        this.expression = Utils.replace_cRu_to_cEn(expression);

        expression = replaceSpecialSpaceSymbolAfterParameter(expression);

        Matcher matcher = Constants.PATTERN_EXPRESSION.matcher(expression);
        if (!matcher.find())
            throw new ExpressionException("Строка '" + expression + "' не является выражением");


        request = matcher.group(Constants.GROUP_EXPRESSION_TEXT);
        matcher = Constants.PATTERN_PARAMETERS.matcher(expression);
        if (!matcher.find()) return;

        parameters = new Parameters(matcher.group(Constants.GROUP_PARAMETERS_PARAMETERS));
        request = matcher.group(Constants.GROUP_PARAMETERS_REQUEST_TEXT);

        if (parameters.getParameters().containsKey(Parameters.Params.c.name()))
            expressionClass = ExpressionClassesFactory.create(parameters.getParameters().get(Parameters.Params.c.name()).getTextOnly());
        else if (parameters.getParameters().containsKey(Parameters.Params.с.name()))
            expressionClass = ExpressionClassesFactory.create(parameters.getParameters().get(Parameters.Params.с.name()).getTextOnly());

        if (request.isEmpty() && expressionClass != null && expressionClass.getRequest() != null)
        {
            request = expressionClass.getRequest();
        }

        if (request.isEmpty() && parameters.getParameters().containsKey(Parameters.Params.c.name()))
        {
            request = parameters.getParameters().get(Parameters.Params.c.name()).getTextOnly();
            request = request.replaceAll("_", " ");
        }
    }

    Expression(String expression, String group) throws ExpressionException
    {
        this(expression, new HtmlString(group));
    }

    Expression(String expression, HtmlString group) throws ExpressionException
    {
        this(expression);

        this.group = group;

        if (parameters.getParameters().containsKey(Parameters.Params.g.name()))
        {
            priorityGroupEnabled = true;

            HtmlString gr = parameters.getParameters().get(Parameters.Params.g.name());
            if (gr == null || gr.toString() == null || gr.toString().isEmpty())
                this.group = null;
            else
                this.group = gr;
        }
    }

    static String replaceSpecialSpaceSymbolAfterParameter(String expression)
    {
        return expression.replaceAll(Constants.REG_EXP_SPECIAL_SPACE_SYMBOL_AFTER_PARAMETER, "$1 $3");
    }

    static int parse_t_m_parameters(String len, String defaultSym)
    {
        if (len.endsWith("%"))
        {
            len = len.substring(0, len.length() - 1);
            return FontUtils.linePercentSizeToCharCount(Integer.parseInt(len), defaultSym);
        } else
            return FontUtils.getSymbolWidthCorrection(Integer.parseInt(len), defaultSym);
    }

    Parameters getParameters()
    {
        return parameters;
    }

    public String getRequest()
    {
        String str = request.replaceAll(Constants.SPACE_SYMBOL, " ");
        str = Utils.prepareExpressionRequest(str);
        return str;
    }

    public String makeResponse()
    {
        if (expressionClass != null && expressionClass.getResponse() != null)
            return expressionClass.getResponse();

        if (response.isEmpty()) return makeTemplate();

        String result = response;
        int minimumLen = getNormalTextMinimumLength();
        String defaultSym = getDefaultSymbolString();

        int startLen = result.length();
        while (startLen++ < minimumLen)
        {
            result += defaultSym;
        }

        return result.replaceAll("\r\n|\n", Constants.HTML_NEW_LINE_TAG);
    }

    public Expression setResponse(String response)
    {
        this.response = response;
        return this;
    }

    public String makeTemplate()
    {
        if (expressionClass != null && expressionClass.getTemplate() != null)
            return expressionClass.getTemplate();

        String template = "";
        int len = getTemplateLength();
        String defSym = getDefaultSymbolString();
        while (len-- > 0)
        {
            template += defSym;
        }
        return template;
    }

    String getExpression()
    {
        return expression;
    }

    private String getDefaultSymbolString()
    {
        if (expressionClass != null)
            return expressionClass.getDefaultSymbolString();

        if (parameters.getParameters().containsKey(Parameters.Params.s.name()))
            return parameters.getParameters().get(Parameters.Params.s.name()).toString();

        return Constants.TEMPLATE_SYMBOL;
    }

    public int getTemplateLength()
    {
        return parse_t_m_parameters(getTemplateLength2(), getDefaultSymbolString());
    }

    private String getTemplateLength2()
    {
        if (parameters.getParameters().containsKey(Parameters.Params.t.name()))
        {
            // return Integer.parseInt(parameters.getParameters().get(Parameters.Params.t.name()));
            return parameters.getParameters().get(Parameters.Params.t.name()).getTextOnly();
        }

        if (expressionClass != null)
            return expressionClass.getTemplateLength();

        return "" + expression.replaceAll(Constants.SPACE_SYMBOL, " ").length();
    }

    int getNormalTextMinimumLength()
    {
        return parse_t_m_parameters(getNormalTextMinimumLength2(), getDefaultSymbolString());
    }

    private String getNormalTextMinimumLength2()
    {
        if (expressionClass != null)
            return expressionClass.getNormalTextMinimumLength();

        String min = "0";

        if (parameters.getParameters().containsKey(Parameters.Params.m.name()))
            min = parameters.getParameters().get(Parameters.Params.m.name()).getTextOnly();

        return min;
    }

    AbstractClass getExpressionClass()
    {
        return expressionClass;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Expression that = (Expression) o;

        String exp1 = expression.replaceAll(Constants.SPACE_SYMBOL, " ");
        String exp2 = that.expression.replaceAll(Constants.SPACE_SYMBOL, " ");

        exp1 = Utils.smartRemoveGlobalParameter(exp1);
        exp2 = Utils.smartRemoveGlobalParameter(exp2);

        if (!exp1.equals(exp2)) return false;

        if (group != null && group.toString() != null ? !group.getTextOnly().equals(that.group != null ? that.group.getTextOnly() : null) : (that.group != null && that.group.toString() != null)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        String exp = expression.replaceAll(Constants.SPACE_SYMBOL, " ");
        exp = Utils.smartRemoveGlobalParameter(exp);

        int result = exp.hashCode();
        result = 31 * result + (group != null && group.toString() != null ? group.getTextOnly().hashCode() : 0);
        return result;
    }

    public String getDefaultResponse()
    {
        if (expressionClass != null) return expressionClass.getDefaultResponse();
        return null;
    }

    public Integer getInputType()
    {
        if (expressionClass != null) return expressionClass.getInputType();
        return null;
    }

    public String getGroup()
    {
        if (group == null) return null;

        return group.getTextOnly();
    }

    public String toString()
    {
        return "[expression: " + expression + ", group: " + group + "]";
    }

    public View getValueView(Activity activity)
    {
        int id = R.layout.expression_value_default;
        if (expressionClass != null) id = expressionClass.getValueViewId();

        return activity.getLayoutInflater().inflate(id, null);
    }

    public View.OnFocusChangeListener getOnValueFocusChangeListener(final Context parent, View valueView, String request)
    {
        String[] choices = getChoices();
        if (choices != null)
        {
            return new ChoicesOnFocusChangeListener(parent, request, choices);
        } else if (expressionClass != null)
            return expressionClass.getOnValueFocusChangeListener(parent, valueView);
        return null;
    }

    public String[] getChoices()
    {
        HtmlString choices = getParameters().getParameters().get(Parameters.Params.h.name());
        if (choices != null) return choices.getTextOnly().split(Parameters.CHOIСES_SEPARATOR);
        if (expressionClass != null) return expressionClass.getChoices();
        return null;
    }

    public void setGroup(HtmlString group)
    {
        this.group = group;
    }

    public boolean isSetPriorityGroup()
    {
        return priorityGroupEnabled;
    }

    private class ChoicesOnFocusChangeListener implements View.OnFocusChangeListener
    {
        private Context parent;
        private String title;
        private String[] choices;

        public ChoicesOnFocusChangeListener(Context parent, String title, String[] choices)
        {
            this.parent = parent;
            this.title = title;
            this.choices = choices;
        }

        @Override
        public void onFocusChange(final View v, boolean hasFocus)
        {
            if (hasFocus)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(parent);

                builder.setTitle(title)
                        .setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (v instanceof TextView)
                                {
                                    ((TextView) v).setText(choices[which]);
                                }
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                ListView listView = dialog.getListView();

                // Set the divider color of alert dialog list view
                listView.setDivider(new ColorDrawable(parent.getResources().getColor(R.color.listViewSeparator)));

                // Set the divider height of alert dialog list view
                listView.setDividerHeight(1);

                dialog.show();
            }
        }
    }
}
