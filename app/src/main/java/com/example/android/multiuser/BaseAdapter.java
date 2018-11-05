package com.example.android.multiuser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.cloud.mobile.fiori.object.ObjectCell;


import java.util.List;

// Generically typed class to handle the common portions of the adapters
public class BaseAdapter<T> extends RecyclerView.Adapter<BaseAdapter<T>.ViewHolder> {

    protected List<T> mData;
    protected LayoutInflater mInflater;
    protected ItemClickListener mClickListener;

    // data is passed into the constructor
    BaseAdapter(Context context, List<T> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    BaseAdapter() {
        mData = null;
        mInflater = null;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new View(mInflater.getContext()));
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(BaseAdapter<T>.ViewHolder holder, int position) {

    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ObjectCell myObjectCell;

        ViewHolder(View itemView) {
            super(itemView);
            myObjectCell = itemView.findViewById(R.id.objectCell);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // allows click events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // convenience method for getting data at click position
    T getItem(int id) {
        return mData.get(id);
    }

    public void reset(List data) {
        this.mData = data;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }
}