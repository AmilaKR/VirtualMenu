package com.amila.dev.virtualmenu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Adaptor extends RecyclerView.Adapter<Adaptor.MyViewHolder>{

    Common m;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ListItem item = listItems.get(position % listItems.size());

        if(item.getName() == "null"){
            holder.name.setText(" ");
        }else {
            holder.name.setText(item.getName());
        }

        holder.price.setText(item.getPrice());
    }

    @Override
    public int getItemCount() {
        m = Common.getCommon();
        int mul;
        if(m.isAutoScroll()){
            mul = 5;
        }else{
            mul = 1;
        }
        return listItems == null ? 0 : listItems.size() * mul;
    }

    ArrayList<ListItem> listItems;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name;
        public TextView price;
        public MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.text_name);
            price = v.findViewById(R.id.text_price);
        }
    }

    public Adaptor(ArrayList<ListItem> listItems) {
        this.listItems = listItems;
    }
}
