package com.amila.dev.virtualmenu;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends YouTubeBaseActivity implements EasyPermissions.PermissionCallbacks, YouTubePlayer.OnInitializedListener {
    private static final String PREF_ACCOUNT_NAME = "";
    WebView view;
    ImageView imageView;
    ImageView imageView2;
    TextView title;
    TextView date;
    TextSwitcher switcher;

    Common m;
    String tempUrl;
    String tempTitle;
    ArrayList<String> url;
    YouTubePlayerView playerView;
    YouTubePlayer player;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    ArrayList<ListItem> items;
    LinearLayoutManager linearLayoutManager;

    ArrayList<String> bList;

    ArrayList<ListItem> tempList;

    boolean doubleBackToExitPressedOnce = false;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = findViewById(R.id.textView1);
        date = findViewById(R.id.textView2);
        view = findViewById(R.id.web);
        imageView = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        recyclerView = findViewById(R.id.recycler);
        switcher = findViewById(R.id.text_switcher);
        playerView = findViewById(R.id.youtube);

        m = Common.getCommon();

        title.setText(m.getSharedpreferences().getString("ShopName","Null"));
        url = new ArrayList<>();

        bList = m.getInfoList();
        items = m.getMenuList();
        tempList = new ArrayList<>();

        startUp();
        menu();
        //webPlayer();
        slideshow();
        autoScroll();
        setSwitcher();

        playerView.setVisibility(View.VISIBLE);
        playerView.initialize("AIzaSyCdvMuQOpWhjjanXscUS48Ku1iBiK3eC58",this);
        Toast.makeText(this,"You",Toast.LENGTH_LONG).show();

        /*final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startUp();
                getResultsFromApi();
                String url = m.getSharedpreferences().getString("URL",null);
                if(!m.isWebUrl() && tempUrl != null && url.compareTo(tempUrl) != 0) {
                    view.loadUrl(tempUrl);
                    m.getEditor().putString("URL",tempUrl);
                    m.getEditor().commit();
                }
                menu();
                handler.postDelayed(this, 60000);
            }
        };
        handler.postDelayed(runnable, 0);*/
    }

    private void startUp(){
        date.setText(getDate());

        if(isDeviceOnline()){
            view.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.GONE);
        }else{
            view.setVisibility(View.GONE);
            imageView2.setVisibility(View.VISIBLE);
            bList.add("No Network Connectivity!!");
        }
    }

    private void slideshow(){
        final Counter c = new Counter();

        String path = Environment.getExternalStorageDirectory().toString()+"/Pictures/VirtualMenu/";
        File directory = new File(path);
        final File[] files = directory.listFiles();

        if (files.length > 0) {
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = BitmapFactory.decodeFile(files[c.getVal()].getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                    imageView2.setImageBitmap(bitmap);
                    handler.postDelayed(this, 5000);
                    c.increment();
                    if(c.getVal() == files.length){
                        c.reset();
                    }
                }
            };
            handler.postDelayed(runnable, 0);
        }


    }

    private void menu(){
        items = m.getMenuList();
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new Adaptor(items);
        recyclerView.setAdapter(adapter);
    }

    private void autoScroll() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstItemVisible = linearLayoutManager.findFirstVisibleItemPosition();
                if (firstItemVisible != 0 && firstItemVisible % items.size() == 0) {
                    recyclerView.getLayoutManager().scrollToPosition(0);
                }
            }
        });

        if(m.isAutoScroll()) {
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    recyclerView.scrollBy(0, 1);
                    handler.postDelayed(this, 50);
                }
            };
            handler.postDelayed(runnable, 0);
        }
    }

    private void setSwitcher(){
        final Counter c = new Counter();
        final Handler handler = new Handler();
        if(bList.size() > 0) {
            System.out.println(bList);
            switcher.removeAllViews();

            switcher.setFactory(new ViewSwitcher.ViewFactory() {
                @Override
                public View makeView() {
                    TextView tv = new TextView(MainActivity.this);
                    tv.setText(bList.get(0));
                    tv.setTextSize(20);
                    tv.setGravity(Gravity.CENTER_HORIZONTAL);
                    return tv;
                }
            });

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    switcher.setText(bList.get(c.getVal()));
                    c.increment();
                    handler.postDelayed(this, 5000);
                    if (c.getVal() == bList.size()) {
                        c.reset();
                    }
                }
            };
            handler.postDelayed(runnable, 0);
        }
    }

    private String getDate() {
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
        String date = formatter.format(today);
        return date;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void webPlayer() {
        String play;
        Date d = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String today = formatter.format(d);
        if(m.isWebUrl()){
            url.clear();
            url.add(m.getSharedpreferences().getString("LocalURL",null));
        }else {
            if(m.getScheduleList().containsKey(today)) {
                url.clear();
                url.add(m.getScheduleList().get(today));
            }else{
                url = m.getUrlList();
            }
        }



        if(url.get(0).contains("youtube")){
            System.out.println("you");
            playerView.initialize("AIzaSyCdvMuQOpWhjjanXscUS48Ku1iBiK3eC58",this);
            playerView.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }else {
            playerView.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
            view.getSettings().setAppCacheEnabled(true);
            view.getSettings().setJavaScriptEnabled(true);
            view.getSettings().setAppCachePath(getCacheDir().getAbsolutePath());
            view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            view.getSettings().setMediaPlaybackRequiresUserGesture(false);
            view.loadUrl(url.get(new Random().nextInt(url.size())));

            view.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    emulateClick(view);
                }
            });
        }
    }

    private void emulateClick(final WebView webview) {
        long delta = 100;
        long downTime = SystemClock.uptimeMillis();
        float x = webview.getLeft() + webview.getWidth() / 2;
        float y = webview.getTop() + webview.getHeight() / 2;

        final MotionEvent downEvent = MotionEvent.obtain(downTime, downTime + delta, MotionEvent.ACTION_DOWN, x, y, 0);
        final MotionEvent upEvent = MotionEvent.obtain(downTime, downTime + delta, MotionEvent.ACTION_UP, x + 10, y + 10, 0);

        webview.post(new Runnable() {
            @Override
            public void run() {
                if (webview != null) {
                    webview.dispatchTouchEvent(downEvent);
                    webview.dispatchTouchEvent(upEvent);
                }
            }
        });
    }

    public void settings(View view) {
        Intent n = new Intent(this,SettingsActivity.class);
        startActivity(n);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                emulateClick(view);
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (m.getmCredential().getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(this,"No network connection available",Toast.LENGTH_LONG).show();
            System.out.println("No network connection available");
        } else {
            new MainActivity.MakeRequestTask(m.getmCredential()).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                m.getmCredential().setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        m.getmCredential().newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this,"This app requires Google Play Services. Please install \" +\n" +
                            "                            \"Google Play Services on your device and relaunch this app.",Toast.LENGTH_LONG).show();
                    System.out.println("This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        m.getmCredential().setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        /*for(String u: url) {
            youTubePlayer.cuePlaylist(url.get(new Random().nextInt(url.size())));
        }*/
        if(null== player) return;
        if (!b) {
            Toast.makeText(this, "ini", Toast.LENGTH_LONG).show();
            player.loadVideo("https://www.youtube.com/watch?v=zvTgnq11gMA");
            Toast.makeText(this, "Play", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                getInfo();
                getData();
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private List<String> getDataFromApi() throws IOException {
            String range = "Sheet1!A:B";
            List<String> results = new ArrayList<>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(m.getSheetId(), range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                for (List row : values) {
                    results.add(row.get(0) + ", " + row.get(1));
                }
            }
            return results;
        }


        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(List<String> output) {
            if (output == null || output.size() == 0) {
                System.out.println("No results returned.");
            }
            m.clearMenu();
            for (String s : output) {
                String[] li = s.split(",");
                m.addMenuItem(li[0],li[1]);
            }
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    System.out.println("The following error occurred:\n"
                            + mLastError);
                }
            } else {
                System.out.println("Request cancelled.");
            }
        }

        public void getInfo() throws IOException {
            String range = "Sheet2";
            ValueRange response = this.mService.spreadsheets().values()
                    .get(m.getSheetId(), range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                m.clearInfo();
                for (List row : values) {
                    m.addInfoItem(row.get(0).toString());
                }
            }
        }

        public void getData() throws IOException {
            ValueRange result = mService.spreadsheets().values().get(m.getSheetId(), "Sheet3").execute();
            tempTitle = String.valueOf(result.getValues().get(0).get(0));
            tempUrl = String.valueOf(result.getValues().get(1).get(0));
        }
    }
}
