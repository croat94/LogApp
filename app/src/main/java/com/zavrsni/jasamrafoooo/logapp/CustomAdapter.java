package com.zavrsni.jasamrafoooo.logapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

public class CustomAdapter extends ArrayAdapter<Ocitanje>{

    protected Context mContext;
    DBHelper mydb;
    int i;
    int brojRedova;

    //constructor
    public CustomAdapter(Context context, Ocitanje[] values) {
        super(context, R.layout.predlozak, values);
        mContext = context;
        mydb = new DBHelper(getContext());
        brojRedova = mydb.numberOfRows();
    }

    @Override
    public int getCount() {
        return brojRedova-1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.predlozak, null);
            holder = new ViewHolder();
            holder.vrijemeKraj = (TextView) convertView.findViewById(R.id.vrijemeKrajText);
            holder.datum = (TextView) convertView.findViewById(R.id.datumText);
            holder.vrijemePocetak = (TextView) convertView.findViewById(R.id.vrijemeText);
            holder.prostorija = (TextView) convertView.findViewById(R.id.prostorijaText);
            holder.slika = (ImageView) convertView.findViewById(R.id.slika);
            holder.layout = (RelativeLayout) convertView.findViewById(R.id.linearLayout);
            holder.boja = (TextView) convertView.findViewById(R.id.boja);
            holder.bojaGore = (TextView) convertView.findViewById(R.id.bojaGore);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        Cursor rs = mydb.getData(brojRedova - 1 - position);
        rs.moveToFirst();
        String uploadano = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_UPLOADANO));
        rs.close();

        Ocitanje ocitanje = getItem(position);
        holder.datum.setText(ocitanje.getDate());
        holder.vrijemePocetak.setText(ocitanje.getStartTime());
        holder.prostorija.setText(ocitanje.getRoom());
        holder.vrijemeKraj.setText(ocitanje.getEndTime());

        File imgFile = slikaZaOcitanje(ocitanje.getRoom(), ocitanje.getDate(), ocitanje.getEndTime());
        if(imgFile.exists()){
            holder.slika.setImageResource(R.drawable.camera);
        }else{
            holder.slika.setImageResource(0);
        }

        if (uploadano.equals("da")){
            holder.boja.setBackgroundColor(Color.rgb(19, 113, 50));
            holder.bojaGore.setBackgroundColor(Color.rgb(19, 113, 50));
        }else {
            holder.boja.setBackgroundColor(Color.rgb(166, 0, 17));
            holder.bojaGore.setBackgroundColor(Color.rgb(166, 0, 17));
        }

       // holder.slika.setTag(new Integer(position));
      /*  holder.slika.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getItem(position).getStartOrEnd().equals("kraj")){
                    Toast.makeText(mContext, "Otvaram sliku...", Toast.LENGTH_SHORT).show();
                }
            }
        });
*/
        return convertView;
    }


    static class ViewHolder{
        TextView boja;
        TextView bojaGore;
        TextView vrijemeKraj;
        TextView datum;
        TextView vrijemePocetak;
        TextView prostorija;
        ImageView slika;
        RelativeLayout layout;
    }

    public File slikaZaOcitanje(String prostorija, String datum, String vrijeme){
        String put = prostorija + "_"+ datum + "_" + vrijeme + ".jpeg";
        String path = Environment.getExternalStorageDirectory() + "/" + put;
        File imgFile = new File(path);
        return imgFile;
    }

}