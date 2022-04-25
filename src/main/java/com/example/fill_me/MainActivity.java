package com.example.fill_me;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    EditText editTextAmount;
    Button button10, button20, button30, buttonApply;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    MifareClassic mfc;

    private int amount = 0;

    final static String TAG = "nfc_test";
    final static String KEYA = "0734bfb93dab".toUpperCase();
    final static String KEYB = "85a438f72a8a".toUpperCase();

    Vibrator vibrator;


    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);


        editTextAmount = findViewById(R.id.editTextAmount);
        button10 = findViewById(R.id.buttonM10);
        button20 = findViewById(R.id.button20);
        button30 = findViewById(R.id.buttonP10);
        buttonApply = findViewById(R.id.buttonApply);

        button10.setOnClickListener(this::onClickButtons);
        button20.setOnClickListener(this::onClickButtons);
        button30.setOnClickListener(this::onClickButtons);
        buttonApply.setOnClickListener(this::onClickButtons);

        onNewIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menuNewCard:{
                Toast.makeText(this, "You press new card", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.menuAdvCheckBox:{

                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    Toast.makeText(this, "Menu avance activer", Toast.LENGTH_SHORT).show();
                    item.setChecked(true);
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);

    }

    public void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            mfc = MifareClassic.get(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
            assert mfc != null;
            checkNfcConnection();
            readAmountOnCard(mfc);

        }
    }

    public void readAmountOnCard(MifareClassic mifare){
        try{
            mfc.connect();
            if(mifare.authenticateSectorWithKeyA(1, hex2Bytes(KEYA))){
                Log.i(TAG, "Authentication successful with key : "+ KEYA);
                byte[] result = mifare.readBlock(4);
                setCardAmount(MfcHelper.readShortFromHexString(bytes2Hex(result))/100.0);
                Toast.makeText(this, "Read Successful!", Toast.LENGTH_SHORT).show();

            }

        }catch(IOException e){
            Log.v(TAG, "Error connecting to tag", e);
        }catch(FormatException e){
          Log.e(TAG, "Format of data on tag is not valid", e);
        } finally {
            try{
            mfc.close();
            }catch(IOException e){
                Log.e(TAG, "Error closing tag connection", e);
            }
        }
    }


    public static byte[] hex2Bytes(String hex) {
        if (!(hex != null && hex.length() % 2 == 0
                && hex.matches("[0-9A-Fa-f]+"))) {
            return null;
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i+1), 16));
            }
        } catch (Exception e) {
            Log.d(TAG, "Argument(s) for hexStringToByteArray(String s)"
                    + "was not a hex string");
        }
        return data;
    }

    public static String bytes2Hex(byte[] bytes) {
        StringBuilder ret = new StringBuilder();
        if (bytes != null) {
            for (Byte b : bytes) {
                ret.append(String.format("%02X", b.intValue() & 0xFF));
            }
        }
        return ret.toString();
    }

    public void checkNfcConnection(){
        if(mfc != null) {
            try {
                mfc.connect();
                buttonApply.setEnabled(true);
                button10.setEnabled(true);
                button20.setEnabled(true);
                button30.setEnabled(true);
            } catch (IOException e) {
                Toast.makeText(this, "No tag found!", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error connecting to tag", e);
                buttonApply.setEnabled(false);
                button10.setEnabled(false);
                button20.setEnabled(false);
                button30.setEnabled(false);
            } finally {
                try {
                    mfc.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing tag connection", e);
                }
            }
        }
    }

    public void onClickButtons(View view){
        checkNfcConnection();
        switch(view.getId()){
            case R.id.buttonM10:{
                setCardAmount(getCardAmount()-10);
                break;
            }
            case R.id.button20:{
                setCardAmount(20);
                break;
            }
            case R.id.buttonP10:{
                setCardAmount(getCardAmount()+10);
                break;
            }
            case R.id.buttonApply:{
                writeAmountToCard();
                break;
            }
        }

    }

    private void setCardAmount(double amount){
        if(amount <= 100 && amount >= 0) {
            this.amount = (int)amount;

        }else if(amount > 100){
            this.amount = 100;

        }else if(amount < 0){
            this.amount = 0;

        }else{
            this.amount = 0;

        }

        this.amount *= 100;
        editTextAmount.setText(getCardAmount() + " $");

    }

    double getCardAmount(){
        return amount/100.0;
    }

    public void writeAmountToCard(){
        // Utiliser ma class utilitaire HexTool pour la conversion Big et little endian
        try {
            mfc.connect();
            // Amount data are stored on block 4 and 8
            mfc.authenticateSectorWithKeyB(1, hex2Bytes(KEYB));
            byte[] dataToWrite = hex2Bytes(MfcHelper.makeValidHexString((short)this.amount));
            mfc.writeBlock(4, dataToWrite);
            mfc.authenticateSectorWithKeyB(2, hex2Bytes(KEYB));
            mfc.writeBlock(8, dataToWrite);
            Toast.makeText(this, "Write Complete!", Toast.LENGTH_SHORT).show();
            Log.v(TAG, "Amount successfully wrote to the tag!");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrate();
            }
        }catch (IOException e){
            Log.e(TAG, "Error connecting to tag", e);
        }finally {
            try{
                mfc.close();
            }catch (IOException e){
                Log.e(TAG, "Error closing connection to tag");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void vibrate(){
        vibrator.cancel();
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
    }

}