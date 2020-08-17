package com.glofora.whatstustoolbox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.glofora.whatstustoolbox.R;
import com.glofora.whatstustoolbox.models.Card;

import java.util.List;

public class MainCardsAdapter extends RecyclerView.Adapter<MainCardsAdapter.MyViewHolder> {

    private static final String TAG = MainCardsAdapter.class.getSimpleName();
    private List<Card> cards;
    private Context context;
    private final OnItemClickListener itemClickListener;

    public MainCardsAdapter(List<Card> cards, OnItemClickListener itemClickListener) {
        this.cards=cards;
        this.itemClickListener = itemClickListener;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        Card card=cards.get(position);
        holder.bind(card,itemClickListener);
        holder.text.setText(card.getText());
        holder.imageView.setImageResource(card.getL_image());
        holder.smallImage.setImageResource(card.getImage());
        holder.layout.setBackgroundColor(context.getResources().getColor(card.getColor()));

    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        FrameLayout layout;
        ImageView imageView,smallImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            text=itemView.findViewById(R.id.text);
            layout=itemView.findViewById(R.id.layout);
            imageView=itemView.findViewById(R.id.imageView);
            smallImage = itemView.findViewById(R.id.smallImage);

        }

        public void bind(final Card card, final OnItemClickListener itemClickListener) {

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(card);
                }
            });

        }
    }

    public interface OnItemClickListener{
        void onItemClick(Card cardItem);
    }

}



