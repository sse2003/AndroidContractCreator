package org.sse.contracts.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.sse.contracts.R;
import org.sse.contracts.Constants;

public class PurchaseElementView extends LinearLayout
{
    public static final String TAG = "PurchaseElementView";

    public PurchaseElementView(Context context)
    {
        super(context);
        init();
    }

    public PurchaseElementView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public PurchaseElementView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
    }

    public void setSkuDetails(final Activity owner, final PurchaseWrapper purchaseWrapper)
    {
        TextView descriptionView = (TextView) findViewById(R.id.purchaseDescriptionText);
        TextView costView = (TextView) findViewById(R.id.purchaseCostText);
        ImageView permissionImage = (ImageView) findViewById(R.id.permissionImage);

        String title = purchaseWrapper.getTitle().replaceFirst(Constants.PATTERN_PURCHASE_SKU_TITLE_REPLACE, "");
        if (title.isEmpty()) title = purchaseWrapper.getTitle();
        descriptionView.setText(title);

        boolean purchased = true;

        if (purchased)
        {
            costView.setVisibility(INVISIBLE);
            permissionImage.setVisibility(VISIBLE);

            descriptionView.setTextColor(getResources().getColor(R.color.purchasedElement));
            costView.setTextColor(getResources().getColor(R.color.purchasedElement));

            setOnClickListener(null);
            setClickable(false);
        } else
        {
            costView.setVisibility(VISIBLE);
            permissionImage.setVisibility(INVISIBLE);

            costView.setText(purchaseWrapper.getPrice());
            descriptionView.setTextColor(getResources().getColor(R.color.defaultElement));
            costView.setTextColor(getResources().getColor(R.color.defaultElement));

            if (purchaseWrapper.getOnClickListener() != null)
                setOnClickListener(purchaseWrapper.getOnClickListener());
            else
                setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                    }
                });
        }

        descriptionView.setTypeface(purchaseWrapper.getTypeFace());
    }
}
