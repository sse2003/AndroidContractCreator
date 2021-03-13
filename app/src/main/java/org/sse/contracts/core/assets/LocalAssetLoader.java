package org.sse.contracts.core.assets;

import android.content.Context;
import trikita.log.Log;

import org.sse.contracts.Utils;

import java.io.IOException;
import java.io.InputStream;

public class LocalAssetLoader implements AbstractAssetLoader
{
    private Context context;
    private String  path;
    private boolean encrupted = true;


    public LocalAssetLoader(Context context, String path)
    {
        this.context = context;
        this.path = path;
    }

    public LocalAssetLoader(Context context, String path, boolean encrupted)
    {
        this.context = context;
        this.path = path;
        this.encrupted = encrupted;
    }

    @Override
    public String load(String fileName)
    {
        if (context == null)
        {
            return "";
        }

        try
        {
            InputStream in = context.getAssets().open(path + "/" + fileName);
            String content = encrupted ? Utils.convertAndDecryptStreamToString(in) : Utils.convertStreamToString(in);
            in.close();
            return content;
        } catch (IOException e)
        {
            Utils.exceptionReport(e);
        }
           return "";
    }
}
