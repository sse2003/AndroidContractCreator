package org.sse.contracts.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import trikita.log.Log;

import org.sse.contracts.Utils;
import org.sse.contracts.activity.listeners.WritePermissionListener;
import org.sse.contracts.core.task_manager.AsyncTaskManager;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public abstract class BasePermissionsActivity extends AppCompatActivity
{
    public static final String TAG_LICENSING = "Licensing";
    public static final String TAG_WRITE_PERM = "WritePermission";



    public static final int REQUEST_CODE_ACTIVITY_COMPLETED = 201;

    public static final int RESULT_CODE_NORMAL_MODE = 1;

    public static final int RESULT_CODE_DEMO_MODE = 2;

    protected final static int WRITE_EXTERNAL_RESULT = 200;
    private WritePermissionListener writePermissionListener;

    protected AsyncTaskManager mAsyncTaskManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Utils.setWorkingContext(getApplicationContext());

        mAsyncTaskManager = new AsyncTaskManager(this);
        mAsyncTaskManager.handleRetainedTask(getLastCustomNonConfigurationInstance());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_RESULT)
        {
            if (writePermissionListener == null)
            {
                return;
            }

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                writePermissionListener.onGranted();
            else
                writePermissionListener.onDenied();
        } else
        {
            Log.w(TAG_WRITE_PERM, "Неизвестный запрос разрешений: " + requestCode);
        }
    }

    protected boolean shouldAskPermission()
    {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public void checkWritePermission(WritePermissionListener listener)
    {
        writePermissionListener = listener;

        if (shouldAskPermission())
        {
            if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                String perms[] = {WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, perms, WRITE_EXTERNAL_RESULT);
            } else
            {
                executePermissionListener(writePermissionListener);
            }
        } else
        {
            executePermissionListener(writePermissionListener);
        }
    }

    private void executePermissionListener(WritePermissionListener listener)
    {
        if (listener != null) listener.onGranted();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ((PurchaseManager.getInstance().getIABHelper() == null) || !PurchaseManager.getInstance().getIABHelper().handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance()
    {
        Log.d("onRetainCustomNonConfigurationInstance");

        // Delegate task retain to manager
        return mAsyncTaskManager.retainTask();
    }
}
