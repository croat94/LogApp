package com.zavrsni.jasamrafoooo.logapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;


public class NovoOcitanje extends ActionBarActivity {

    DBHelper mydb;
    ProgressDialog progress;

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    public String pocIliKraj = "nijedno";
    public String date;
    public String time;
    public int i;
    public String prostorija="";
    public Bitmap myPhoto;
    public boolean imaSliku = false;
    public boolean dozvoljenoCitanje = false;
    public boolean nastavitiSOcitanjem;

    private TextView mTextView;
    private NfcAdapter mNfcAdapter;
    private Uri mImageUri;
    Toast t;

    ImageView slikaProstorije;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_ocitanje);

        mydb = new DBHelper(this);
        i = mydb.numberOfRows();

        provjeriUnosImena();

        mTextView = (TextView) findViewById(R.id.textTag);
        slikaProstorije = (ImageView) findViewById(R.id.slikaProstorije);
        setupNfcAdapter();
        handleIntent(getIntent());
    }

    public void onButtonZavrsi(View view) {
        dozvoljenoCitanje = true;
        pocIliKraj = "kraj";
        provjeriPosljednjeOcitanje();
    }

    public void onButtonPocni(View view){
        dozvoljenoCitanje = true;
        pocIliKraj = "poc";
        provjeriPosljednjeOcitanje();
    }

    public void spremiPodatke(){
        Cursor rs = mydb.getData(0);
        rs.moveToFirst();
        String pomIme = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_DATUM));
        String pomPrezime = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_POCETAK));
        if (!rs.isClosed()){
            rs.close();
        }
        if (pocIliKraj.equals("poc"))
            mydb.unesiOcitanje(i, date, time, "Nije završeno", prostorija, pomIme + " " + pomPrezime, "ne");
        else {
            mydb.updateKraj(mydb.numberOfRows(), time);
            if (imaSliku) {

                Runnable r = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        spremiSliku();
                        new Runnable()
                        {
                            public void run()
                            {
                                Toast.makeText(NovoOcitanje.this,
                                        "Spremanje podataka...", Toast.LENGTH_LONG).show();
                            }
                        };
                    }
                };

                Thread t = new Thread(r);
                t.start();
            }
        }
        //Toast.makeText(this, "Podaci su spremljeni u lokalnu bazu podataka", Toast.LENGTH_LONG).show();
        finish();
    }

    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public boolean launchCamera() {
        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);*/

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo;
        try {
            // place where to store camera taken picture
            photo = this.createTemporaryFile("picture", ".jpg");
            photo.delete();
        } catch (Exception e) {
            return false;
        }
        mImageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        //start camera intent
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        return true;
    }
    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdir();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    public void grabImage(ImageView imageView)
    {
        this.getContentResolver().notifyChange(mImageUri, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
            imageView.setImageBitmap(bitmap);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to load", e);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK)
        {
            imaSliku = true;
            this.grabImage(slikaProstorije);
            Toast.makeText(this, "Spremanje podataka u lokalnu bazu podataka...", Toast.LENGTH_SHORT).show();
            spremiPodatke();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void spremiSliku(){
        myPhoto= ((BitmapDrawable)slikaProstorije.getDrawable()).getBitmap();
        File sdCardDirectory = Environment.getExternalStorageDirectory(); //dohvati SD karticu
        File image = new File(sdCardDirectory, prostorija + "_"+ date + "_" + time + ".jpeg");
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(image);
            myPhoto.compress(Bitmap.CompressFormat.JPEG, 100, outStream); /* 100 to keep full quality of the image */
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setupNfcAdapter(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "Ovaj uređaj ne podržava NFC!", Toast.LENGTH_LONG).show();
            finish();
        }
        else if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC nije uključen!", Toast.LENGTH_LONG).show();
            finish();
        }else{
            mTextView.setText("Odaberite opciju");
        }
    }

    //************************************ možda nije potrebno*************************************
    private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }




    @Override
    protected void onResume() {
        super.onResume();
        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }


    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }


    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */
            byte[] payload = record.getPayload();
            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;
            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"
            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }else {
                dozvoljenoCitanje = false;
            }
            prostorija = result;
            //dohvati vrijeme i datum i spremi ih u bazu
            Calendar cal = Calendar.getInstance();
            date = ""+cal.get(Calendar.DATE)+"."+(cal.get(Calendar.MONTH)+1)+"."+cal.get(Calendar.YEAR)+".";
            time = ""+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND);
            date = uljepsajFormat(0, cal.get(Calendar.DATE), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
            time = uljepsajFormat(1, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
            //omoguci fotografiranje i spremanje nakon skeniranja za kraj aktivnosti
            if (dozvoljenoCitanje) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(100); //vibrira 100ms
                dozvoljenoCitanje = false;
                mTextView.setText(date+"  "+time);
                if (pocIliKraj.equals("poc")) {
                    spremiPodatke();
                } else {
                    if (hasCamera())
                        launchCamera();
                    else
                        spremiPodatke();
                }
            }
        }
    }

    public String uljepsajFormat(int vrijemeIliDatum, int prvi, int drugi, int treci){
        String prviText, drugiText, treciText;
        prviText = provjeriFormat(prvi);
        drugiText = provjeriFormat(drugi);
        treciText = provjeriFormat(treci);
        if (vrijemeIliDatum == 0)
            return prviText + "." + drugiText + "." + treciText + ".";
        else
            return prviText + ":" + drugiText + ":" + treciText;
    }

    public String provjeriFormat(int broj){
        if (broj < 10)
            return "0" + broj;
        else
            return ""+ broj;
    }

    public void provjeriUnosImena(){
        //ako nije uneseno ime, mora ga se unesti
        Cursor rs = mydb.getData(0);
        rs.moveToFirst();
        if (mydb.numberOfRows()==1 && rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_DATUM)).equals("/")){
            Toast.makeText(this, "Unesite podatke o osobi prije skeniranja oznaka!", Toast.LENGTH_LONG).show();
            finish();
        }else{
            String pomIme = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_DATUM));
            String pomPrezime = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_POCETAK));
            if (pomIme.equals("") || pomPrezime.equals("")){
                Toast.makeText(this, "Unesite podatke o osobi prije skeniranja oznaka!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void provjeriPosljednjeOcitanje(){
        Cursor rs = mydb.getData(mydb.numberOfRows() - 1);
        rs.moveToFirst();
        String temp = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_KRAJ));
        String tempUploadano = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_UPLOADANO));
        if (pocIliKraj.equals("poc")){
            if (temp.equals("Nije završeno") && tempUploadano.equals("ne")) {
                alertDialog("Niste završili prošlu aktivnost!",
                        "Jeste li sigurni da želite započeti novu aktivnost?");
            }else{
                zapocniProgressDialog();
            }
        }else{
            if (temp.equals("Nije završeno")&& tempUploadano.equals("ne")) {
                zapocniProgressDialog();
            }else {
                alertDialog("Upozorenje!",
                        "Ne možete završiti aktivnost prije nego što ju započnete!");
            }
        }
    }

    public void alertDialog(String title, String message){
        try {
            AlertDialog alert = new AlertDialog.Builder(NovoOcitanje.this).create();
            alert.setCancelable(false);

            alert.setTitle(title);
            alert.setMessage(message);
            alert.setIcon(android.R.drawable.ic_dialog_alert);
            if (pocIliKraj.equals("poc")) {
                alert.setButton2("Da", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                zapocniProgressDialog();
                            }
                        }
                );
                alert.setButton("Ne", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                );
            }else{
                alert.setButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                );
            }
            alert.show();
        }
        catch(Exception e){}
    }

    public void zapocniProgressDialog(){
        progress = ProgressDialog.show(NovoOcitanje.this, "", "Skenirajte NFC oznaku", true);
        // omogući prekidanje progress dialoga
        progress.setCancelable(true);
        progress.setCanceledOnTouchOutside(false);
    }
}