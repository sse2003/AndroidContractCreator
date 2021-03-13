package org.sse.contracts.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import trikita.log.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.sse.contracts.R;
import org.sse.contracts.Utils;
import org.sse.contracts.activity.listeners.ContractClickListener;
import org.sse.contracts.activity.recycler.DividerItemDecoration;
import org.sse.contracts.Constants;
import org.sse.contracts.core.contract.AbstractContract;
import org.sse.contracts.core.contract.RemoteContract;
import org.sse.contracts.view.ContractView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DevelopmentActivity extends BasePermissionsActivity
{
    private DevelopmentActivity.RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_development);
        setTitle(R.string.title_activity_development);
        Log.d("onCreate");

        try
        {
            RecyclerView view = (RecyclerView) findViewById(R.id.developmentRecycler);

            view.setLayoutManager(new LinearLayoutManager(this));
            view.addItemDecoration(new DividerItemDecoration(this));


            recyclerAdapter = new DevelopmentActivity.RecyclerAdapter(this);
            view.setAdapter(recyclerAdapter);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference testDocuments = storageRef.child(Constants.TEST_CONTRACTS_PATH);


            StorageReference index = testDocuments.child(Constants.FILE_INDEX_NAME);

            index.getBytes(5 * 1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>()
            {
                @Override
                public void onSuccess(byte[] bytes)
                {
                    String result = new String(bytes);
                    String[] results = result.split("\r\n");
                    recyclerAdapter.setContracts(results);
                }
            });

        } catch (Exception e)
        {
            Utils.exceptionReport(e);
        }
    }


    @Override
    protected void onDestroy()
    {
        Log.d("onDestroy");
        super.onDestroy();
    }


    public class RecyclerAdapter extends RecyclerView.Adapter<DevelopmentActivity.RecyclerAdapter.ViewHolder>
    {

        private final DevelopmentActivity owner;
        private List<AbstractContract> allContracts = new ArrayList();

        public RecyclerAdapter(DevelopmentActivity owner) throws IOException
        {
            this.owner = owner;
        }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new ViewHolder(owner, LayoutInflater.from(parent.getContext()).inflate(R.layout.contract_view, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position)
        {
            holder.contractView.setContract(allContracts.get(position));
        }

        @Override
        public int getItemCount()
        {
            return allContracts.size();
        }

        public void setContracts(String[] results)
        {
            allContracts.clear();
            for (String name : results)
            {
                if (name.isEmpty())
                    continue;

                allContracts.add(new RemoteContract(this, Constants.TEST_CONTRACTS_PATH, name));
            }
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public final ContractView contractView;

            public ViewHolder(final DevelopmentActivity owner, View v)
            {
                super(v);

                contractView = (ContractView) v.findViewById(R.id.contractView);
                if (contractView != null)
                {
                    contractView.setOnClickListener(new ContractClickListener(owner));
                }
            }
        }
    }
}
