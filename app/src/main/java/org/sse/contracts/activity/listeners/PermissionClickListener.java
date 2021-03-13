package org.sse.contracts.activity.listeners;


import android.content.Intent;
import android.view.View;

import org.sse.contracts.Utils;
import org.sse.contracts.activity.BasePermissionsActivity;

public class PermissionClickListener implements View.OnClickListener
{
    private BasePermissionsActivity baseActivity;

    public PermissionClickListener(BasePermissionsActivity baseActivity)
    {
        this.baseActivity = baseActivity;
    }

    @Override
    public void onClick(View view)
    {
        PermissionView permissionView = (PermissionView) view;
        Permission perm = permissionView.getPermission();

        if (perm.check())
        {
            String description = perm.getDescription(view.getContext());
            if (description != null)
                Utils.showToast(view.getContext(), description);
        } else
        {
            baseActivity.startActivity(new Intent(baseActivity, PurchaseActivity.class));
        }
    }
}
