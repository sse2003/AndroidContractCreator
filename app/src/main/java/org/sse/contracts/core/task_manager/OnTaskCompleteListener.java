package org.sse.contracts.core.task_manager;

public interface OnTaskCompleteListener
{
    // Notifies about task completeness
    void onTaskComplete(Task task);
}
