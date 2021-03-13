package org.sse.contracts.core.contract;

import trikita.log.Log;

import org.sse.contracts.Utils;
import org.sse.contracts.core.conf.InternalConfigurations;
import org.sse.contracts.Constants;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Contracts
{
    private static Contracts INSTANCE = null;
    private Set<AbstractContract> contracts;

    private Lock lock = new ReentrantLock();

    public static Contracts getInstance()
    {
        if (INSTANCE == null) INSTANCE = new Contracts();
        return INSTANCE;
    }

    private Contracts()
    {
    }

    public Set<AbstractContract> getContracts() throws IOException
    {
        try
        {
            lock.lock();

            if (contracts != null) return contracts;

            contracts = new HashSet();

            String[] files = Utils.getWorkingContext().getAssets().list(Constants.CONTRACTS_PATH);

            firstInit(files);

            ContractsIndexParser index = new ContractsIndexParser(InternalConfigurations.getInstance().getContractsIndex());
            secondInit(index);
        } finally
        {
            lock.unlock();
        }

        return contracts;
    }

    void firstInit(String[] files) throws IOException
    {
        for (String fileName : files)
        {
            {
                File f = new File(fileName);
                if (f.isDirectory()) continue;
            }

            AbstractContract cnt;
            if (InternalConfigurations.getInstance().hasSavedContract(fileName))
            {
                cnt = new LocalSavedContract(fileName);
                String content = cnt.getContent();
                if (content == null || content.isEmpty())
                {
                    cnt = new LocalAssetContract(Constants.CONTRACTS_PATH, fileName);
                }
            } else
            {
                cnt = new LocalAssetContract(Constants.CONTRACTS_PATH, fileName);
            }

            contracts.add(cnt);
        }
    }

    void secondInit(ContractsIndexParser index)
    {
        for (String doc : index.getDocuments())
        {
            LocalSavedContract lsc = new LocalSavedContract(doc);
            if (!contracts.contains(lsc))
            {
                contracts.add(lsc);
                Log.d(" - " + lsc);
            }
        }
    }

    public AbstractContract findByGroupId(String groupId)
    {
        try
        {
            lock.lock();

            for (AbstractContract c : getContracts())
                if (groupId.equals(c.getGroupId())) return c;
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            lock.unlock();
        }

        return null;
    }

    public AbstractContract findByName(String name)
    {
        try
        {
            lock.lock();

            for (AbstractContract c : getContracts())
                if (name.equals(c.getName())) return c;
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            lock.unlock();
        }

        return null;
    }

    public void clear()
    {
        lock.lock();

        contracts = null;
        lock.unlock();
    }
}

