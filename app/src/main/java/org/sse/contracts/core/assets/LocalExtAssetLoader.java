package org.sse.contracts.core.assets;

import android.content.Context;
import trikita.log.Log;

import org.sse.contracts.Utils;

import java.io.IOException;
import java.io.InputStream;

public class LocalExtAssetLoader extends LocalAssetLoader
{
    public LocalExtAssetLoader(Context context, String path)
    {
        super(context, path);
    }

    public LocalExtAssetLoader(Context context, String path, boolean encrupted)
    {
        super(context, path, encrupted);
    }


    @Override
    public String load(String fileName)
    {
        return Utils.extractBody(super.load(fileName));
    }
}
