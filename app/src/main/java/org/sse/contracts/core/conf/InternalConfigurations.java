package org.sse.contracts.core.conf;

import android.content.Context;
import android.content.SharedPreferences;

import org.onepf.oms.appstore.googleUtils.Purchase;
import org.sse.contracts.BuildConfig;
import org.sse.contracts.Constants;
import org.sse.contracts.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InternalConfigurations
{
    private static final String PREFERENCES = "preferences";
    private static final String FREE_COUNTER = "freeCounter";
    private static final String PREMIUM_MODE = "premiumMode";
    private static final String GENERATED_COUNTER = "generatedCounter";
    private static final String FIRST_START_TIME = "firstStartTime";
    private static final String OPEN_FIRST_TIME_EXPRESSION_ACTIVITY = "openFirstTimeExpressionActivity";
    private static final String OPEN_FIRST_TIME_PREVIEW_ACTIVITY = "openFirstTimePreviewActivity";
    private static final String LAST_CURRENT_TIME = "lastCurrentTime";
    private static final String CONTRACTS_INDEX = "contractsIndex";
    private static final String LAST_UPDATE_TIME = "lastUpdateTime";

    private static final String SUBSCRIPTION_EXPIRY_TIME = "subscriptionExpiryTime";

    private static final String LAST_SUBSCRIPTION_PURCHASED_TOKEN = "lastSubscriptionPurchasedToken";

    private static InternalConfigurations INSTANCE = null;
    private final Lock availablePurchasesLock = new ReentrantLock();
    private Set<String> availablePurchases = null;

    private InternalConfigurations()
    {
    }

    public static InternalConfigurations getInstance()
    {
        if (INSTANCE == null) INSTANCE = new InternalConfigurations();
        return INSTANCE;
    }


    public long getApplicationFirstStartTime()
    {
        return getPreferences().getLong(FIRST_START_TIME, System.currentTimeMillis());
    }

    private SharedPreferences getPreferences()
    {
        Context context = Utils.getWorkingContext();
        return new ObscuredSharedPreferences(context, context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE));
    }

    public int getFreeCounter()
    {
        return getPreferences().getInt(FREE_COUNTER, Constants.DEFAULT_FREE_DOCUMENTS);
    }

    public void addFreeCounter(int add)
    {
        SharedPreferences pref = getPreferences();
        int value = pref.getInt(FREE_COUNTER, Constants.DEFAULT_FREE_DOCUMENTS);
        pref.edit().putInt(FREE_COUNTER, value + add).commit();
    }

    public void decrementFreeCounter()
    {
        final SharedPreferences prefs = getPreferences();
        int cnt = prefs.getInt(FREE_COUNTER, Constants.DEFAULT_FREE_DOCUMENTS);
        if (cnt-- == 0) cnt = 0;

        prefs.edit().putInt(FREE_COUNTER, cnt).commit();
    }

    public boolean isPremiumMode()
    {
        if (BuildConfig.DEBUG_NO_PREMIUM) return false;

        availablePurchasesLock.lock();
        try
        {
            if (availablePurchases != null)
            {
                if (availablePurchases.contains(InAppConfig.PREMIUM))
                {
                    setPremiumMode();
                    return true;
                }

                clearPremiumMode();
                return false;
            }

            return getPreferences().getBoolean(PREMIUM_MODE, false);
        } finally
        {
            availablePurchasesLock.unlock();
        }
    }

    private void setPremiumMode()
    {
        getPreferences().edit().putBoolean(PREMIUM_MODE, true).commit();
    }

    private void clearPremiumMode()
    {
        getPreferences().edit().putBoolean(PREMIUM_MODE, false).commit();
    }

    private void setContractGroupPurchased(String groupId)
    {
        getPreferences().edit().putBoolean(groupId, true).commit();
    }

    private void clearContractGroupPurchased(String groupId)
    {
        getPreferences().edit().putBoolean(groupId, false).commit();
    }


    public boolean isContractGroupPurchased(String groupId)
    {

        availablePurchasesLock.lock();
        try
        {
            if (availablePurchases != null)
            {
                if (availablePurchases.contains(groupId))
                {
                    setContractGroupPurchased(groupId);
                    return true;
                }
                clearContractGroupPurchased(groupId);
                return false;
            }

            return getPreferences().getBoolean(groupId, false);
        } finally
        {
            availablePurchasesLock.unlock();
        }
    }

    public void addAvailablePurchase(Purchase purchase)
    {
        availablePurchasesLock.lock();
        if (availablePurchases == null) availablePurchases = new HashSet<>();
        availablePurchases.add(purchase.getSku());
        availablePurchasesLock.unlock();
    }

    public Set<String> getAvailablePurchases()
    {
        return availablePurchases;
    }

    public void setAvailablePurchases(List<Purchase> purchases)
    {
        availablePurchasesLock.lock();
        availablePurchases = new HashSet<>();
        for (Purchase p : purchases)
        {
            availablePurchases.add(p.getSku());
        }
        availablePurchasesLock.unlock();
    }

    public void incrementGeneratedCounter()
    {
        final SharedPreferences prefs = getPreferences();
        int cnt = getGeneratedCounter() + 1;

        prefs.edit().putInt(GENERATED_COUNTER, cnt).commit();
    }


    public int getGeneratedCounter()
    {
        return getPreferences().getInt(GENERATED_COUNTER, 0);
    }

    public boolean isOpenFirstTimeExpressionActivity()
    {
        return getPreferences().getBoolean(OPEN_FIRST_TIME_EXPRESSION_ACTIVITY, true);
    }

    public void clearFlagOpenFirstTimeExpressionActivity()
    {
        getPreferences().edit().putBoolean(OPEN_FIRST_TIME_EXPRESSION_ACTIVITY, false).commit();
    }

    public boolean isOpenFirstTimePreviewActivity()
    {
        return getPreferences().getBoolean(OPEN_FIRST_TIME_PREVIEW_ACTIVITY, true);
    }

    public void clearFlagOpenFirstTimePreviewActivity()
    {
        getPreferences().edit().putBoolean(OPEN_FIRST_TIME_PREVIEW_ACTIVITY, false).commit();
    }

    public boolean checkAllowShowingAD()
    {
        if (BuildConfig.DEBUG_AD_ALWAYS) return true;

        if ((availablePurchases != null && availablePurchases.size() > 0) || isPremiumMode() || hasSubscription())
            return false;
        return true;
    }

    public boolean checkAllowLoadingTemplates()
    {
        if (isPremiumMode()) return true;
        if (availablePurchases != null && availablePurchases.size() == 1 && availablePurchases.contains(InAppConfig.DISABLE_AD))
            return false;
        return (availablePurchases != null && availablePurchases.size() > 0);
    }

    public void addConsumingPurchase(Purchase purchase)
    {
        SharedPreferences prefs = getPreferences();

        long currentTime = Utils.getSaveCurrentTimeMillis();

        long endTime = prefs.getLong(SUBSCRIPTION_EXPIRY_TIME, 0);
        long addTime = Utils.convertPurchasedSkuToUsingTime_mls(purchase.getSku());
        if (endTime < currentTime) endTime = currentTime;

        endTime += addTime;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(SUBSCRIPTION_EXPIRY_TIME, endTime);
        editor.putString(LAST_SUBSCRIPTION_PURCHASED_TOKEN, purchase.getToken());

        editor.commit();
    }

    public boolean hasSubscription()
    {
        return getSubscriptionExpiryTime() > Utils.getSaveCurrentTimeMillis();
    }

    public long getSubscriptionExpiryTime()
    {
        return getPreferences().getLong(SUBSCRIPTION_EXPIRY_TIME, 0);
    }

    public String getLastSubscriptionPurchasedToken()
    {
        return getPreferences().getString(LAST_SUBSCRIPTION_PURCHASED_TOKEN, "");
    }

    public long getLastCurrentTime()
    {
        return getPreferences().getLong(LAST_CURRENT_TIME, System.currentTimeMillis());
    }

    public void setLastCurrentTime()
    {
        getPreferences().edit().putLong(LAST_CURRENT_TIME, System.currentTimeMillis()).commit();
    }

    public boolean hasSavedContract(String name)
    {
        return getPreferences().contains(Utils.removeExtension(name));
    }

    public String getSavedContract(String name)
    {
        return getPreferences().getString(Utils.removeExtension(name), null);
    }

    public void setSavedContract(String name, String content)
    {
        getPreferences().edit().putString(Utils.removeExtension(name), content).commit();
    }

    public String getContractsIndex()
    {
        return getPreferences().getString(CONTRACTS_INDEX, null);
    }

    public void setContractsIndex(String index)
    {
        getPreferences().edit().putString(CONTRACTS_INDEX, index).commit();
    }

    public long getLastUpdateTime()
    {
        return getPreferences().getLong(LAST_UPDATE_TIME, 0);
    }

    public void setLastUpdateTime(long time)
    {
        getPreferences().edit().putLong(LAST_UPDATE_TIME, time).commit();
    }
}
