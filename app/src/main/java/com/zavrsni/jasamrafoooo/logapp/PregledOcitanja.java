package com.zavrsni.jasamrafoooo.logapp;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;


public class PregledOcitanja extends ActionBarActivity {

    public ListAdapter mojAdapter;
    DBHelper mydb;
    public Ocitanje[] pomocna;
    public Ocitanje[] pomocna2;
    int brojRedova;
    int brojRedova2;
    int i;
    ListView lista;
    String datum;
    String vrijemePocetak;
    String vrijemeKraj;
    String prostorija;
    ImageView myImage;
    TextView imageBackgroundColor;

    @Override
    public void onBackPressed() {
        if (imageBackgroundColor.getVisibility() == View.VISIBLE){
            myImage.setImageDrawable(null);
            lista.setEnabled(true);
            imageBackgroundColor.setBackgroundColor(Color.argb(0, 0, 0, 0));
            imageBackgroundColor.setVisibility(View.GONE);
        }else{
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregled_ocitanja);
        mydb = new DBHelper(this);
        brojRedova = mydb.numberOfRows();
        //pomocna = new Ocitanje[brojRedova];
        myImage = (ImageView) findViewById(R.id.myImage);
        imageBackgroundColor = (TextView) findViewById(R.id.imageBackgroundColor);
        imageBackgroundColor.setVisibility(View.GONE);

        brojRedova2 = mydb.numberOfRows();
        pomocna = new Ocitanje[brojRedova2];
        pomocna2 = new Ocitanje[brojRedova2];
        for (i=brojRedova2-1; i>=0;i--){
            Cursor rs = mydb.getData(i);
            rs.moveToFirst();
            String uploadano = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_UPLOADANO));
            datum = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_DATUM));
            vrijemeKraj = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_KRAJ));
            vrijemePocetak = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_POCETAK));
            prostorija = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_PROSTORIJA));
            if (i == brojRedova - 1 && uploadano.equals("ne") && vrijemeKraj.equals("Nije završeno")){
                vrijemeKraj = "U tijeku";
            }

            pomocna[i] = new Ocitanje(datum, vrijemePocetak, vrijemeKraj, prostorija);
            if (!rs.isClosed()){
                rs.close();
            }
        }
        mydb.close();

        for (i=0; i<brojRedova2;i++){
            pomocna2[i] = pomocna[brojRedova2-1-i];
        }
        mojAdapter = new CustomAdapter(getApplicationContext(), pomocna2);
        lista = (ListView) findViewById(R.id.predlozak);

        lista.setAdapter(mojAdapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String putDoSlike = dohvatiPodatkeOSlici(position);
                prikaziSliku(putDoSlike);
            }
        });
    }

    public String dohvatiPodatkeOSlici(int position){
        Cursor rs = mydb.getData(brojRedova2 - 1 -position);
        rs.moveToFirst();
        datum = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_DATUM));
        vrijemeKraj = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_KRAJ));
        prostorija = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_PROSTORIJA));
        if (!rs.isClosed()){
            rs.close();
        }
        return prostorija + "_"+ datum + "_" + vrijemeKraj + ".jpeg";
    }

    public void prikaziSliku(String put){
        final String path = Environment.getExternalStorageDirectory() + "/" + put;
        final File imgFile = new File(path);
        if(imgFile.exists()) {

            imageBackgroundColor.setVisibility(View.VISIBLE);
            imageBackgroundColor.setBackgroundColor(Color.rgb(0, 0, 0));
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                myImage.setImageBitmap(myBitmap);
                                lista.setEnabled(false);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }


    }
}
