package org.sse.contracts.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import trikita.log.Log;

import org.sse.contracts.Utils;
import org.sse.contracts.Constants;

import java.io.File;
import java.io.FilenameFilter;

public class SelectFileDialogFragment extends DialogFragment
{
    private OnDialogDismissListener listener = null;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        try
        {
            listener = (OnDialogDismissListener) context;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(listener.toString()
                    + " must implement OnDialogDismissListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Choose your file");
        final String fileList[] = loadFileList();

        if (fileList == null)
        {
            Log.e("Showing file picker before loading the file list");
            dialog = builder.create();
            return dialog;
        }

        builder.setItems(fileList, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                String selectedFileName = fileList[which];
                if (listener != null)
                    listener.onDialogDismissListener(new File(getContext().getFilesDir(), selectedFileName));
            }
        });

        dialog = builder.show();
        return dialog;
    }

    private String[] loadFileList()
    {
        File mPath = getContext().getFilesDir();

        try
        {
            mPath.mkdirs();
        } catch (SecurityException e)
        {
            Utils.exceptionReport(e);
        }
        if (mPath.exists())
        {
            FilenameFilter filter = new FilenameFilter()
            {

                @Override
                public boolean accept(File dir, String filename)
                {
                    return filename.contains(Constants.PRELOAD_FILE_EXT);
                }
            };
            return mPath.list(filter);
        } else
        {
            return new String[0];
        }
    }

    public interface OnDialogDismissListener
    {
        public void onDialogDismissListener(File fileName);
    }

}
