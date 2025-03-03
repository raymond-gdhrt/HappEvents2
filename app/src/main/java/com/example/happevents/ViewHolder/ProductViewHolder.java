package com.example.happevents.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.happevents.Interface.ItemClickListener;
import com.example.happevents.R;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView txtproductName,txtProductDescription,txtproductPrice;
    public ImageView imageView;
    public ItemClickListener listener;
    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.product_image);
        txtProductDescription=(TextView) itemView.findViewById(R.id.product_description);
        txtproductName=(TextView) itemView.findViewById(R.id.product_name);
        txtproductPrice = (TextView) itemView.findViewById(R.id.product_price);

    }

    public void setItemClickListener(ItemClickListener listener){
        this.listener=listener;
    }
    @Override
    public void onClick(View view) {

        listener.onClick(view,getAdapterPosition(),false);

    }
}
