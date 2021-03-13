package org.sse.contracts.activity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.sse.contracts.BuildConfig;
import org.sse.contracts.R;
import org.sse.contracts.Utils;
import org.sse.contracts.activity.listeners.ContractClickListener;
import org.sse.contracts.activity.listeners.PermissionClickListener;
import org.sse.contracts.activity.recycler.DividerItemDecoration;
import org.sse.contracts.core.conf.InternalConfigurations;
import org.sse.contracts.Constants;
import org.sse.contracts.core.contract.AbstractContract;
import org.sse.contracts.core.contract.Contracts;
import org.sse.contracts.core.contract.ContractsIndexParser;
import org.sse.contracts.view.ContractView;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import trikita.log.Log;


public class MainActivity extends BasePermissionsActivity implements NavigationView.OnNavigationItemSelectedListener, PurchaseStatusChangeListener
{
    private static final String TAG_UPDATE = "Update";

    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mLicenseChecker;
    private RecyclerAdapter contractsRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (!BuildConfig.DEBUG)
            Log.level(Log.W);

        Crashlytics.setUserIdentifier(Utils.getUserId());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (InternalConfigurations.getInstance().isPremiumMode())
            setTitle(getTitle() + ", " + getString(R.string.premium));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            @Override
            public void onDrawerOpened(View drawerView)
            {
                if (navigationView != null)
                {

                    if (InternalConfigurations.getInstance().isPremiumMode())
                    {
                        hideMenu((MenuItem) navigationView.getMenu().findItem(R.id.menu_buy));
                    }

                    if (!InternalConfigurations.getInstance().checkAllowShowingAD())
                    {
                        hideMenu((MenuItem) navigationView.getMenu().findItem(R.id.menu_AD_disable));
                    }

                    MenuItem expiredInfoMenu = (MenuItem) navigationView.getMenu().findItem(R.id.menu_expired_time_info);

                    if (expiredInfoMenu != null)
                    {
                        if (InternalConfigurations.getInstance().hasSubscription() && !InternalConfigurations.getInstance().isPremiumMode())
                        {
                            navigationView.getMenu().setGroupVisible(R.id.menu_group_expired_time_info, true);
                            String text = getResources().getString(R.string.subscriptions_expired);
                            Date expired = new Date(InternalConfigurations.getInstance().getSubscriptionExpiryTime());
                            expiredInfoMenu.setTitle(text + "\r\n  " + Constants.DEFAULT_DATE_FORMAT.format(expired));
                        } else
                        {
                            navigationView.getMenu().setGroupVisible(R.id.menu_group_expired_time_info, false);
                        }
                    }

                    if (Utils.isDeveloper())
                    {
                        navigationView.getMenu().setGroupVisible(R.id.menu_group_development, true);
                    }
                }
                super.onDrawerOpened(drawerView);
            }
        };

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        if (navigationView != null)
        {
            navigationView.setNavigationItemSelectedListener(this);
        }

        // Try to use more data here. ANDROID_ID is a single point of attack.
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Library calls this when it's done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback(this);
        // Construct the LicenseChecker with a policy.
        mLicenseChecker = new LicenseChecker(this, new ServerManagedPolicy(this, new AESObfuscator(Constants.SALT, getPackageName(), deviceId)),
                Constants.GOOGLE_BASE64_PUBLIC_KEY);


        try
        {
            RecyclerView view = (RecyclerView) findViewById(R.id.contractsRecycler);

            view.setLayoutManager(new LinearLayoutManager(this));
            view.addItemDecoration(new DividerItemDecoration(this));


            contractsRecyclerAdapter = new RecyclerAdapter(this);
            view.setAdapter(contractsRecyclerAdapter);

            InternalConfigurations.getInstance().getApplicationFirstStartTime();
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
        }

        PurchaseManager.getInstance().addPurchaseStatusChangeListener(this);

        try
        {

            doCheckLicensing();
            checkUpdate(Utils.isRunningOnDebugMode());
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
        }

    }

    private void hideMenu(MenuItem item)
    {
        if (item != null) item.setVisible(false);
    }

    private void doCheckLicensing()
    {
        mLicenseChecker.checkAccess(mLicenseCheckerCallback);
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = null;
        if (searchItem != null)
        {
            searchView = (SearchView) searchItem.getActionView();

            if (searchView != null)
            {
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
                {
                    @Override
                    public boolean onQueryTextSubmit(String query)
                    {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText)
                    {
                        if (contractsRecyclerAdapter == null) return false;

                        contractsRecyclerAdapter.filter(newText);
                        return true;
                    }
                });
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.menu_not_found_contract:
                showNotFoundDialog();
            break;

            case R.id.menu_buy:
                showPurchaseActivity();
            break;

            case R.id.menu_AD_disable:
                showAdDisableDialog();
            break;

            case R.id.menu_development:
                showDevelopmentActivity();
            break;

            case R.id.menu_check_update:
                checkUpdate(true);
            break;

            case R.id.menu_settings:
                showSettingsActivity();
            break;

            default:
                Log.e("Нет обработчика меню для id: " + id);
            break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSettingsActivity()
    {
        Log.d("showSettingsActivity");

        Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
        startActivity(intent);
    }

    private void checkUpdate(boolean force)
    {
        final long currentTime = System.currentTimeMillis();
        long dif = currentTime - InternalConfigurations.getInstance().getLastUpdateTime();


        if (!force)
        {
            if (dif < Constants.UPDATE_TIME_PERIOD)
            {
                return;
            }
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        final StorageReference testDocuments = storageRef.child(Constants.CONTRACTS_PATH);

        final StorageReference index = testDocuments.child(Constants.FILE_INDEX_NAME);

        index.getBytes(Constants.FIREBASE_REQUEST_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>()
        {
            @Override
            public void onSuccess(byte[] bytes)
            {
                String receivedIndex = new String(bytes);

                ContractsIndexParser newIndex = new ContractsIndexParser(receivedIndex);

                for (final String newDoc : newIndex.getDocuments())
                {
                    AbstractContract lastVersion = null;

                    lastVersion = Contracts.getInstance().findByName(newDoc);

                    if (lastVersion == null || lastVersion.getVersion() == null || newIndex.getVersion(newDoc) > lastVersion.getVersion())
                    {
                        final StorageReference fileRef = testDocuments.child(newDoc + Constants.DOCUMENT_EXT);
                        fileRef.getBytes(Constants.FIREBASE_REQUEST_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>()
                        {
                            @Override
                            public void onSuccess(byte[] bytes)
                            {
                                String newContent = new String(bytes);

                                InternalConfigurations.getInstance().setSavedContract(newDoc, newContent);
                                Contracts.getInstance().clear();

                                try
                                {
                                    if (contractsRecyclerAdapter != null)
                                    {
                                        contractsRecyclerAdapter.reinit();
                                        contractsRecyclerAdapter.notifyDataSetChanged();
                                    }
                                } catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }

                InternalConfigurations.getInstance().setContractsIndex(receivedIndex);
                InternalConfigurations.getInstance().setLastUpdateTime(currentTime);
            }
        });
    }

    private void showDevelopmentActivity()
    {
        Log.d("showDevelopmentActivity");

        Intent intent = new Intent(getBaseContext(), DevelopmentActivity.class);
        startActivity(intent);
    }

    private void showAdDisableDialog()
    {
        PurchaseManager.getInstance().purchase(this, InAppConfig.DISABLE_AD);
    }

    private void showNotFoundDialog()
    {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder.setTitle(R.string.not_found_contract)
                .setMessage(R.string.message_not_found_contract)
                .setIcon(R.drawable.ic_info_details)
                .setCancelable(false)
                .setNegativeButton(R.string.button_exit, null)
                .setPositiveButton(R.string.button_leave_feedback, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        final String appPackageName = getPackageName(); //
                        try
                        {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe)
                        {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                });

        dialogBuilder.show();
    }

    private void showPurchaseActivity()
    {
        PurchaseManager.getInstance().purchase(this, InAppConfig.PREMIUM);
    }

    @Override
    protected void onDestroy()
    {
        Log.d("onDestroy");
        PurchaseManager.getInstance().removePurchaseStatusChangeListener(this);
        super.onDestroy();
        mLicenseChecker.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d("onActivityResult, requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == REQUEST_CODE_ACTIVITY_COMPLETED)
        {
            if (resultCode == RESULT_CODE_DEMO_MODE)
            {
                InternalConfigurations.getInstance().decrementFreeCounter();
            }
            requestToRepaintActivity();
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestToRepaintActivity()
    {
        RecyclerView rv = (RecyclerView) findViewById(R.id.contractsRecycler);
        if (rv != null && rv.getAdapter() != null) rv.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void purchaseStatusChanged()
    {
        requestToRepaintActivity();
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
    {
        private final MainActivity owner;
        private List<AbstractContract> allContracts;
        private List<AbstractContract> filterContracts;

        // Конструктор
        public RecyclerAdapter(MainActivity owner) throws IOException
        {
            this.owner = owner;
            reinit();
        }

        public void reinit() throws IOException
        {
            this.allContracts = new LinkedList<>(Contracts.getInstance().getContracts());
            Collections.sort(allContracts, new Comparator<AbstractContract>()
            {
                @Override
                public int compare(AbstractContract t1, AbstractContract t2)
                {
                    return t1.getName().compareTo(t2.getName());
                }
            });
            this.filterContracts = new LinkedList<>(allContracts);
        }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new ViewHolder(owner, LayoutInflater.from(parent.getContext()).inflate(R.layout.contract_view, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position)
        {
            holder.contractView.setContract(filterContracts.get(position));
        }

        @Override
        public int getItemCount()
        {
            return filterContracts.size();
        }

        public void filter(String filterText)
        {
            filterContracts.clear();
            for (AbstractContract c : allContracts)
            {
                if (filterText.isEmpty() || c.getName().toLowerCase().contains(filterText.toLowerCase()))
                    filterContracts.add(c);
            }

            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position)
        {
            return super.getItemViewType(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {

            public final ContractView contractView;

            public ViewHolder(final MainActivity owner, View v)
            {
                super(v);

                contractView = (ContractView) v.findViewById(R.id.contractView);
                if (contractView != null)
                {
                    contractView.setOnClickListener(new ContractClickListener(owner));
                    PermissionView permissionView = (PermissionView) contractView.findViewById(R.id.permissionView);
                    if (permissionView != null)
                    {
                        permissionView.setOnClickListener(new PermissionClickListener(owner));
                    }
                }
            }
        }
    }

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback
    {
        private Context context;

        public MyLicenseCheckerCallback(Context context)
        {
            this.context = context;
        }

        public void allow(int policyReason)
        {
            if (isFinishing())
            {
                // Don't update UI if Activity is finishing.
                return;
            }
        }

        public void dontAllow(int policyReason)
        {

            if (isFinishing())
            {
                // Don't update UI if Activity is finishing.
                return;
            }

            // Should not allow access. In most cases, the app should assume
            // the user has access unless it encounters this. If it does,
            // the app should inform the user of their unlicensed ways
            // and then either shut down the app or limit the user to a
            // restricted set of features.
            // In this example, we show a dialog that takes the user to Market.
            // If the reason for the lack of license is that the service is
            // unavailable or there is another problem, we display a
            // retry button on the dialog and a different message.

//            displayDialog(policyReason == Policy.RETRY);
        }

        public void applicationError(int errorCode)
        {
            String msg = "LicenseChecker, error code: " + errorCode;
            Log.e(TAG_LICENSING, msg);

            Utils.exceptionReport(new Exception(msg));

            if (isFinishing())
            {
                // Don't update UI if Activity is finishing.
                return;
            }
            // This is a polite way of saying the developer made a mistake
            // while setting up or calling the license checker library.
            // Please examine the error code and fix the error.
        }
    }
}
