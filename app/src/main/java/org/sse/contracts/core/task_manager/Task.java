package org.sse.contracts.core.task_manager;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import org.sse.contracts.R;

/**
 * https://habrahabr.ru/post/114570/
 */

public abstract class Task<Result> extends AsyncTask<Object, String, Result> implements OnTaskCompleteListener
{
    protected Context context;
    private Result mResult;
    private String mProgressMessage;
    private IProgressTracker mProgressTracker;

    /* UI Thread */
    public Task(Context context)
    {
        this.context = context;
        mProgressMessage = context.getString(R.string.processing);
    }

    /* UI Thread */
    public void setProgressTracker(IProgressTracker progressTracker)
    {
        // Attach to progress tracker
        mProgressTracker = progressTracker;

        // Initialise progress tracker with current task state
        if (mProgressTracker != null)
        {
            mProgressTracker.onProgress(mProgressMessage);
            if (mResult != null)
            {
                mProgressTracker.onComplete();
            }
        }
    }

    /* UI Thread */
    @Override
    protected void onCancelled()
    {
        // Detach from progress tracker
        mProgressTracker = null;
    }

    /* UI Thread */
    @Override
    protected void onProgressUpdate(String... values)
    {
        // Update progress message
        mProgressMessage = values[0];
        // And send it to progress tracker
        if (mProgressTracker != null)
        {
            mProgressTracker.onProgress(mProgressMessage);
        }
    }

    /* UI Thread */
    @Override
    protected void onPostExecute(Result result)
    {
        // Update result
        mResult = result;
        // And send it to progress tracker
        if (mProgressTracker != null)
        {
            mProgressTracker.onComplete();
        }
        // Detach from progress tracker
        mProgressTracker = null;
    }
}