# Android-Studio-API-with-JSON
This project is fetch data from Taiwan PTX with API，after that using JSON parser the data and format to recyclevie.

> Deprecated API after 2022/12/31

>API Require
>
>>okhttp  
>>PTX html verification:https://motc-ptx-api-documentation.gitbook.io/motc-ptx-api-documentation/api-shi-yong/hmac

>UI and Java Code
>>UI：activity_main.xml、cardview_item.xml  
>>Java：MainActivity.java、HMAC_SHA1.java（used to encode the signature）

<img  src="https://user-images.githubusercontent.com/41913354/158035767-49a52188-7797-4d0c-936a-28e758701015.png" width="250"/>

## AndroidManifest.xml
```
    //...
    <uses-permission android:name="android.permission.INTERNET"/>
    //...
-------------------------------------------------------------------------------------------
dependencies {
    //...
    implementation 'com.squareup.okhttp3:okhttp:4.7.2'//okhttp
    implementation 'com.squareup.okhttp3:okhttp-urlconnection:4.7.2'//log connenting status
    implementation 'com.squareup.okhttp3:logging-interceptor:4.7.2'//log connenting status
    //...
}  
```

## activity_main.xml
```
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycle_View"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```

## MainActivity.java
```
public class MainActivity extends AppCompatActivity {
    private RecyclerView recycle_View;
    private Cardview_adapter cardview_adapter;
    private ArrayList<HashMap<String, String>> receiveDATA = new ArrayList<>();
    private Handler mainHandler = new Handler();

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

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String now_time_format = dateFormat.format(calendar.getTime());

        try {
            Signature = HMAC_SHA1.Signature("x-date: " + now_time_format, APP_KEY);
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        String URL = "https://ptx.transportdata.tw/MOTC/v2/Tourism/Restaurant?%24top=30&%24format=JSON";

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)).build();

        String sAuth = "hmac username=\"" + APP_ID + "\", algorithm=\"hmac-sha1\", headers=\"x-date\", signature=\"" + Signature + "\"";

        Request request = new Request.Builder()
                .url(URL)
                .addHeader("Authorization", sAuth)
                .addHeader("x-date", now_time_format)
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
```

## cardview_item.xml
```
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="20dp"
    android:layout_marginTop="15dp"
    app:cardCornerRadius="20dp"
    app:cardElevation="10dp">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <ImageView
            android:id="@+id/cardview_item_photo"
            android:layout_width="130dp"
            android:layout_height="130dp"
            android:layout_margin="10dp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:srcCompat="@mipmap/ic_launcher_round" />


        <TextView
            android:id="@+id/cardview_item_name"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="15dp"
            android:text="Name："
            app:layout_constraintBottom_toTopOf="@id/cardview_item_address"
            app:layout_constraintStart_toEndOf="@id/cardview_item_photo"
            app:layout_constraintTop_toTopOf="@id/cardview_item_photo" />

        <TextView
            android:id="@+id/cardview_item_address"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Address："
            app:layout_constraintBottom_toBottomOf="@id/cardview_item_photo"
            app:layout_constraintStart_toStartOf="@id/cardview_item_name"
            app:layout_constraintTop_toBottomOf="@id/cardview_item_name" />
    </androidx.constraintlayout.widget.ConstraintLayout>

</androidx.cardview.widget.CardView>
```

