package com.amila.dev.virtualmenu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SettingsActivity extends AppCompatActivity {

    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences.Editor editor;
    SharedPreferences sharedpreferences;

    EditText et;
    EditText et2;
    Switch sw;
    Switch sw2;

    Common m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        et = findViewById(R.id.editText);
        et2 = findViewById(R.id.editText2);
        sw = findViewById(R.id.switch1);
        sw2 = findViewById(R.id.switch2);
        m = Common.getCommon();

        if(sharedpreferences.getString("SheetID","Null") != null){
            et.setText(sharedpreferences.getString("SheetID","Null"));
        }
        if(sharedpreferences.getString("LocalURL","Null") != null){
            et2.setText(sharedpreferences.getString("LocalURL","NULL"));
        }
        sw.setChecked(m.isAutoScroll());
        sw2.setChecked(m.isWebUrl());
        et2.setEnabled(sw2.isChecked());
    }

    private void getKeyHash(String hashStretagy) {
        try {
            final PackageInfo info = this.getPackageManager()
                    .getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance(hashStretagy);
                md.update(signature.toByteArray());

                final byte[] digest = md.digest();
                final StringBuilder toRet = new StringBuilder();
                for (int i = 0; i < digest.length; i++) {
                    if (i != 0) toRet.append(":");
                    int b = digest[i] & 0xff;
                    String hex = Integer.toHexString(b);
                    if (hex.length() == 1) toRet.append("0");
                    toRet.append(hex);
                }

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("SHA1", toRet.toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this,"App Fingerprint Copied to the Clipboard.",Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }

    public void save(View view) {
        editor.putString("SheetID", et.getText().toString());
        m.setWebUrl(sw2.isChecked());
        if(sw.isChecked()){
            editor.putBoolean("AutoScroll", true);
            m.setAutoScroll(true);
        }else{
            editor.putBoolean("AutoScroll", false);
            m.setAutoScroll(false);
        }
        if(sw2.isChecked() && et2.getText().length() > 0){
            editor.putString("LocalURL",et2.getText().toString());
        }else{
            sw2.setChecked(false);
            editor.putBoolean("Local",sw2.isChecked());
        }
        editor.commit();
        m.setSheetId(et.getText().toString());
        Intent intent = new Intent(this,SplashActivity.class);
        startActivity(intent);
    }

    public void setUrl(View view) {
        et2.setEnabled(sw2.isChecked());
        editor.putBoolean("Local",sw2.isChecked());
        System.out.println(sw2.isChecked());
    }

    public void copyKey(View view) {
        getKeyHash("SHA1");
    }
}
