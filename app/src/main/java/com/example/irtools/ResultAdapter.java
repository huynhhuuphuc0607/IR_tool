package com.example.irtools;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
    public static final int KEY_1 = 1;
    public static final int KEY_2 = 2;
    private Context c;
    private int linearResID;
    private int gridResID;
    private ArrayList<Result> results;
    private boolean linearNow;
    //declare interface
    private OnItemClicked onClick;

    //make interface like this
    public interface OnItemClicked {
        void onItemClick(View v, int position);
    }

    public void setOnClick(OnItemClicked onClick) {
        this.onClick = onClick;
    }

    public ResultAdapter(Context c, ArrayList<Result> results, boolean linearNow) {
        this.c = c;
        this.results = results;
        this.linearNow = linearNow;
        this.linearResID = R.layout.one_linear_result_item;
        this.gridResID = R.layout.one_grid_result_item;
    }

    @NonNull
    @Override
    public ResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        if (linearNow)
            v = LayoutInflater.from(c).inflate(linearResID, parent, false);
        else
            v = LayoutInflater.from(c).inflate(gridResID, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResultAdapter.ViewHolder holder, int position) {
        TextView positionTextView = holder.positionTextView;
        TextView titleTextView = holder.titleTextView;
        TextView subtitleTextView = holder.subtitleTextView;
        TextView tfidfTextView = holder.tfidfTextView;
        CardView lCardView = holder.lCardView;
        CardView gCardView = holder.gCardView;

        Result res = results.get(position);
        positionTextView.setText(position + "");

        titleTextView.setText(res.getUrl());
        subtitleTextView.setText("Doc id: " + res.getDocId());
        tfidfTextView.setText(String.format("%.2f", res.getTf_idf()));

        if (linearNow) {
            Log.d("IRtool",res.getUrl() + "|" + positionTextView.getId() + "|" + tfidfTextView.getId());
            lCardView.setTag(res.getUrl() + "|" + res.getTf_idf()+"|" + positionTextView.getId() + "|" + tfidfTextView.getId());
            lCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClick.onItemClick(v,position);
                }
            });
        } else {
            gCardView.setTag(res.getUrl() + "|" + res.getTf_idf()+"|" + positionTextView.getId() + "|" + tfidfTextView.getId());
            gCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClick.onItemClick(v,position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView positionTextView;
        public TextView titleTextView;
        public TextView subtitleTextView;
        public TextView tfidfTextView;
        public CardView lCardView;
        public CardView gCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            positionTextView = itemView.findViewById(R.id.positionTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            subtitleTextView = itemView.findViewById(R.id.subtitleTextView);
            tfidfTextView = itemView.findViewById(R.id.tfidfTextView);
            if (linearNow)
                lCardView = itemView.findViewById(R.id.lCardView);
            else
                gCardView = itemView.findViewById(R.id.gCardView);
        }
    }

    public void clear() {
        this.results.clear();
    }

    public void switchLayout() {
        this.linearNow = !linearNow;
    }


}
