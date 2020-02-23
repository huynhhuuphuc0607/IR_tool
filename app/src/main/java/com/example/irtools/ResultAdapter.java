package com.example.irtools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private Context c;
    private int resID;
    private ArrayList<Result> results;

    public ResultAdapter(Context c, int resID, ArrayList<Result> results) {
        this.c = c;
        this.resID = resID;
        this.results = results;
    }

    @NonNull
    @Override
    public ResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(resID, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResultAdapter.ViewHolder holder, int position) {
        TextView positionTextView = holder.positionTextView;
        TextView titleTextView = holder.titleTextView;
        TextView subtitleTextView = holder.subtitleTextView;
        TextView tfidfTextView = holder.tfidfTextView;

        Result res = results.get(position);
        positionTextView.setText(position + "");

        titleTextView.setText(res.getUrl());
        subtitleTextView.setText("Doc id: " + res.getDocId());
        tfidfTextView.setText(String.format("%.2f", res.getTf_idf()));
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            positionTextView = itemView.findViewById(R.id.positionTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            subtitleTextView = itemView.findViewById(R.id.subtitleTextView);
            tfidfTextView = itemView.findViewById(R.id.tfidfTextView);
        }
    }
}
