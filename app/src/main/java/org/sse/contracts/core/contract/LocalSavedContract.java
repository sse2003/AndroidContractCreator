package org.sse.contracts.core.contract;

import android.os.Parcel;

import org.sse.contracts.core.conf.InternalConfigurations;

import java.io.IOException;

public class LocalSavedContract extends AbstractContract
{
    public LocalSavedContract(String fileName)
    {
        super("", fileName);
    }

    public static final Creator<LocalSavedContract> CREATOR = new Creator<LocalSavedContract>()
    {
        @Override
        public LocalSavedContract createFromParcel(Parcel in)
        {
            return new LocalSavedContract(in);
        }

        @Override
        public LocalSavedContract[] newArray(int size)
        {
            return new LocalSavedContract[size];
        }
    };

    public LocalSavedContract(Parcel in)
    {
        super(in);
    }

    @Override
    public String loadContent() throws IOException
    {
        return InternalConfigurations.getInstance().getSavedContract(fileName);
    }
}
