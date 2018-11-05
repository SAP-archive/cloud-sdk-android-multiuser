package com.example.android.multiuser;

import com.sap.cloud.android.odata.espmcontainer.Customer;
import com.sap.cloud.android.odata.espmcontainer.ESPMContainer;
import com.sap.cloud.mobile.odata.offline.OfflineODataProvider;

import java.util.ArrayList;

public class StorageManager {
    private static OfflineODataProvider sharedDataProvider;
    private static ESPMContainer sharedESPMContainer;
    private static OfflineODataProvider currentUserDataProvider;
    private static ESPMContainer currentUserESPMContainer;
    public static CustomerRecyclerViewAdapter adapter;
    public static ArrayList<Customer> customersListToDisplay = new ArrayList<>();

    private static final StorageManager INSTANCE = new StorageManager();

    public static StorageManager getInstance() {
        return INSTANCE;
    }

    public OfflineODataProvider getCurrentUserOfflineODataProvider() {
        return currentUserDataProvider;
    }

    public void setCurrentUserOfflineODataProvider(OfflineODataProvider o) {
        currentUserDataProvider = o;
    }

    public ESPMContainer getCurrentUserESPMContainer() {
        return currentUserESPMContainer;
    }

    public void setCurrentUserESPMContainer(ESPMContainer ec) {
        currentUserESPMContainer = ec;
    }

    public ArrayList<Customer> getCustomersListToDisplay() {
        return customersListToDisplay;
    }

    public OfflineODataProvider getSharedOfflineODataProvider() {
        return sharedDataProvider;
    }

    public void setSharedOfflineODataProvider(OfflineODataProvider o) {
        sharedDataProvider = o;
    }

    public ESPMContainer getSharedESPMContainer() {
        return sharedESPMContainer;
    }

    public void setSharedESPMContainer(ESPMContainer ec) {
        sharedESPMContainer = ec;
    }

}
