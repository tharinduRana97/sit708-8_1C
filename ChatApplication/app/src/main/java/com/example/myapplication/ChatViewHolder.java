package com.example.myapplication;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.ChatMessage;

public class ChatViewHolder extends RecyclerView.ViewHolder {
    private TextView messageText;

    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.messageText);
    }

    public void bind(ChatMessage msg) {
        messageText.setText(msg.getMessage());
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) messageText.getLayoutParams();
        params.gravity = msg.isUser() ? Gravity.END : Gravity.START;
        messageText.setLayoutParams(params);
    }
}