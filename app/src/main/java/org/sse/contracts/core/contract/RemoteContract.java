package org.sse.contracts.core.contract;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import trikita.log.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.sse.contracts.Constants;

import java.io.IOException;

public class RemoteContract extends AbstractContract
{
    private String content = "";

    public RemoteContract(final String path, final String fileName)
    {
        this(null, path, fileName);
    }

    public RemoteContract(final RecyclerView.Adapter recyclerView, final String path, final String fileName)
    {
        super(path, fileName);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference testDocumentsRef = storageRef.child(path);
        final StorageReference fileRef = testDocumentsRef.child(fileName);

        fileRef.getBytes(Constants.FIREBASE_REQUEST_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>()
        {
            @Override
            public void onSuccess(byte[] bytes)
            {
                content = new String(bytes);

                if (recyclerView != null)
                    recyclerView.notifyDataSetChanged();
            }
        });
    }

    public RemoteContract(Parcel in)
    {
        super(in);
        content = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeString(content);
    }

    public static final Parcelable.Creator<RemoteContract> CREATOR = new Parcelable.Creator<RemoteContract>()
    {
        @Override
        public RemoteContract createFromParcel(Parcel in)
        {
            return new RemoteContract(in);
        }

        @Override
        public RemoteContract[] newArray(int size)
        {
            return new RemoteContract[size];
        }
    };

    @Override
    public String loadContent() throws IOException
    {
        return content;
    }
}
