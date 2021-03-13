package org.sse.contracts.core.task_manager;

public interface IProgressTracker
{
    // Updates progress message
    void onProgress(String message);

    // Notifies about task completeness
    void onComplete();
}
