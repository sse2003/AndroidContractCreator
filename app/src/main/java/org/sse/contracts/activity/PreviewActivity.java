package org.sse.contracts.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import trikita.log.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import org.sse.contracts.R;
import org.sse.contracts.Utils;
import org.sse.contracts.activity.listeners.WritePermissionListener;
import org.sse.contracts.core.conf.InternalConfigurations;
import org.sse.contracts.Constants;
import org.sse.contracts.core.ContractType;
import org.sse.contracts.core.PdfCreator;
import org.sse.contracts.core.contract.AbstractContract;
import org.sse.contracts.core.task_manager.Task;

import java.io.File;

public class PreviewActivity extends BasePermissionsActivity implements PurchaseStatusChangeListener
{
    public static final String PREVIEW = "preview";
    public static final String OUTPUT = "output";

    private AbstractContract contract;
    private ContractType type;
    private FloatingActionButton fab_save;
    private FloatingActionButton fab_share;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        contract = getIntent().getExtras().getParcelable(AbstractContract.CONTRACT);
        if (contract == null)
            Log.e("Contract is not set !");

        type = ContractType.valueOf(getIntent().getExtras().getString(AbstractContract.TYPE));

        WebView webView = (WebView) findViewById(R.id.webView);
        if (webView != null)
        {
            webView.setInitialScale(1);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setScrollbarFadingEnabled(false);

            String text = getIntent().getExtras().getString(PREVIEW);
            text = Utils.prepareWebViewHtml(text, "_");
            text = Utils.prepareWebViewHtml(text, Constants.SPACE_SYMBOL);

            webView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
        }

        fab_save = (FloatingActionButton) findViewById(R.id.fab_save);
        if (fab_save != null)
        {
            fab_save.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (checkPurchasePermissions())
                        checkWritePermissionAndCreatePdf();
                }
            });

            purchaseStatusChanged();
        }

        fab_share = (FloatingActionButton) findViewById(R.id.fab_share);
        if (fab_share != null)
        {
            fab_share.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (checkPurchasePermissions())
                        share();
                }
            });

            purchaseStatusChanged();
        }

        if (InternalConfigurations.getInstance().isOpenFirstTimePreviewActivity())
            showHelp(null);

        PurchaseManager.getInstance().addPurchaseStatusChangeListener(this);
    }

    @Override
    protected void onDestroy()
    {
        PurchaseManager.getInstance().removePurchaseStatusChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }

    public void showHelp(MenuItem item)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.ic_help);
        dialog.setMessage(R.string.help_preview_activity);

        dialog.setPositiveButton(R.string.button_exit, null);

        dialog.show();
        InternalConfigurations.getInstance().clearFlagOpenFirstTimePreviewActivity();
    }

    public boolean checkPurchasePermissions()
    {
        Permission permission = PermissionsManager.getInstance().getPermission(contract);

        if (permission.check())
            return true;

        startActivity(new Intent(this, PurchaseActivity.class));
        return false;
    }

    public void checkWritePermissionAndCreatePdf()
    {
        final Context parent = this;
        checkWritePermission(new WritePermissionListener()
        {
            @Override
            public void onGranted()
            {
                mAsyncTaskManager.setupTask(new Task<PdfCreator.Result>(parent)
                {
                    @Override
                    protected PdfCreator.Result doInBackground(Object... voids)
                    {
                        return createPdf();
                    }

                    @Override
                    public void onTaskComplete(Task task)
                    {
                        if (task.isCancelled())
                        {
                            return;
                        }

                        try
                        {
                            PdfCreator.Result result = (PdfCreator.Result) task.get();
                            if (result != null && result.isSuccessful())
                            {
                                uploadFile(result);
                                Utils.showToast(getApplicationContext(), getString(R.string.document_сreated) + result.getCreatedFile().getName());
                            } else
                            {
                                showErrorCreationDocument();
                            }
                        } catch (Exception e)
                        {
                            Utils.exceptionReport(e);
                        }
                    }
                });
            }

            @Override
            public void onDenied()
            {
                Utils.showToast(getApplicationContext(), getString(R.string.no_write_permissions));
            }
        });
    }

    public PdfCreator.Result createPdf()
    {
        try
        {
            Context context = getApplicationContext();
            String filename = contract.getName();
            String output = getIntent().getExtras().getString(OUTPUT);
            File folder = Utils.getDocumentsFolder();

            PdfCreator.Result result = PdfCreator.createPDF(PdfCreator.DocumentType.Normal, type, output, folder, filename, context);

            if (result.isSuccessful())
            {
                InternalConfigurations.getInstance().incrementGeneratedCounter();
            }

            setResultCode();

            return result;

        } catch (Throwable e)
        {
            Utils.exceptionReport(e);
        }

        return new PdfCreator.Result(false);
    }

    private void setResultCode()
    {
        Permission permission = PermissionsManager.getInstance().getPermission(contract);
        setResult(permission.isDemo() ? RESULT_CODE_DEMO_MODE : RESULT_CODE_NORMAL_MODE);
    }

    public void foundBug(MenuItem item)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.found_a_bug);
        alert.setMessage(R.string.message_write_bug_description);

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(R.string.button_send, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String value = input.getText().toString();
                if (value.length() > 5)
                {
                    if (DataBaseHelper.addBugOfContract(getApplicationContext(), contract.getName(), type, value, contract.getVersion()))
                        Utils.showToast(getApplicationContext(), getString(R.string.message_request_created));
                }
            }
        });

        alert.setNegativeButton(R.string.button_exit, null);
        alert.show();
    }

    public void share()
    {
        final Context parent = this;
        checkWritePermission(new WritePermissionListener()
        {
            @Override
            public void onGranted()
            {
                Utils.disableDeathOnFileUriExposure();

                mAsyncTaskManager.setupTask(new Task<PdfCreator.Result>(parent)
                {
                    @Override
                    protected PdfCreator.Result doInBackground(Object... voids)
                    {
                        return createPdf();
                    }

                    @Override
                    public void onTaskComplete(Task task)
                    {
                        if (task.isCancelled())
                        {
                            return;
                        }

                        try
                        {
                            PdfCreator.Result result = (PdfCreator.Result) task.get();

                            if (!result.isSuccessful())
                            {
                                showErrorCreationDocument();
                                return;
                            }

                            final Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType(Constants.MIME_PDF);
                            Uri uri = Uri.fromFile(result.getCreatedFile());
                            intent.putExtra(Intent.EXTRA_STREAM, uri);

                            try
                            {
                                startActivity(Intent.createChooser(intent, getString(R.string.share)));
                            } catch (android.content.ActivityNotFoundException ex)
                            {
                                Utils.exceptionReport(ex);
                                Utils.showToast(getApplicationContext(), getString(R.string.error));
                            }

                        } catch (Exception e)
                        {
                            Utils.exceptionReport(e);
                        }
                    }
                });
            }

            @Override
            public void onDenied()
            {
                Utils.showToast(getApplicationContext(), getString(R.string.no_write_permissions));
            }
        });


    }

    private void showErrorCreationDocument()
    {
        Utils.showToast(getApplicationContext(), getString(R.string.error_сreating_document));
    }

    private void uploadFile(PdfCreator.Result result)
    {
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManager.addCompletedDownload(result.getCreatedFile().getName(), result.getCreatedFile().getName(), false, Constants.MIME_PDF, result.getCreatedFile().getAbsolutePath(), result.getCreatedFile().length(), true);
    }

    @Override
    public void purchaseStatusChanged()
    {

        if (fab_save == null || fab_share == null)
        {
            return;
        }

        Permission permission = PermissionsManager.getInstance().getPermission(contract);

        if (permission.check())
        {
            fab_save.setImageResource(R.drawable.ic_save);
            fab_share.setVisibility(View.VISIBLE);
        } else
        {
            fab_save.setImageResource(R.drawable.ic_lock);
            fab_share.setVisibility(View.INVISIBLE);
        }
    }
}
