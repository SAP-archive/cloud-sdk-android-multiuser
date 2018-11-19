package com.example.android.multiuser;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.sap.cloud.android.odata.espmcontainer.Customer;
import com.sap.cloud.mobile.fiori.contact.ProfileHeader;

import static com.example.android.multiuser.MainActivity.myTag;
import static com.example.android.multiuser.StorageManager.adapter;

public class ChangeCustomerDetailActivity extends AppCompatActivity {

    public static final String TAG = "myDebuggingTag";

    EditText houseNumber;
    EditText street;
    EditText postalCode;
    EditText city;
    EditText phone;


    StorageManager storageManager;
    static Customer customer;

    private boolean streetChanged = false;
    private boolean postalCodeChanged = false;
    private boolean phoneChanged = false;
    private boolean cityChanged = false;
    private boolean houseNumChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_customer_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        setTitle("Edit Customer");
        setProfileHeader();
        storageManager = StorageManager.getInstance();
        street = findViewById(R.id.et_street);
        postalCode = findViewById(R.id.et_postal_code);
        phone = findViewById(R.id.et_phone);
        city = findViewById(R.id.et_city);
        houseNumber = findViewById(R.id.et_house_num);
        street.setText(customer.getStreet());
        phone.setText(customer.getPhoneNumber());
        postalCode.setText(customer.getPostalCode());
        city.setText(customer.getCity());
        houseNumber.setText(customer.getHouseNumber());
        street.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                streetChanged = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
        postalCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                postalCodeChanged = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                phoneChanged = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
        city.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                cityChanged = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
        houseNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                houseNumChanged = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
    }


    private void setProfileHeader() {
        ProfileHeader customerHeader = findViewById(R.id.profile_header);
        customerHeader.setHeadline(customer.getFirstName() + " " + customer.getLastName());
        customerHeader.setDetailImage(R.drawable.ic_account_circle_white_24dp);
        customerHeader.setSubheadline(customer.getEmailAddress());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.save_item:
                Log.d(myTag, "Save pressed");
                if (streetChanged) {
                    customer.setStreet(street.getText().toString());
                    adapter.notifyDataSetChanged();
                }

                if (cityChanged) {
                    customer.setCity(city.getText().toString());
                    adapter.notifyDataSetChanged();
                }

                if (phoneChanged) {
                    customer.setPhoneNumber(phone.getText().toString());
                    adapter.notifyDataSetChanged();
                }

                if (postalCodeChanged) {
                    customer.setPostalCode(postalCode.getText().toString());
                    adapter.notifyDataSetChanged();
                }

                if (houseNumChanged) {
                    customer.setHouseNumber(houseNumber.getText().toString());
                    adapter.notifyDataSetChanged();
                }

                if (streetChanged || cityChanged || phoneChanged || postalCodeChanged || houseNumChanged) {
                    storageManager.getCurrentUserESPMContainer().updateEntityAsync(customer, () ->
                                    Log.d(TAG, "Successfully updated customer info")
                            , (error) ->
                                    Log.d(TAG, "Error updating customer: " + error.getMessage())
                    );
                    Toast toast = Toast.makeText(this, "Successfully updated the customer.", Toast.LENGTH_LONG);
                    toast.show();
                    onBackPressed();
                }
                else {
                    Toast.makeText(this, "No properties were changed.", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.customer_menu, menu);
        return true;
    }
}
