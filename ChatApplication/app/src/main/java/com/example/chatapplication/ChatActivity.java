package com.example.chatapplication;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.adapter.ChatAdapter;
import com.example.chatapplication.model.ChatMessage;
import com.example.chatapplication.service.ChatService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


public class ChatActivity extends AppCompatActivity {
    private ChatAdapter adapter;
    private ChatService api;

    private Handler typingHandler = new Handler();
    private Runnable typingRunnable;

    private TextView typingDots;

    private RecyclerView recyclerView;
    private int dotCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.chatRecyclerView);
        EditText messageInput = findViewById(R.id.messageInput);
        ImageButton sendButton = findViewById(R.id.sendButton);
        typingDots = findViewById(R.id.typingDots);

        adapter = new ChatAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5050/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        api = retrofit.create(ChatService.class);

        String username = getIntent().getStringExtra("username");
        adapter.addMessage(new ChatMessage("Welcome " + username + "!", false));
        scrollToBottom();

        sendButton.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                adapter.addMessage(new ChatMessage(msg, true));
                messageInput.setText("");
                sendMessage(msg);
            }
        });
    }
    private void sendMessage(String message) {
        typingDots.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        typingDots.startAnimation(fadeIn);


        // Start the dot animation
        typingRunnable = new Runnable() {
            @Override
            public void run() {
                dotCount = (dotCount + 1) % 4;
                String dots = new String(new char[dotCount]).replace("\0", ".");
                typingDots.setText("" + dots);
                typingHandler.postDelayed(this, 500);
            }
        };
        typingHandler.post(typingRunnable);
        Call<String> call = api.sendMessage(message);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String reply = response.body() != null ? response.body().trim() : "Sorry, no response.";
                typingHandler.removeCallbacks(typingRunnable);
                Animation fadeOut = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.fade_out);
                typingDots.startAnimation(fadeOut);
                typingDots.setVisibility(View.GONE);
                adapter.addMessage(new ChatMessage(reply, false));
                scrollToBottom();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                typingHandler.removeCallbacks(typingRunnable);
                Animation fadeOut = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.fade_out);
                typingDots.startAnimation(fadeOut);
                typingDots.setVisibility(View.GONE);
                adapter.addMessage(new ChatMessage("Failed to connect.", false));
                scrollToBottom();
            }
        });
    }
    private void scrollToBottom() {
        recyclerView.post(() ->
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1)
        );
    }
}