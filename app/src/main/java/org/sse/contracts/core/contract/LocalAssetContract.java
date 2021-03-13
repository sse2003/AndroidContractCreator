package org.sse.contracts.core.contract;

import android.os.Parcel;
import android.os.Parcelable;

import org.sse.contracts.Constants;
import org.sse.contracts.Utils;
import org.sse.contracts.core.assets.LocalAssetLoader;

public class LocalAssetContract extends AbstractContract
{
    public LocalAssetContract(String path, String fileName)
    {
        super(path, fileName);
    }

    public static final Parcelable.Creator<LocalAssetContract> CREATOR = new Parcelable.Creator<LocalAssetContract>()
    {
        @Override
        public LocalAssetContract createFromParcel(Parcel in)
        {
            return new LocalAssetContract(in);
        }

        @Override
        public LocalAssetContract[] newArray(int size)
        {
            return new LocalAssetContract[size];
        }
    };

    public LocalAssetContract(Parcel in)
    {
        super(in);
    }

    @Override
    public String loadContent()
    {
        return new LocalAssetLoader(Utils.getWorkingContext(), Constants.CONTRACTS_PATH).load(fileName);
    }
}
