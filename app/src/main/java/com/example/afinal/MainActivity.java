package com.example.afinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    DB db;
    Button btn;
    ListView listView;
    int state = 1;
    ArrayList<Sawon> sawon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DB(this, "db", null, 1);
        db.tables.setTable("sawon");
        SQLiteDatabase database = db.getWritableDatabase();
        btn = (Button) findViewById(R.id.btn_jsontosql);
        listView = (ListView) findViewById(R.id.listview);
        listView.setVisibility(View.VISIBLE);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onList(1);
            }
        });
        Button btn_desc = (Button) findViewById(R.id.btn_desc);
        btn_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onList(2);
            }
        });
        Button btn_order = (Button) findViewById(R.id.btn_salary);
        btn_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onList(3);
            }
        });
        Button btn_m = (Button) findViewById(R.id.male);
        btn_m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onList(4);
            }
        });
        Button btn_f = (Button) findViewById(R.id.female);
        btn_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onList(5);
            }
        });
    }

    public void onList(int _state) {
        JsonAsyncTask task = new JsonAsyncTask(this);

        ArrayList<Sawon> sawons = new ArrayList<>();
        try {

            JsonParser parser = new JsonParser(MainActivity.this);
            sawon = parser.onParsingJson(task.execute().get()); //asyncTask용
            Cursor cursor = null;
            switch (_state) {
                case 1:
                    cursor = db.onSearchData("sawon");
                    break;
                case 2:
                    cursor = db.onSearchDataDesc("sawon");
                    break;
                case 3:
                    cursor = db.onSearchDataOrder("sawon");
                    break;
                case 4:
                    cursor = db.onSearchDataGender("sawon", "남");
                    break;
                case 5:
                    cursor = db.onSearchDataGender("sawon", "여");
                    break;
            }

            Log.d("TAG_CURSOR", String.valueOf(cursor.getCount()));
            String id = "";
            String name = "";
            String gender = "";
            String imgUrl = "";
            String salary = "";
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                id = cursor.getString(0);
                name = cursor.getString(1);
                gender = cursor.getString(2);
                imgUrl = cursor.getString(3);
                salary = cursor.getString(4);
                sawons.add(new Sawon(id, name, gender, imgUrl, salary));
            }

            cursor.close();
        } catch (InterruptedException | JSONException e) {
            Log.d("TAG", e.getMessage());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        CustomAdapter adapter = new CustomAdapter(MainActivity.this, sawons, state);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, "아이템 클릭:"+i, Toast.LENGTH_SHORT).show();
                showPopup(sawons.get(i).getName(), Integer.parseInt(sawons.get(i).getId()), sawons.get(i).getImgUrl());

            }
        });
        Log.d("TAG", String.valueOf(listView.getCount()));
    }


    private void showPopup(String name, int id, String imgUrl) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_layout, null);

        ImageView imageView = popupView.findViewById(R.id.imageView);
        TextView nameTextView = popupView.findViewById(R.id.nameTextView);
        ImageView qrCodeImageView = popupView.findViewById(R.id.qrCodeImageView);

        nameTextView.setText(name);

        Glide.with(this)
                .load(imgUrl)
                .centerCrop()
                .into(imageView);

        Bitmap qrCode = generateQRCode(name, id);
        qrCodeImageView.setImageBitmap(qrCode);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(popupView);

    }

    private Bitmap generateQRCode(String name, int id) {
        String qrData = "Name: " + name + ", ID: " + id;

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, 200, 200);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            Bitmap qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            qrCodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            return qrCodeBitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back_music:
                if (item.isChecked()) {
                    // 체크되어 있으면 배경 음악 재생
                    stopBackgroundMusic();
                    item.setChecked(false);
                } else {
                    // 체크 해제되어 있으면 배경 음악 정지
                    playBackgroundMusic();
                    item.setChecked(true);
                }
                break;
            case R.id.rectangle:
                state = 2;
                Toast.makeText(this, "lo:"+state, Toast.LENGTH_SHORT).show();
                item.setChecked(true);
                break;
            case R.id.round_shape:
                state = 3;
                Toast.makeText(this, "lo:"+state, Toast.LENGTH_SHORT).show();
                break;
        }
        item.setChecked(true);

        return true;
    }

    private MediaPlayer mediaPlayer;

    private void playBackgroundMusic() {
        if (mediaPlayer == null) {
            try {
                mediaPlayer = new MediaPlayer();

                AssetFileDescriptor descriptor = getAssets().openFd("music.mp3");
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();

                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mediaPlayer.start();
    }

    private void stopBackgroundMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            // 음악을 정지하고 MediaPlayer 객체 해제
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
}