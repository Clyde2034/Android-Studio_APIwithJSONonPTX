package com.clyde.apiwithjson;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recycle_View;
    private Cardview_adapter cardview_adapter;
    private ArrayList<HashMap<String, String>> receiveDATA = new ArrayList<>();//解析後的資料 by asd66998854
    private Handler mainHandler = new Handler();//主線程 by asd66998854

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycle_View = findViewById(R.id.recycle_View);
        GET_DATA();
    }

    private class Cardview_adapter extends RecyclerView.Adapter<Cardview_adapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView cardview_item_photo;
            private TextView cardview_item_name, cardview_item_address;
            private View mView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardview_item_photo = itemView.findViewById(R.id.cardview_item_photo);
                cardview_item_name = itemView.findViewById(R.id.cardview_item_name);
                cardview_item_address = itemView.findViewById(R.id.cardview_item_address);
                mView = itemView;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.cardview_item_name.setText("");
            holder.cardview_item_address.setText("");
            holder.cardview_item_name.setText("名稱：" + receiveDATA.get(holder.getAdapterPosition()).get("Name"));
            holder.cardview_item_address.setText("地址：" + receiveDATA.get(holder.getAdapterPosition()).get("Address"));
            new DownloadImage(holder.cardview_item_photo, receiveDATA.get(holder.getAdapterPosition()).get("PictureUrl1")).start();

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this, "第" + (holder.getAdapterPosition() + 1) + "個", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return receiveDATA.size();
        }
    }

    private void GET_DATA() {
        String APP_ID = "f0e004b33e2f4349916c2c09064323df";//Your APP_ID on PTX platform
        String APP_KEY = "v0_BS-lG5yeP5dKsrDjLcLHkOqI";//Your APP_KEY on PTX platform
        String Signature = "";

        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        Date now_time_unformat = new Date();
        String now_time_format = formatter.format(now_time_unformat);

        try {
            Signature = HMAC_SHA1.Signature("x-date: ", APP_KEY);
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        String URL = "https://ptx.transportdata.tw/MOTC/v2/Tourism/Restaurant?%24top=30&%24format=JSON";

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)).build();

        String sAuth = "hmac username=\"" + APP_ID + "\", algorithm=\"hmac-sha1\", headers=\"x-date\", signature=\""
                + Signature + "\"";

        Request request = new Request.Builder()
                .url(URL)
                .addHeader("Authorization", "")
                .addHeader("x-date", now_time_format)
                .addHeader("User-Agent:", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Failed to Fetch DATA from PTX", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                receiveDATA.clear();
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("Name", jsonObject.getString("RestaurantName"));
                        hashMap.put("Address", jsonObject.getString("Address"));
                        Log.d("Test", jsonObject.getString("Address"));
                        Log.d("Size", String.valueOf(jsonArray.length()));
                        hashMap.put("PictureUrl1", jsonObject.getJSONObject("Picture").getString("PictureUrl1"));
                        receiveDATA.add(hashMap);
                    }

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            recycle_View.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            cardview_adapter = new Cardview_adapter();
                            recycle_View.setAdapter(cardview_adapter);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    class DownloadImage extends Thread {
        ImageView imageView;
        String url;

        private DownloadImage(ImageView imageView, String url) {
            this.imageView = imageView;
            this.url = url;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = new URL(url).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.getLayoutParams().width = 280;
                        imageView.getLayoutParams().height = 280;
                        imageView.setImageBitmap(bitmap);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}