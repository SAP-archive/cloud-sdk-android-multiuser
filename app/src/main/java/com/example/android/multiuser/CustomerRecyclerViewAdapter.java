package com.example.android.multiuser;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.sap.cloud.android.odata.espmcontainer.Customer;

import java.util.List;

public class CustomerRecyclerViewAdapter extends BaseAdapter<Customer> {

    CustomerRecyclerViewAdapter(Context context, List<Customer> customerList) {
        super(context, customerList);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.rv_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Customer customer = mData.get(position);
        holder.myObjectCell.prepareDetailImageView().setVisibility(View.GONE);
        holder.myObjectCell.setPreserveDetailImageSpacing(false);
        holder.myObjectCell.setBackgroundColor(Color.WHITE);
        holder.myObjectCell.setHeadline(customer.getLastName() + ", " + customer.getFirstName() );
        holder.myObjectCell.setSubheadline(customer.getStreet() + ", " + customer.getCity() + ", " + customer.getCountry());
    }
}