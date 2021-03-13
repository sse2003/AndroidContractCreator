package org.sse.contracts.core.task_manager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import trikita.log.Log;
import android.widget.TextView;

import org.sse.contracts.R;
import org.sse.contracts.Utils;

public abstract class ShowIntentTask extends Task<Intent>
{
    final int requestCode;

    public ShowIntentTask(FragmentActivity fragmentActivity, int requestCode)
    {
        super(fragmentActivity);
        this.requestCode = requestCode;
    }

    @Override
    public void onTaskComplete(Task task)
    {
        if (task.isCancelled())
        {
            Log.d("Задача отменена");
        } else
        {
            Log.d("Задача выполнена");

            try
            {
                Intent result = (Intent) task.get();
                if (result == null) return;

                ((FragmentActivity)context).startActivityForResult(result, requestCode);
            } catch (Exception e)
            {
                Utils.exceptionReport(e);
            }
        }
    }
}
