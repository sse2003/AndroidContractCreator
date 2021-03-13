package org.sse.contracts.core.contract;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import trikita.log.Log;

import org.sse.contracts.Utils;
import org.sse.contracts.Constants;
import org.sse.contracts.core.ContractType;
import org.sse.contracts.core.HtmlParser;
import org.sse.contracts.core.assets.LocalExtAssetLoader;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;

public abstract class AbstractContract implements Parcelable
{
    public static final String CONTRACT = "contract_normal";
    public static final String TYPE = "type";

    protected String path;
    protected String fileName;
    protected String groupId = null;
    protected Integer version = null;

    public AbstractContract(String path, String fileName)
    {
        this.path = path;
        this.fileName = fileName;
    }

    protected AbstractContract(Parcel in)
    {
        path = in.readString();
        fileName = in.readString();
    }

    protected abstract String loadContent() throws IOException;

    final public String getContent() throws IOException
    {
        return Utils.prepareExtExpressions(loadContent(), new LocalExtAssetLoader(Utils.getWorkingContext(), Constants.EXT_PATH));
    }

    public List<ContractType> getCompatibleContractTypes() throws IOException
    {
        Context context = Utils.getWorkingContext();
        if (context == null)
            throw new IOException("Context не задан");

        HtmlParser p = new HtmlParser(getContent(), null);
        return p.getCompatibleContractTypes();
    }

    public String getName()
    {
        return Utils.removeFileExtension(fileName);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(path);
        dest.writeString(fileName);
    }

    public String getGroupId()
    {
        if (groupId == null)
        {
            try
            {
                String content = getContent();
                if (content == null)
                {
                    content = getContent();
                    Log.e( "Content = null !!!");
                }
                Matcher m = Constants.PATTERN_GROUP_ID.matcher(content);
                if (m.find()) groupId = m.group(Constants.GROUP_GROUP_ID);
            } catch (IOException e)
            {
                Utils.exceptionReport(e);
            }
        }
        return groupId;
    }

    public Integer getVersion()
    {
        if (version == null)
        {
            try
            {
                String content = getContent();
                if (content == null) return null;

                Matcher m = Constants.PATTERN_DOCUMENT_VERSION.matcher(content);
                if (m.find())
                {
                    String v = m.group(Constants.GROUP_DOCUMENT_VERSION);
                    version = Integer.valueOf(v);
                }
            } catch (Exception e)
            {
                Utils.exceptionReport(e);
            }
        }
        return version;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null) return false;

        AbstractContract that = (AbstractContract) o;

        return fileName != null ? Utils.removeExtension(fileName).equals(Utils.removeExtension(that.fileName)) : that.fileName == null;
    }

    @Override
    public int hashCode()
    {
        return fileName != null ? Utils.removeExtension(fileName).hashCode() : 0;
    }

    @Override
    public String toString()
    {
        Integer ver = getVersion();

        return getClass().getName() + "{" +
                "fileName='" + fileName + '\'' +
                ", version=" + ((ver != null) ? ver : "null") +
                '}';
    }
}
