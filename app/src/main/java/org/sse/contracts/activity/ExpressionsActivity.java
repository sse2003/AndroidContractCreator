package org.sse.contracts.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import trikita.log.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.sse.contracts.Constants;
import org.sse.contracts.R;
import org.sse.contracts.Utils;
import org.sse.contracts.activity.listeners.WritePermissionListener;
import org.sse.contracts.core.conf.InternalConfigurations;
import org.sse.contracts.core.ContractType;
import org.sse.contracts.core.Expression;
import org.sse.contracts.core.HtmlParser;
import org.sse.contracts.core.conf.UserConfigurations;
import org.sse.contracts.core.contract.AbstractContract;
import org.sse.contracts.core.exceptions.LoadPresetFilePermissionException;
import org.sse.contracts.core.task_manager.AlertDialogTask;
import org.sse.contracts.core.task_manager.ShowIntentTask;
import org.sse.contracts.core.task_manager.Task;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

public class ExpressionsActivity extends BasePermissionsActivity implements OpenFileDialog.OpenDialogListener
{
    private final static int MIN_INPUT_CHARACTERS_WHEN_CHANGED_APPLY = 3 * 2;
    private static final String XML_TAG_EXPRESSION = "expression";
    private static final String XML_ATTR_EXPRESSION_HASH = "hash";
    private static final String XML_ATTR_EXPRESSION_RESPONCE = "responce";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_ATTR_CONFIG_APP_ID = "appId";

    private HtmlParser parser = null;
    private AbstractContract contract;
    private ContractType type;
    private int changed = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expressions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final LinearLayout detailsLay = (LinearLayout) findViewById(R.id.detailsLay);
        if (detailsLay == null)
            Log.e("Layout 'detailsLay' not found !");

        contract = getIntent().getExtras().getParcelable(AbstractContract.CONTRACT);
        if (contract == null)
            Log.e("Contract is not set !");

        type = ContractType.valueOf(getIntent().getExtras().getString(AbstractContract.TYPE));

        String title = contract.getName();
        if (!title.contains(type.getDescription())) title += ", " + type.getDescription();
        setTitle(title);

        final Context owner = this;

        mAsyncTaskManager.setupTask(new Task<Set<Expression>>(this)
        {
            @Override
            protected Set<Expression> doInBackground(Object... objects)
            {
                try
                {
                    if (parser == null)
                        parser = new HtmlParser(contract.getContent(), type);

                    return parser.getExpressions();

                } catch (Exception e)
                {
                    Utils.exceptionReport(e);
                }
                return null;
            }

            @Override
            public void onTaskComplete(Task task)
            {
                if (task.isCancelled())
                {
                    ((ExpressionsActivity) owner).finish();
                    return;
                }

                try
                {
                    Set<Expression> expressions = (Set<Expression>) task.get();
                    createExpressionViews(expressions, detailsLay, contract, savedInstanceState);
                } catch (Exception e)
                {
                    Utils.exceptionReport(e);
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_next);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try
                {
                    showPreviewActivity(null);
                } catch (Exception e)
                {
                    Utils.exceptionReport(e);
                }
            }
        });

        if (InternalConfigurations.getInstance().isOpenFirstTimeExpressionActivity())
            showHelp(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_expressions, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        LinearLayout detailsLay = (LinearLayout) findViewById(R.id.detailsLay);
        try
        {
            for (Expression exp : parser.getExpressions())
            {
                View view = detailsLay.findViewWithTag(exp);
                if (view == null)
                    throw new Exception("Не найдено View для выражения '" + exp.getRequest() + "'");

                if (view instanceof EditText)
                    outState.putString(String.valueOf(exp.hashCode()), ((EditText) view).getText().toString());
            }
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Do not remove..
    }

    private void createExpressionViews(Set<Expression> expressions, LinearLayout detailsLay, AbstractContract contract, Bundle savedInstanceState) throws Exception
    {
        String lastGroup = null;
        ViewGroup root = detailsLay;

        for (Expression exp : expressions)
        {
            if (exp.getGroup() != null && !exp.getGroup().equals(lastGroup))
            {
                lastGroup = exp.getGroup();
                addGroupView(detailsLay, exp.getGroup());
            }

            addExpressionView(detailsLay, root, exp, savedInstanceState);
        }

        if (InternalConfigurations.getInstance().checkAllowShowingAD())
        {
            addGroupView(detailsLay, getString(R.string.ads));
            addBanner(detailsLay);
        }
    }

    private void addBanner(LinearLayout detailsLay)
    {
    }

    private void addGroupView(LinearLayout detailsLay, String group)
    {
        View view = getLayoutInflater().inflate(R.layout.expression_group_view, null, false);
        View groupView = view.findViewById(R.id.expressionGroup);

        if (groupView instanceof TextView)
        {
            ((TextView) groupView).setText(group);
        }

        detailsLay.addView(view);
    }

    private void addExpressionView(LinearLayout detailsLay, ViewGroup root, Expression exp, Bundle savedInstanceState)
    {
        View expressionView = getLayoutInflater().inflate(R.layout.expression_view, root, false);

        View nameView = expressionView.findViewById(R.id.expressionName);

        String expressionText = exp.getRequest();
        if (exp.getChoices() != null) expressionText += Constants.CHOICES_DESCRIPTION_SUFFIX;
        else expressionText += ":";

        if (nameView instanceof TextView)
        {
            ((TextView) nameView).setText(expressionText);
        }

        FrameLayout layoutValue = (FrameLayout) expressionView.findViewById(R.id.layoutExpressionValue);

        View valueView = exp.getValueView(this);

        valueView.setTag(exp);

        if (valueView instanceof EditText)
        {
            if (exp.getInputType() != null)
                ((TextView) valueView).setInputType(exp.getInputType());

            if (savedInstanceState != null)
                ((TextView) valueView).setText(savedInstanceState.getString(String.valueOf(exp.hashCode()), exp.getDefaultResponse()));
            else if (exp.getDefaultResponse() != null)
                ((TextView) valueView).setText(exp.getDefaultResponse());

            if (nameView == null)
            {
                ((TextView) valueView).setHint(expressionText);
            }


            final View.OnFocusChangeListener onValueFocusChangeListener = exp.getOnValueFocusChangeListener(this, valueView, expressionText);
            valueView.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    changed++;
                    if (onValueFocusChangeListener != null)
                        onValueFocusChangeListener.onFocusChange(v, hasFocus);
                }
            });
        }

        layoutValue.addView(valueView);
        detailsLay.addView(expressionView);
    }

    private void fillParserExpressions() throws Exception
    {
        LinearLayout detailsLay = (LinearLayout) findViewById(R.id.detailsLay);

        for (Expression exp : parser.getExpressions())
        {
            fillParserExpression(detailsLay, exp);
        }
    }

    private void fillParserExpression(LinearLayout detailsLay, Expression exp) throws Exception
    {
        View view = detailsLay.findViewWithTag(exp);
        if (view == null)
            throw new Exception("Не найдено View для выражения '" + exp.getRequest() + "'");

        if (view instanceof EditText)
            exp.setResponse(((EditText) view).getText().toString());
    }

    @Override
    public void onBackPressed()
    {
        if (changed > MIN_INPUT_CHARACTERS_WHEN_CHANGED_APPLY)
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warning)
                    .setMessage(R.string.message_data_will_be_lost)
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, null)
                    .setNeutralButton(R.string.save, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            save((MenuItem) null);
                        }
                    }).show();
        } else
        {
        }
    }

    public void onPreview(final View view)
    {
        mAsyncTaskManager.setupTask(new AlertDialogTask(this)
        {
            @Override
            protected TextView doInBackground(Object... voids)
            {
                try
                {
                    fillParserExpressions();
                } catch (Exception ex)
                {
                    Log.e(ex.getMessage(), ex);
                    return null;
                }

                // valueView содержит объект Expression в tag.
                View valueView = ((View) view.getParent()).findViewById(R.id.expressionValue);
                if (valueView.getTag() == null)
                {
                    return null;
                }

                Expression exp = (Expression) valueView.getTag();

                String message = null;
                try
                {
                    message = parser.getPreview(exp);
                } catch (Exception ex)
                {
                    Log.e(ex.getMessage(), ex);
                    return null;
                }

                if (message == null || message.isEmpty()) return null;


                Spanned sp = Html.fromHtml(message);
                TextView tv = new TextView(context);
                tv.setText(sp);
                return tv;
            }
        });


    }

    public void showPreviewActivity(MenuItem item)
    {
        final Context owner = this;

        mAsyncTaskManager.setupTask(new ShowIntentTask(this, REQUEST_CODE_ACTIVITY_COMPLETED)
        {
            @Override
            protected Intent doInBackground(Object... voids)
            {
                try
                {
                    Intent intent = new Intent(owner, PreviewActivity.class);
                    fillParserExpressions();
                    String css = Utils.loadDefaultCssStyle(owner);
                    css = Utils.modifyCss(css, type, UserConfigurations.getFontSize(), UserConfigurations.getLeftMargin(), UserConfigurations.getRightMargin(), UserConfigurations.getTopMargin(), UserConfigurations.getBottomMargin());
                    String preview = Utils.injectCssIntoHtml(parser.buildNormal(true), css);
                    String output = parser.buildNormal(false);

                    intent.putExtra(AbstractContract.CONTRACT, contract);
                    intent.putExtra(AbstractContract.TYPE, type.name());
                    intent.putExtra(PreviewActivity.PREVIEW, preview);
                    intent.putExtra(PreviewActivity.OUTPUT, output);

                    return intent;
                } catch (Exception e)
                {
                    Utils.exceptionReport(e);
                    return null;
                }
            }
        });
    }

    public void save(MenuItem item)
    {
        checkWritePermission(new WritePermissionListener()
        {
            @Override
            public void onGranted()
            {
                try
                {
                    showSaveDialog();
                } catch (Exception e)
                {
                    Utils.exceptionReport(e);
                }
            }

            @Override
            public void onDenied()
            {
                Utils.showToast(getApplicationContext(), getString(R.string.no_write_permissions));
            }
        });
    }

    private void save(File file) throws Exception
    {

        XmlSerializer ser = Xml.newSerializer();

        Writer writer = new FileWriter(file);
        ser.setOutput(writer);
        ser.startDocument("UTF-8", true);
        ser.startTag("", XML_TAG_CONFIG);
        ser.attribute("", XML_ATTR_CONFIG_APP_ID, "" + InternalConfigurations.getInstance().getApplicationFirstStartTime());
        ser.endTag("", XML_TAG_CONFIG);

        LinearLayout detailsLay = (LinearLayout) findViewById(R.id.detailsLay);
        for (Expression exp : parser.getExpressions())
        {
            View view = detailsLay.findViewWithTag(exp);
            if (view == null)
            {
                Log.e("Не найдено View для выражения '" + exp.getRequest() + "'");
                continue;
            }

            ser.startTag("", XML_TAG_EXPRESSION);
            ser.attribute("", XML_ATTR_EXPRESSION_HASH, String.valueOf(exp.hashCode()));
            ser.attribute("", XML_ATTR_EXPRESSION_RESPONCE, ((EditText) view).getText().toString());
            ser.endTag("", XML_TAG_EXPRESSION);
        }
        ser.endDocument();
        writer.flush();
        writer.close();
    }

    public void load(MenuItem item)
    {
        OpenFileDialog fileDialog = new OpenFileDialog(this);
        fileDialog.setFilter(Constants.PRELOAD_FILE_EXT);
        fileDialog.show();
        // Next step: OnSelectedFile()
    }


    private void load(File file) throws Exception
    {
        if (!file.exists())
        {
            return;
        }

        if (!InternalConfigurations.getInstance().checkAllowLoadingTemplates())
        {
            throw new LoadPresetFilePermissionException();
        }

        XmlPullParser xmlParser = Xml.newPullParser();
        Reader reader = new FileReader(file);
        xmlParser.setInput(reader);

        LinearLayout detailsLay = (LinearLayout) findViewById(R.id.detailsLay);

        while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT)
        {

            switch (xmlParser.getEventType())
            {
                case XmlPullParser.START_TAG:
                    if (XML_TAG_EXPRESSION.equals(xmlParser.getName()))
                    {
                        String hash = null;
                        String responce = null;
                        for (int i = 0; i < xmlParser.getAttributeCount(); i++)
                        {
                            String attrName = xmlParser.getAttributeName(i);
                            if (XML_ATTR_EXPRESSION_HASH.equals(attrName))
                                hash = xmlParser.getAttributeValue(i);
                            else if (XML_ATTR_EXPRESSION_RESPONCE.equals(attrName))
                                responce = xmlParser.getAttributeValue(i);
                        }

                        if (hash != null && responce != null)
                        {
                            Integer iHash = Integer.parseInt(hash);

                            Expression exp = parser.getExpressionByHash(iHash);

                            if (exp == null)
                            {
                                break;
                            }

                            View view = detailsLay.findViewWithTag(exp);

                            if (view == null)
                                throw new Exception("Не найдено View для выражения '" + exp.getRequest() + "'");

                            if (view instanceof EditText)
                                ((EditText) view).setText(responce);
                            else
                                throw new Exception("Нет обработчика для View: " + view.getClass());

                        } else
                        {
                            Log.e("Ошибка парсера xml. Отсутствует необходимый атрибут, hash: " + hash + ", responce: " + responce);
                        }
                    }
            }

            xmlParser.next();
        }
    }

    private void showSaveDialog() throws Exception
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(R.string.save_config);
        dialog.setMessage(R.string.file_name);

        final EditText input = new EditText(dialog.getContext());
        SimpleDateFormat dateFormat = new SimpleDateFormat(", yyyyMMdd_HHmm");
        String presetFileName = contract.getName() + dateFormat.format(Calendar.getInstance().getTime());
        input.setText(presetFileName);
        dialog.setView(input);

        dialog.setNegativeButton(android.R.string.cancel, null).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                try
                {
                    String fileName = input.getText().toString() + Constants.PRELOAD_FILE_EXT;
                    if (fileName.isEmpty()) return;
                    File presetFile = new File(Utils.getDocumentsFolder(), fileName);
                    save(presetFile);
                    changed = 0;
                } catch (Exception e)
                {
                    Utils.exceptionReport(e);
                }
            }
        });

        dialog.show();
    }

    @Override
    public void onSelectedFile(File fileName)
    {
        if (fileName == null) return;

        try
        {
            load(fileName);
            changed = 0;
        } catch (LoadPresetFilePermissionException e)
        {
            showPurchaseDialog(PermissionsManager.getInstance().getPermission(contract));
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
        }
    }

    private void showPurchaseDialog(final Permission permission)
    {
        final Activity owner = this;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_load_error)
                .setIcon(R.drawable.ic_lock)
                .setMessage(R.string.message_load_error)
                .setCancelable(false)
                .setNegativeButton(R.string.button_exit, null)
                .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        PurchaseManager.getInstance().purchase(owner, InAppConfig.PREMIUM);
                    }
                }).create().show();
    }

    public void showHelp(MenuItem item)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.ic_help);
        dialog.setMessage(R.string.help_expression_activity);

        dialog.setPositiveButton(R.string.button_exit, null);

        dialog.show();

        InternalConfigurations.getInstance().clearFlagOpenFirstTimeExpressionActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE_ACTIVITY_COMPLETED)
            setResult(resultCode);
        else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
