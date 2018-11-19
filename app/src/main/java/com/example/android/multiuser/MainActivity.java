package com.example.android.multiuser;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.cloud.android.odata.espmcontainer.Customer;
import com.sap.cloud.android.odata.espmcontainer.ESPMContainer;
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata;
import com.sap.cloud.android.odata.espmcontainer.Product;
import com.sap.cloud.mobile.foundation.authentication.OAuth2Configuration;
import com.sap.cloud.mobile.foundation.authentication.OAuth2Interceptor;
import com.sap.cloud.mobile.foundation.authentication.OAuth2WebViewProcessor;
import com.sap.cloud.mobile.foundation.common.ClientProvider;
import com.sap.cloud.mobile.foundation.common.SettingsParameters;
import com.sap.cloud.mobile.foundation.logging.Logging;
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor;
import com.sap.cloud.mobile.foundation.networking.HttpException;
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar;
import com.sap.cloud.mobile.foundation.settings.Settings;
import com.sap.cloud.mobile.foundation.user.UserInfo;
import com.sap.cloud.mobile.foundation.user.UserRoles;
import com.sap.cloud.mobile.odata.DataQuery;
import com.sap.cloud.mobile.odata.core.AndroidSystem;
import com.sap.cloud.mobile.odata.offline.OfflineODataDefiningQuery;
import com.sap.cloud.mobile.odata.offline.OfflineODataException;
import com.sap.cloud.mobile.odata.offline.OfflineODataParameters;
import com.sap.cloud.mobile.odata.offline.OfflineODataProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.android.multiuser.ChangeCustomerDetailActivity.customer;
import static com.example.android.multiuser.StorageManager.adapter;
import static com.example.android.multiuser.StorageManager.customersListToDisplay;

public class MainActivity extends AppCompatActivity implements CustomerRecyclerViewAdapter.ItemClickListener {

    private final static String USER_NUMBER = "i500828";
    private final static String OAUTH_CLIENT_ID = "c1ae10d7-1287-4b08-b00e-5199e6f5a93d";

    public final static String OAUTH_REDIRECT_URL = "https://oauthasservices-" + USER_NUMBER + "trial.hanatrial.ondemand.com";
    private final static String AUTH_END_POINT = "https://oauthasservices-" + USER_NUMBER + "trial.hanatrial.ondemand.com/oauth2/api/v1/authorize";
    private final static String TOKEN_END_POINT = "https://oauthasservices-" + USER_NUMBER + "trial.hanatrial.ondemand.com/oauth2/api/v1/token";
    public final static String serviceURL = "https://hcpms-" + USER_NUMBER + "trial.hanatrial.ondemand.com";
    public final static String appID = "com.sap.multiuser";
    public final static String connectionID = "com.sap.edm.sampleservice.v2";
    public static final String myTag = "myDebuggingTag";

    public static String currentUser;
    public static String countryCode;
    public StorageManager storageManager = StorageManager.getInstance();
    private String deviceID;
    private LinearLayout loadingSpinnerParent;
    private SettingsParameters settingsParameters;
    private MenuItem syncMenuItem;
    private MenuItem loginOrOutMenuItem;
    private MenuItem logSharedDataMenuItem;
    private MenuItem changeCountryMenuItem;
    private TextView loginTextView;
    private boolean firstOpen;
    private Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // User authentication
        deviceID = android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        Logging.ConfigurationBuilder cb = new Logging.ConfigurationBuilder();
        cb.logToConsole(true);
        Logging.initialize(this.getApplicationContext(), cb);
        Log.d(myTag, deviceID);
        OAuth2Configuration oAuth2Configuration = new OAuth2Configuration.Builder(getApplicationContext())
                .clientId(OAUTH_CLIENT_ID)
                .responseType("code")
                .authUrl(AUTH_END_POINT)
                .tokenUrl(TOKEN_END_POINT)
                .redirectUrl(OAUTH_REDIRECT_URL)
                .build();
        SAPOAuthTokenStore oauthTokenStore = SAPOAuthTokenStore.getInstance();
        try {
            settingsParameters = new SettingsParameters(serviceURL, appID, deviceID, "1.0");
        } catch (MalformedURLException e) {
            Log.d(myTag, "Error creating the settings parameters: " + e.getMessage());
        }
        OkHttpClient myOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AppHeadersInterceptor(appID, deviceID, "1.0"))
                .addInterceptor(new OAuth2Interceptor(new OAuth2WebViewProcessor(oAuth2Configuration), oauthTokenStore))
                .cookieJar(new WebkitCookieJar())
                .build();

        ClientProvider.set(myOkHttpClient);


        settings = new Settings(ClientProvider.get(), settingsParameters);

        ch.qos.logback.classic.Logger myRootLogger = Logging.getRootLogger();
        myRootLogger.setLevel(Level.ERROR);  //levels in order are all, trace, debug, info, warn, error, off
        loadingSpinnerParent = findViewById(R.id.loading_spinner_parent);
        loginTextView = findViewById(R.id.login_text);

        RecyclerView recyclerView = findViewById(R.id.rvCustomers);
        LinearLayoutManager llm = new LinearLayoutManager(this);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(llm);

        adapter = new CustomerRecyclerViewAdapter(getApplicationContext(), customersListToDisplay);
        adapter.setClickListener(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(15);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setAdapter(adapter);
        firstOpen = savedInstanceState == null;
    }

    private void toastAMessageFromBackground(String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            Toast toast = Toast.makeText(getApplicationContext(),
                    msg,
                    Toast.LENGTH_LONG);
            toast.show();
        });
    }

    /**
     * setupSharedStore populates offline stores (product and customer stores) from server
     */
    private void setupSharedStore() {
        Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.sap.cloud.mobile.odata");
        logger.setLevel(Level.ALL);
        AndroidSystem.setContext(getApplicationContext());

        try {
            URL url = new URL(serviceURL + "/" + connectionID);
            OfflineODataParameters offParam = new OfflineODataParameters();
            offParam.setStoreName("sharedStore");
            storageManager.setSharedOfflineODataProvider(new OfflineODataProvider(url, offParam, ClientProvider.get(), null, null));
            OfflineODataDefiningQuery productsQuery = new OfflineODataDefiningQuery("Products", "Products", false);
            // Customers are retrieved to find possible countries to select from
            OfflineODataDefiningQuery customersQuery = new OfflineODataDefiningQuery("Customers", "Customers", false);
            storageManager.getSharedOfflineODataProvider().addDefiningQuery(productsQuery);
            storageManager.getSharedOfflineODataProvider().addDefiningQuery(customersQuery);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(myTag, "Exception encountered setting up shared store: " + e.getMessage());
        }
        Log.d(myTag, "Opening Shared Offline Store");
        runOnUiThread(() -> {
            ((TextView) findViewById(R.id.description)).setText("Opening Shared Offline Store");
        });
        storageManager.getSharedOfflineODataProvider().open(() -> {
            Log.d(myTag, "Shared Offline Store is open");
            toastAMessageFromBackground("Shared Offline Store opened");
            getCurrentUserID();
            storageManager.setSharedESPMContainer(new ESPMContainer(storageManager.getSharedOfflineODataProvider()));
        }, (error) -> checkError(error.toString()));
    }

    private void checkError(String error) {
        if (error.contains("Unable to resolve host")) { // no wifi
            Log.d(myTag, "Shared Offline Store failed to open. Check WiFi.");
            runOnUiThread(() -> {
                findViewById(R.id.loading_spinner).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.description)).setText("Failed to open Shared Offline Store. Make sure that you have Internet connection and restart the app.");
            });
        } else if (error.contains("HTTP status code: 503")) { // bad i number
            Log.d(myTag, "Shared Offline Store failed to open. Check i-number in MainActivity.java file.");
            runOnUiThread(() -> {
                findViewById(R.id.loading_spinner).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.description)).setText("Failed to open Shared Offline Store. Make sure that you have the correct USER_NUMBER in MainActivity.java file.");
            });
        } else if (error.toString().contains("HTTP status code: 404")) {
            Log.d(myTag, "Shared Offline Store failed to open. Check Cockpit for MultiUser project.");
            runOnUiThread(() -> {
                findViewById(R.id.loading_spinner).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.description)).setText("Failed to open Shared Offline Store. Make sure that the user has MultiUser project in Cockpit.");
            });
        } else if (error.contains("unauthorized_client")) {
            Log.d(myTag, "Shared Offline Store failed to open. Check OAUTH_CLIENT_ID in MainActivity.java file.");
            runOnUiThread(() -> {
                findViewById(R.id.loading_spinner).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.description)).setText("Failed to open Shared Offline Store. Make sure that you have the correct OAUTH_CLIENT_ID in MainActivity.java file.");
            });
        } else {
            Log.d(myTag, "Shared Offline Store failed to open. Error not caught: " + error.toString());
            runOnUiThread(() -> {
                findViewById(R.id.loading_spinner).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.description)).setText("Failed to open Shared Offline Store. Error: " + error.toString());
            });
        }
    }

    /**
     * getCountryCode checks the JSON Storage for the user's country, assigning the country to the
     * user if found, otherwise displays country selection to user
     */
    private void getCountryCode() {
        settings.load(Settings.SettingTarget.USER, "countrySelection", new Settings.CallbackListener() {
            @Override
            // User was previously assigned a country
            public void onSuccess(@NonNull JSONObject jsonObject) {
                try {
                    String countryFromStorageService = jsonObject.getString(currentUser);
                    Log.d(myTag, "Retrieved the country code from the storage service: " + countryFromStorageService);
                    countryCode = countryFromStorageService;
                    setupCurrentUserOfflineStore();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(myTag, "Error while parsing JSON for country code: " + e.getMessage());
                }
            }

            @Override
            // Could not find country assigned to user
            public void onError(@NonNull Throwable result) {
                if (result instanceof HttpException) {
                    HttpException httpException = (HttpException) result;
                    if (httpException.code() == 404) {
                        Log.d(myTag, "Country code not set in the JSON Storage, user must choose a country.");
                        selectCustomerRegion();
                    } else {
                        Log.e(myTag, httpException.message() + ", with Error code: " + httpException.code());
                    }
                } else {
                    Log.e(myTag, "Error while getting country code from storage service: " + result.getMessage());
                }
            }
        });
    }

    public void onLogout() {
        Log.d(myTag, "In onLogout");
        toastAMessageFromBackground("Syncing data, please hold.");
        // Upload changes on logout
        storageManager.getCurrentUserOfflineODataProvider().upload(() -> {
            Log.d(myTag, "Successfully uploaded any changes made to customer data.");
            toastAMessageFromBackground("Successfully synced all changed data.");
            logoutLogic();
            runOnUiThread(() -> {
                loginOrOutMenuItem.setTitle("Login");
                loginOrOutMenuItem.setEnabled(true);
                changeCountryMenuItem.setVisible(false);
                logSharedDataMenuItem.setVisible(false);
                syncMenuItem.setVisible(false);
            });
        }, (error) -> {
            Log.d(myTag, "Error while uploading current user store: " + error.getMessage());
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
                    .setMessage("Sync failed. The application was unable to upload its latest changes.")
                    .setCancelable(false)
                    .setOnKeyListener(new Dialog.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                finish();
                            }
                            return true;
                        }
                    })
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            onLogout();
                        }
                    });
            runOnUiThread(() -> alert.show());
        });
    }

    public void onLogin() {
        Log.d(myTag, "In onLogin");
        setupSharedStore();
        runOnUiThread(() -> {
            loginTextView.setVisibility(View.GONE);
            loadingSpinnerParent.setVisibility(View.VISIBLE);
            loginOrOutMenuItem.setEnabled(false);
        });
    }

    /**
     * selectCustomerRegion display list of countries for the user to pick from, which is derived from
     * the customers' countries
     */
    private void selectCustomerRegion() {
        AlertDialog.Builder regionDialog = new AlertDialog.Builder(this);
        regionDialog.setTitle("Choose a Country");
        HashSet<String> countryIds = new HashSet<>();
        DataQuery customersQuery = new DataQuery().from(ESPMContainerMetadata.EntitySets.customers);
        storageManager.getSharedESPMContainer().getCustomersAsync(customersQuery, customers -> {
            for (Customer customer : customers) {
                countryIds.add(customer.getCountry());
            }
            String[] types = countryIds.toArray(new String[countryIds.size()]);
            Arrays.sort(types);
            regionDialog.setItems(types, (DialogInterface dialog, int position) -> {
                if (countryCode != types[position]) {
                    runOnUiThread(() -> {
                        loadingSpinnerParent.setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.description)).setText("Retrieving available countries");
                    });
                    countryCode = types[position];
                    storeCountry(countryCode);
                    setupCurrentUserOfflineStore();
                }
                dialog.dismiss();
            });

            regionDialog.setOnCancelListener((DialogInterface dialog) -> {
                loadingSpinnerParent.setVisibility(View.GONE);
                changeCountryMenuItem.setVisible(true);
                loginOrOutMenuItem.setEnabled(true);
                logSharedDataMenuItem.setVisible(true);
                syncMenuItem.setVisible(true);
            });

            if (countryCode == null) {
                regionDialog.setCancelable(false);
            } else {
                regionDialog.setCancelable(true);
            }

            regionDialog.show();
        }, (error) -> Log.d(myTag, "Failed getting customers for their countries with error: " + error.getMessage()));
    }

    /**
     * storeCoutnry maps the user's ID to countryCode in the Mobile Services JSON Storage
     *
     * @param countryCode country chosen by user
     */
    private void storeCountry(String countryCode) {
        // Create JSON mapping user to country
        JSONObject jsonVal;
        try {
            jsonVal = new JSONObject("{'" + currentUser + "':'" + countryCode + "'}");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(myTag, "Error while creating JSON: " + e.getMessage());
            return;
        }

        // Send JSON map to be made under countrySelection node
        settings.store(Settings.SettingTarget.USER, "countrySelection", jsonVal, new Settings.CallbackListener() {
            @Override
            public void onSuccess(@NonNull JSONObject obj) {
                try {
                    Integer responseCode = obj.getInt("code");
                    String responseMessage = obj.getString("message");
                    Log.d(myTag, responseMessage + ", code: " + responseCode);
                } catch (JSONException je) {
                    Log.e(myTag, "Error occurred getting values from JSON: " + je.getMessage());
                }
            }

            @Override
            public void onError(@NonNull Throwable result) {
                if (result instanceof HttpException) {
                    HttpException ne = (HttpException) result;
                    Log.e(myTag, ne.message() + ", with Error code: " + ne.code());
                } else {
                    Log.e(myTag, "Error occurred updating the storage service: " + result.getMessage());
                }
            }
        });
    }

    /**
     * getCurrentUserID sets currentUser to the ID of the user that is currently logged in
     */
    private void getCurrentUserID() {
        Log.d(myTag, "In getCurrentUserID");
        UserRoles roles = new UserRoles(ClientProvider.get(), settingsParameters);
        UserRoles.CallbackListener callbackListener = new UserRoles.CallbackListener() {
            @Override
            public void onSuccess(@NonNull UserInfo ui) {
                toastAMessageFromBackground("Successfully logged in.");
                Log.d(myTag, "Successfully logged in");
                Log.d(myTag, "Logged in User Id: " + ui.getId());
                currentUser = ui.getId();
                getSupportActionBar().setTitle(currentUser);
                getCountryCode();
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                Log.d(myTag, "Failed to register " + throwable.getMessage());
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
                        .setMessage("Failed to register user. Check your network connection and try again.")
                        .setCancelable(false)
                        .setOnKeyListener(new Dialog.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    finish();
                                }
                                return true;
                            }
                        })
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                setupSharedStore();
                            }
                        });
                runOnUiThread(() -> {
                    alert.show();
                });
            }
        };
        roles.load(callbackListener);
    }

    /**
     * logoutLogic resets app to before user is authenticated
     */
    private void logoutLogic() {
        CookieManager.getInstance().removeAllCookies(null);

        Request request = new Request.Builder()
                .post(RequestBody.create(null, ""))
                .url(serviceURL + "/mobileservices/sessions/logout")
                .build();

        Callback updateUICallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, final IOException e) {
                Log.d(myTag, "Log out failed: " + e.getLocalizedMessage());
                toastAMessageFromBackground("Log out failed, please check your network connection.");
            }

            @Override
            public void onResponse(@NonNull Call call, final Response response) {
                if (response.isSuccessful()) {
                    Log.d(myTag, "Successfully logged out");
                    runOnUiThread(() -> {
                        storageManager.getCustomersListToDisplay().clear();
                        adapter.notifyDataSetChanged();
                        loginTextView.setVisibility(View.VISIBLE);
                        currentUser = null;
                        getSupportActionBar().setTitle("Call Center");
                    });
                } else {
                    Log.d(myTag, "Log out failed " + response.networkResponse());
                    toastAMessageFromBackground("Log out failed " + response.networkResponse());
                }
            }
        };
        ClientProvider.get().newCall(request).enqueue(updateUICallback);
    }

    /**
     * setUpCurrentUserDatabase retrieves any notifications directed to the user, downloads and
     * displays customers from the user's country
     */
    private void setupCurrentUserOfflineStore() {
        try {
            URL url = new URL(serviceURL + "/" + connectionID);
            OfflineODataParameters offParam = new OfflineODataParameters();
            offParam.setStoreName(currentUser);

            // Reset store
            if (storageManager.getCurrentUserOfflineODataProvider() != null) {
                storageManager.getCurrentUserOfflineODataProvider().close();
            }
            // Filter customers on country
            storageManager.setCurrentUserOfflineODataProvider(new OfflineODataProvider(url, offParam, ClientProvider.get(), null, null));
            String query = "Customers?$filter=Country eq '" + countryCode + "'";
            Log.d(myTag, "The defining query is: " + query);
            OfflineODataDefiningQuery definingQuery = new OfflineODataDefiningQuery("CustomersInRegion", query, false);
            storageManager.getCurrentUserOfflineODataProvider().addDefiningQuery(definingQuery);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(myTag, "Exception encountered setting up current user store: " + e.getMessage());
        }
        Log.d(myTag, "Current user offline store is now: " + storageManager.getCurrentUserOfflineODataProvider().getServiceName());

        // Retrieve and display customers to the user
        runOnUiThread(() -> ((TextView) findViewById(R.id.description)).setText("Opening " + currentUser + "'s Offline Store"));
        storageManager.getCurrentUserOfflineODataProvider().open(() -> {
            Log.d(myTag, "Current user offline store is open");
            // Store every users' offline store
            runOnUiThread(() -> ((TextView) findViewById(R.id.description)).setText("Downloading latest changes."));
            storageManager.getCurrentUserOfflineODataProvider().download(() -> {
                storageManager.setCurrentUserESPMContainer(new ESPMContainer(storageManager.getCurrentUserOfflineODataProvider()));
                if (currentUser != null) {
                    runOnUiThread(() -> initCustomerList());
                }
            }, (error) -> {
                Log.d(myTag, "Current user offline store failed to download");
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
                        .setMessage("Failed to download Current User's offline store. Ensure that you have working Internet connection and try again.")
                        .setCancelable(false)
                        .setOnKeyListener(new Dialog.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    finish();
                                }
                                return true;
                            }
                        })
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                setupCurrentUserOfflineStore();
                            }
                        });
                runOnUiThread(() -> {
                    alert.show();
                });
            });
        }, (error) -> {
            Log.d(myTag, "Current user offline store failed to open");
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
                    .setMessage("Current user offline store failed to open. Ensure that you have working Internet connection and try again.")
                    .setCancelable(false)
                    .setOnKeyListener(new Dialog.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                finish();
                            }
                            return true;
                        }
                    })
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            setupCurrentUserOfflineStore();
                        }
                    });

            runOnUiThread(() -> {
                alert.show();
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adds options to the menu
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        logSharedDataMenuItem = menu.findItem(R.id.action_query_shared);
        loginOrOutMenuItem = menu.findItem(R.id.action_register);
        changeCountryMenuItem = menu.findItem(R.id.action_change_country);
        syncMenuItem = menu.findItem(R.id.action_sync);

        if (firstOpen) {
            onLogin();
        } else if (storageManager.getCurrentUserOfflineODataProvider() != null) {
            initCustomerList();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Do action based on menu item selected
        if (id == R.id.action_register) { // Login/Logout
            runOnUiThread(() -> {
                changeCountryMenuItem.setVisible(false);
                logSharedDataMenuItem.setVisible(false);
                syncMenuItem.setVisible(false);
                loginOrOutMenuItem.setEnabled(false);
            });
            if (currentUser == null) {
                onLogin();
            } else {
                onLogout();
            }
        } else if (id == R.id.action_change_country) { // Change Country
            if (currentUser != null) {
                runOnUiThread(() -> {
                    loadingSpinnerParent.setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.description)).setText("Syncing Changes.");
                    changeCountryMenuItem.setVisible(false);
                    logSharedDataMenuItem.setVisible(false);
                    syncMenuItem.setVisible(false);
                    loginOrOutMenuItem.setEnabled(false);
                });
                storageManager.getCurrentUserOfflineODataProvider().upload(() -> {
                    Log.d(myTag, "Successfully uploaded current user store.");
                    selectCustomerRegion();
                }, (error) -> {
                    Log.d(myTag, "Error while uploading current user store: " + error.getMessage());
                    android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
                            .setMessage("Sync failed. The application was unable to upload its latest changes.")
                            .setCancelable(false)
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    onOptionsItemSelected(item);
                                }
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                runOnUiThread(() -> {
                                    loadingSpinnerParent.setVisibility(View.GONE);
                                    changeCountryMenuItem.setVisible(true);
                                    logSharedDataMenuItem.setVisible(true);
                                    syncMenuItem.setVisible(true);
                                    loginOrOutMenuItem.setEnabled(true);
                                    initCustomerList();
                                });
                            });
                    runOnUiThread(() -> {
                        alert.show();
                    });
                });
            } else {
                toastAMessageFromBackground("You need to be logged in to change countries.");
            }
            return true;
        } else if (id == R.id.action_query_shared) { // Log Shared Data
            if (storageManager != null) {
                querySharedStore();
            } else {
                toastAMessageFromBackground("The shared store is not open.");
            }
            return true;
        } else if (id == R.id.action_sync) { // Sync data
            runOnUiThread(() -> {
                loadingSpinnerParent.setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.description)).setText("Syncing Changes.");
                changeCountryMenuItem.setVisible(false);
                logSharedDataMenuItem.setVisible(false);
                syncMenuItem.setVisible(false);
                loginOrOutMenuItem.setEnabled(false);
            });
            toastAMessageFromBackground("Uploading current user store.");
            storageManager.getCurrentUserOfflineODataProvider().upload(() -> {
                Log.d(myTag, "Successfully uploaded current user store.");
                toastAMessageFromBackground("Successfully uploaded store, downloading any changes...");
                storageManager.getCurrentUserOfflineODataProvider().download(() -> {
                    Log.d(myTag, "Successfully downloaded current user store.");
                    toastAMessageFromBackground("Successfully downloaded store.");
                    storageManager.setCurrentUserESPMContainer(new ESPMContainer(storageManager.getCurrentUserOfflineODataProvider()));
                    runOnUiThread(() -> {
                        loadingSpinnerParent.setVisibility(View.GONE);
                        changeCountryMenuItem.setVisible(true);
                        logSharedDataMenuItem.setVisible(true);
                        syncMenuItem.setVisible(true);
                        loginOrOutMenuItem.setEnabled(true);
                        initCustomerList();
                    });
                }, (error) -> {
                    Log.d(myTag, "Current user offline store failed to download");
                    android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
                            .setMessage("Sync failed. The application was unable to upload its latest changes.")
                            .setCancelable(false)
                            .setOnKeyListener(new Dialog.OnKeyListener() {
                                @Override
                                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                                        finish();
                                    }
                                    return true;
                                }
                            })
                            .setPositiveButton("Retry", (dialog, which) -> onOptionsItemSelected(item))
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                runOnUiThread(() -> {
                                    loadingSpinnerParent.setVisibility(View.GONE);
                                    changeCountryMenuItem.setVisible(true);
                                    logSharedDataMenuItem.setVisible(true);
                                    syncMenuItem.setVisible(true);
                                    loginOrOutMenuItem.setEnabled(true);
                                    initCustomerList();
                                });
                            });
                    runOnUiThread(() -> {
                        alert.show();
                    });
                });
            }, (error) -> {
                Log.d(myTag, "Error while uploading current user store: " + error.getMessage());
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth)
                        .setMessage("Sync failed. The application was unable to upload its latest changes.")
                        .setCancelable(false)
                        .setOnKeyListener(new Dialog.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    finish();
                                }
                                return true;
                            }
                        })
                        .setPositiveButton("Retry", (dialog, which) -> onOptionsItemSelected(item))
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            runOnUiThread(() -> {
                                loadingSpinnerParent.setVisibility(View.GONE);
                                changeCountryMenuItem.setVisible(true);
                                logSharedDataMenuItem.setVisible(true);
                                syncMenuItem.setVisible(true);
                                loginOrOutMenuItem.setEnabled(true);
                                initCustomerList();
                            });
                        });
                runOnUiThread(() -> {
                    alert.show();
                });
            });
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * querySharedStore prints products in store
     */
    private void querySharedStore() {
        List<Product> products = storageManager.getSharedESPMContainer().getProducts();
        Log.d(myTag, "Found these products:");
        for (Product product : products) {
            Log.d(myTag, product.getName());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onResume();
    }

    /**
     * initCustomerList retrieves and displays customers based on user's country
     */
    private void initCustomerList() {
        storageManager.getCustomersListToDisplay().clear();
        DataQuery customersQuery = new DataQuery()
                .orderBy(Customer.lastName);
        storageManager.getCurrentUserESPMContainer().getCustomersAsync(customersQuery, (customers) -> {
            if (!customers.get(0).getCountry().equals(countryCode)) {
                try {
                    Log.d(myTag, "Current user offline store outdated. Downloading latest copy");
                    toastAMessageFromBackground("Current user offline store outdated. Downloading latest copy");
                    storageManager.getCurrentUserOfflineODataProvider().clear();
                    setupCurrentUserOfflineStore();
                } catch (OfflineODataException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(myTag, "Current user offline store is open");
                toastAMessageFromBackground("Current user offline store opened");
                Log.d(myTag, "Customer list populated for country: " + customers.get(0).getCountry());
                for (Customer customer : customers) {
                    storageManager.getCustomersListToDisplay().add(customer);
                }
                adapter.notifyDataSetChanged();
                loadingSpinnerParent.setVisibility(View.GONE);
                loginTextView.setVisibility(View.GONE);
                runOnUiThread(() -> {
                    changeCountryMenuItem.setVisible(true);
                    logSharedDataMenuItem.setVisible(true);
                    syncMenuItem.setVisible(true);
                    loginOrOutMenuItem.setEnabled(true);
                    loginOrOutMenuItem.setTitle("Logout");
                });
            }
        }, (error) -> Log.d(myTag, "Error getting customers: " + error.getMessage()));
    }

    @Override
    public void onItemClick(View view, int position) {
        // Edit customer
        customer = storageManager.getCustomersListToDisplay().get(position);
        Intent i = new Intent(this, ChangeCustomerDetailActivity.class);
        startActivity(i);
    }
}
