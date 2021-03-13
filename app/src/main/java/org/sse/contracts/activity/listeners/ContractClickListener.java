package org.sse.contracts.activity.listeners;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;

import org.sse.contracts.Utils;
import org.sse.contracts.activity.BasePermissionsActivity;
import org.sse.contracts.activity.ExpressionsActivity;
import org.sse.contracts.core.ContractType;
import org.sse.contracts.core.contract.AbstractContract;
import org.sse.contracts.view.ContractView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContractClickListener implements View.OnClickListener
{
    private final BasePermissionsActivity baseActivity;

    private List<ContractType> types;

    public ContractClickListener(BasePermissionsActivity baseActivity)
    {
        this.baseActivity = baseActivity;
    }

    @Override
    public void onClick(View view)
    {
        ContractView cv = (ContractView) view;
        showDialog(cv.getContract());
    }

    private void showDialog(final AbstractContract contract)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(baseActivity);
        final List<String> typeItems = new ArrayList<>(4);

        dialog.setTitle(contract.getName());
        try
        {
            types = contract.getCompatibleContractTypes();
            if (types.isEmpty())
            {
                showExpressionActivity(baseActivity, contract, ContractType.ФИЗ_ФИЗ.name());
                return;
            } else if (types.size() == 1)
            {
                showExpressionActivity(baseActivity, contract, types.get(0).name());
                return;
            } else
            {
                for (ContractType type : types)
                {
                    typeItems.add(type.getDescription());
                }

            }
        } catch (IOException e)
        {
            Utils.exceptionReport(e);
        }

        dialog.setSingleChoiceItems(typeItems.toArray(new String[0]), -1, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                showExpressionActivity(baseActivity, contract, types.get(which).name());
                dialog.dismiss();
            }
        });

        dialog.create().show();
    }

    private void showExpressionActivity(Context context, AbstractContract contract, String type)
    {
//        Analytics.logSelectContent(context, type, Analytics.ContentType.Type);

        Intent intent = new Intent(context, ExpressionsActivity.class);
        intent.putExtra(AbstractContract.CONTRACT, contract);
        if (type != null)
            intent.putExtra(AbstractContract.TYPE, type);

        baseActivity.startActivityForResult(intent, BasePermissionsActivity.REQUEST_CODE_ACTIVITY_COMPLETED);
    }
}
