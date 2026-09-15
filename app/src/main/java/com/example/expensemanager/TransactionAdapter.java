package com.example.expensemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TransactionAdapter
        extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private ArrayList<TransactionModel> transactionList;

    public TransactionAdapter(ArrayList<TransactionModel> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        TransactionModel transaction =
                transactionList.get(position);

        holder.txtCategory.setText(
                transaction.getCategory()
        );

        holder.txtNote.setText(
                transaction.getNote()
        );

        holder.txtDetails.setText(
                transaction.getDate()
                        + " • "
                        + transaction.getPaymentMethod()
        );

        holder.txtAmount.setText(
                transaction.getAmountText()
        );

        if (transaction.getType().equals("income")) {

            holder.txtIcon.setText("💰");

        } else {

            holder.txtIcon.setText("💸");
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtIcon;
        TextView txtCategory;
        TextView txtNote;
        TextView txtDetails;
        TextView txtAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtIcon =
                    itemView.findViewById(
                            R.id.txtTransactionIcon
                    );

            txtCategory =
                    itemView.findViewById(
                            R.id.txtTransactionCategory
                    );

            txtNote =
                    itemView.findViewById(
                            R.id.txtTransactionNote
                    );

            txtDetails =
                    itemView.findViewById(
                            R.id.txtTransactionDetails
                    );

            txtAmount =
                    itemView.findViewById(
                            R.id.txtTransactionAmount
                    );
        }
    }
}