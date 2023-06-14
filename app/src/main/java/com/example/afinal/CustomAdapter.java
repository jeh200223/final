package com.example.afinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Sawon> {
    private Context context;
    private ArrayList<Sawon> sawon;
    private int state = 1;

    public CustomAdapter(@NonNull Context context, ArrayList<Sawon> sawon, int state) {
        super(context, R.layout.item, sawon);
        this.context = context;
        this.sawon = sawon;
        this.state = state;
    }

    @Nullable
    @Override
    public Sawon getItem(int position) {
        return sawon.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        Sawon sawon = this.sawon.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);

            if (state == 2 || state == 1) {
                convertView = inflater.inflate(R.layout.item, parent, false);
            } else if (state == 3) {
                convertView = inflater.inflate(R.layout.item_round, parent, false);
            }

            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.id = convertView.findViewById(R.id.idTextView);
            holder.name = convertView.findViewById(R.id.nameTextView);
            holder.sex = convertView.findViewById(R.id.sexTextView);
            holder.salary = convertView.findViewById(R.id.salaryTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.id.setText("사번 : " + sawon.getId());
        holder.name.setText("이름 : " + sawon.getName());
        holder.sex.setText("성별 : " + sawon.getGender());
        holder.salary.setText("급여 : " + sawon.getSalary());

        Glide.with(context)
                .load(sawon.getImgUrl())
                .centerCrop()
                .into(holder.imageView);

        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView id;
        TextView name;
        TextView sex;
        TextView salary;
    }
}
