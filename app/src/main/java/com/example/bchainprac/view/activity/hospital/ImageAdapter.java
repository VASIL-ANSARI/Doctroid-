package com.example.bchainprac.view.activity.hospital;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bchainprac.R;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageHolder> {
    private List<String> img_links;
    private Context context;
    private LayoutInflater inflater;

    public static class ImageHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        public ImageHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.ImgViewId);
        }


    }

    public ImageAdapter(Context context,List<String> img_links) {
        this.context=context;
        this.img_links=img_links;
        inflater=LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View img_view=inflater.inflate(R.layout.image_layout,parent,false);
        return new ImageHolder(img_view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
        try {
            Picasso.with(context).load(img_links.get(position)).into(holder.image);
        }
        catch(Exception e) {
            Toast.makeText(context,"Error occurred: "+e.getClass(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return img_links.size();
    }
}
