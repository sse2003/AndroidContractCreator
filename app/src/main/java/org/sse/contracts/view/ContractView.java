package org.sse.contracts.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.sse.contracts.R;
import org.sse.contracts.core.contract.AbstractContract;
import org.sse.contracts.core.contract.RemoteContract;

import java.io.IOException;

public class ContractView extends LinearLayout
{
    public static final String TAG = "ContractView";

    private AbstractContract contract;

    public ContractView(Context context)
    {
        super(context);
        init();
    }

    public ContractView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public ContractView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
    }

    public AbstractContract getContract()
    {
        return contract;
    }

    public void setContract(AbstractContract contract)
    {
        this.contract = contract;
        TextView contractText = (TextView) findViewById(R.id.contractText);

        if (contract instanceof RemoteContract)
        {
            try
            {
                String status = (contract.getContent().isEmpty()) ? " (Не загружен)" : " (id:" + contract.getGroupId() + ")";
                contractText.setText(contract.getName() + status);
            } catch (IOException e)
            {
                // Ignore
            }

        } else
        {
            contractText.setText(contract.getName());
        }


    }
}
