package org.sse.contracts.core.task_manager;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import trikita.log.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.sse.contracts.R;
import org.sse.contracts.Utils;

public abstract class AlertDialogTask extends Task<TextView>
{
    public AlertDialogTask(Context context)
    {
        super(context);
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
                TextView result = (TextView) task.get();
                if (result == null) return;

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(result)
                        .setPositiveButton(R.string.button_exit, null);
                builder.show();

            } catch (Exception e)
            {
                Utils.exceptionReport(e);
            }
        }
    }
}
