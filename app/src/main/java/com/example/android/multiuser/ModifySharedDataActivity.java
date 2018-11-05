package com.example.android.multiuser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.sap.cloud.android.odata.espmcontainer.Customer;
import com.sap.cloud.mobile.odata.DataQuery;

import static com.example.android.multiuser.ChangeCustomerDetailActivity.customer;
import static com.example.android.multiuser.StorageManager.adapter;
import static com.example.android.multiuser.StorageManager.customersListToDisplay;

public class ModifySharedDataActivity extends AppCompatActivity implements CustomerRecyclerViewAdapter.ItemClickListener {
    StorageManager storageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_shared_data);
        storageManager = StorageManager.getInstance();
        setTitle("Choose a Customer to Change");

        RecyclerView recyclerView = findViewById(R.id.rvCustomers);
        LinearLayoutManager llm = new LinearLayoutManager(this);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(llm);

        adapter = new CustomerRecyclerViewAdapter(getApplicationContext(), customersListToDisplay);
        adapter.setClickListener(this);

        initCustomerList();

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(15);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setAdapter(adapter);
    }

    void initCustomerList(){
        storageManager.getCustomersListToDisplay().clear();
        DataQuery customersQuery = new DataQuery()
                .orderBy(Customer.lastName);
        storageManager.getCurrentUserESPMContainer().getCustomersAsync(customersQuery, (customers) -> {
            for (Customer customer : customers) {
                storageManager.getCustomersListToDisplay().add(customer);
            }
            adapter.notifyDataSetChanged();
        }, (error) -> Log.d("myDebuggingTag", "Error getting customers: " + error.getMessage()));
    }

    @Override
    public void onItemClick(View view, int position) {
        customer = storageManager.getCustomersListToDisplay().get(position);
        Intent i = new Intent(this, ChangeCustomerDetailActivity.class);
        startActivity(i);
    }
}
