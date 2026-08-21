package com.eveningoutpost.dexdrip.utils;


import static com.eveningoutpost.dexdrip.EditAlertActivity.unitsConvert2Disp;
import static com.eveningoutpost.dexdrip.models.JoH.showNotification;
import static com.eveningoutpost.dexdrip.models.JoH.tolerantParseDouble;
import static com.eveningoutpost.dexdrip.services.Ob1G5CollectionService.clearDataWhenTransmitterIdEntered;
import static com.eveningoutpost.dexdrip.utilitymodels.Constants.OUT_OF_RANGE_GLUCOSE_ENTRY_ID;
import static com.eveningoutpost.dexdrip.utils.DexCollectionType.getBestCollectorHardwareName;
import static com.eveningoutpost.dexdrip.xdrip.gs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.bytehamster.lib.preferencesearch.SearchConfiguration;
import com.bytehamster.lib.preferencesearch.SearchPreferenceResult;
import com.bytehamster.lib.preferencesearch.SearchPreferenceResultListener;
import com.eveningoutpost.dexdrip.BasePreferenceActivity;
import com.eveningoutpost.dexdrip.GcmActivity;
import com.eveningoutpost.dexdrip.Home;
import com.eveningoutpost.dexdrip.NFCReaderX;
import com.eveningoutpost.dexdrip.ParakeetHelper;
import com.eveningoutpost.dexdrip.R;
import com.eveningoutpost.dexdrip.WidgetUpdateService;
import com.eveningoutpost.dexdrip.alert.Registry;
import com.eveningoutpost.dexdrip.calibrations.PluggableCalibration;
import com.eveningoutpost.dexdrip.cgm.carelinkfollow.CareLinkFollowService;
import com.eveningoutpost.dexdrip.cgm.carelinkfollow.auth.CareLinkAuthType;
import com.eveningoutpost.dexdrip.cgm.dex.TxIdHelper;
import com.eveningoutpost.dexdrip.cgm.nsfollow.NightscoutFollow;
import com.eveningoutpost.dexdrip.cgm.sharefollow.ShareFollowService;
import com.eveningoutpost.dexdrip.cgm.webfollow.Cpref;
import com.eveningoutpost.dexdrip.cgm.carelinkfollow.auth.CareLinkAuthenticator;
import com.eveningoutpost.dexdrip.cgm.carelinkfollow.auth.CareLinkCredentialStore;
import com.eveningoutpost.dexdrip.cloud.jamcm.Pusher;
import com.eveningoutpost.dexdrip.cloud.nightlite.NightLiteClient;
import com.eveningoutpost.dexdrip.cloud.nightlite.NightLiteEntry;
import com.eveningoutpost.dexdrip.cloud.nightlite.NightLiteQR;
import com.eveningoutpost.dexdrip.healthconnect.HealthConnectEntry;
import com.eveningoutpost.dexdrip.healthconnect.HealthGamut;
import com.eveningoutpost.dexdrip.insulin.inpen.InPenEntry;
import com.eveningoutpost.dexdrip.models.DesertSync;
import com.eveningoutpost.dexdrip.models.JoH;
import com.eveningoutpost.dexdrip.models.Profile;
import com.eveningoutpost.dexdrip.models.UserError;
import com.eveningoutpost.dexdrip.models.UserError.ExtraLogTags;
import com.eveningoutpost.dexdrip.models.UserError.Log;
import com.eveningoutpost.dexdrip.models.UserNotification;
import com.eveningoutpost.dexdrip.profileeditor.ProfileEditor;
import com.eveningoutpost.dexdrip.receiver.InfoContentProvider;
import com.eveningoutpost.dexdrip.services.ActivityRecognizedService;
import com.eveningoutpost.dexdrip.services.BluetoothGlucoseMeter;
import com.eveningoutpost.dexdrip.services.DexCollectionService;
import com.eveningoutpost.dexdrip.services.G5BaseService;
import com.eveningoutpost.dexdrip.services.PlusSyncService;
import com.eveningoutpost.dexdrip.services.UiBasedCollector;
import com.eveningoutpost.dexdrip.services.broadcastservice.BroadcastService;
import com.eveningoutpost.dexdrip.tidepool.AuthFlowOut;
import com.eveningoutpost.dexdrip.tidepool.TidepoolUploader;
import com.eveningoutpost.dexdrip.tidepool.UploadChunk;
import com.eveningoutpost.dexdrip.ui.LockScreenWallPaper;
import com.eveningoutpost.dexdrip.ui.dialog.GenericConfirmDialog;
import com.eveningoutpost.dexdrip.utilitymodels.BgGraphBuilder;
import com.eveningoutpost.dexdrip.utilitymodels.CollectionServiceStarter;
import com.eveningoutpost.dexdrip.utilitymodels.Constants;
import com.eveningoutpost.dexdrip.utilitymodels.Experience;
import com.eveningoutpost.dexdrip.utilitymodels.Inevitable;
import com.eveningoutpost.dexdrip.utilitymodels.Intents;
import com.eveningoutpost.dexdrip.utilitymodels.Pref;
import com.eveningoutpost.dexdrip.utilitymodels.ShotStateStore;
import com.eveningoutpost.dexdrip.utilitymodels.SpeechUtil;
import com.eveningoutpost.dexdrip.utilitymodels.UpdateActivity;
import com.eveningoutpost.dexdrip.utilitymodels.WholeHouse;
import com.eveningoutpost.dexdrip.utilitymodels.pebble.PebbleUtil;
import com.eveningoutpost.dexdrip.utilitymodels.pebble.PebbleWatchSync;
import com.eveningoutpost.dexdrip.utilitymodels.pebble.watchface.InstallPebbleClassicTrendWatchface;
import com.eveningoutpost.dexdrip.utilitymodels.pebble.watchface.InstallPebbleSnoozeControlApp;
import com.eveningoutpost.dexdrip.utilitymodels.pebble.watchface.InstallPebbleTrendClayWatchFace;
import com.eveningoutpost.dexdrip.utilitymodels.pebble.watchface.InstallPebbleTrendWatchFace;
import com.eveningoutpost.dexdrip.utilitymodels.pebble.watchface.InstallPebbleWatchFace;
import com.eveningoutpost.dexdrip.utils.framework.IncomingCallsReceiver;
import com.eveningoutpost.dexdrip.watch.lefun.LeFunEntry;
import com.eveningoutpost.dexdrip.watch.miband.MiBand;
import com.eveningoutpost.dexdrip.watch.miband.MiBandEntry;
import com.eveningoutpost.dexdrip.watch.miband.MiBandService;
import com.eveningoutpost.dexdrip.watch.thinjam.BlueJay;
import com.eveningoutpost.dexdrip.watch.thinjam.BlueJayAdapter;
import com.eveningoutpost.dexdrip.watch.thinjam.BlueJayEntry;
import com.eveningoutpost.dexdrip.wearintegration.Amazfitservice;
import com.eveningoutpost.dexdrip.wearintegration.WatchUpdaterService;
import com.eveningoutpost.dexdrip.webservices.XdripWebService;
import com.eveningoutpost.dexdrip.xDripWidget;
import com.eveningoutpost.dexdrip.xdrip;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nightscout.core.barcode.NSBarcodeConfig;

import net.tribe7.common.base.Joiner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class Preferences extends BasePreferenceActivity implements SearchPreferenceResultListener {
    private static final String TAG = "jamorham PREFS";
    private static byte[] staticKey;
    private volatile AllPrefsFragment preferenceFragment;

    private static Preference units_pref;
    private static String static_units;
    private static Preference profile_insulin_sensitivity_default;
    private static Preference profile_carb_ratio_default;

    private static ListPreference locale_choice;
    private static Preference force_english;
    private static Preference nfc_expiry_days;

    private static AllPrefsFragment pFragment;
    private BroadcastReceiver mibandStatusReceiver;

    // The following three variables enable us to create a common state from the input,
    // whether we scan from camera or a file, and continue with the same following
    // set of commands to avoid code duplication.
    private volatile String scanFormat = null; // The format of the scan
    private volatile String scanContents = null; // Text content of the scan coming either from camera or file
    private volatile byte[] scanRawBytes = null; // Raw bytes of the scan

    private void refreshFragments() {
        refreshFragments(null);
    }
    public static final double MIN_GLUCOSE_INPUT = 40; // The smallest acceptable input glucose value in mg/dL
    public static final double MAX_GLUCOSE_INPUT = 400; // The largest acceptable input glucose value in mg/dL

    private void refreshFragments(final String jumpTo) {
        this.preferenceFragment = new AllPrefsFragment(jumpTo);
        this.preferenceFragment.setParent(this);
        pFragment = this.preferenceFragment;
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                this.preferenceFragment).commit();
    }

    public static List<String> getAllPreferenceKeys(final PreferenceGroup parent) {
        final List<Preference> source = getAllPreferences(parent);
        final List<String> results = new ArrayList<>(source.size());
        for (final Preference preference : source) {
            results.add(preference.getKey());
        }
        return results;
    }

    public static List<Preference> getAllPreferences(final PreferenceGroup parent) {
        final int preferenceCount = parent.getPreferenceCount();
        final List<Preference> results = new ArrayList<>(preferenceCount);
        for (int i = 0; i < preferenceCount; i++) {
            final Preference preference = parent.getPreference(i);
            results.add(preference);
            if (preference instanceof PreferenceGroup) {
                // recurse
                results.addAll(getAllPreferences((PreferenceGroup)preference));
            }
        }
        return results;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSearchResultClicked(@NonNull SearchPreferenceResult searchPreferenceResult) {
        try {
            searchPreferenceResult.closeSearchPage(this);
            searchPreferenceResult.highlight(this.preferenceFragment, Color.YELLOW);
        } catch (RuntimeException e) {
            Log.wtf(TAG, "Got error trying to highlight search results: " + e);
            JoH.static_toast_long("" + e);
        }
    }


    public interface OnServiceTaskCompleted {
        void onTaskCompleted(byte[] result);
    }

    public class ServiceCallback implements OnServiceTaskCompleted {
        @Override
        public void onTaskCompleted(byte[] result) {
            if (result.length > 0) {
                if ((staticKey == null) || (staticKey.length != 16)) {
                    toast("Error processing security key");
                } else {
                    byte[] plainbytes = JoH.decompressBytesToBytes(CipherUtils.decryptBytes(result, staticKey));
                    staticKey = null;
                    Log.d(TAG, "Plain bytes size: " + plainbytes.length);
                    if (plainbytes.length > 0) {
                        SdcardImportExport.storePreferencesFromBytes(plainbytes, getApplicationContext());
                    } else {
                        toast("Error processing data - empty");
                    }
                }
            } else {
                toast("Error processing settings - no data - try again?");
            }
        }
    }


    private void toast(final String msg) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
            android.util.Log.d(TAG, "Toast msg: " + msg);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Couldn't display toast: " + msg);
        }
    }

    private void installxDripPlusPreferencesFromQRCode(SharedPreferences prefs, String data) {
        Log.d(TAG, "installing preferences from QRcode");
        try {
            Map<String, String> prefsmap = QRcodeUtils.decodeString(data);
            if (prefsmap != null) {
                if (prefsmap.containsKey(getString(R.string.all_settings_wizard))) {
                    if (prefsmap.containsKey(getString(R.string.wizard_key))
                            && prefsmap.containsKey(getString(R.string.wizard_uuid))) {
                        staticKey = CipherUtils.hexToBytes(prefsmap.get(getString(R.string.wizard_key)));

                        new WebAppHelper(new ServiceCallback()).executeOnExecutor(xdrip.executor, getString(R.string.wserviceurl) + "/joh-getsw/" + prefsmap.get(getString(R.string.wizard_uuid)));
                    } else {
                        Log.d(TAG, "Incorrectly formatted wizard pref");
                    }
                    return;
                }

                val sb = getMapKeysString(prefsmap);
                val msg = getString(R.string.import_qr_code_warning) + sb;

                GenericConfirmDialog.show(this, gs(R.string.are_you_sure), msg, () -> {
                    final SharedPreferences.Editor editor = prefs.edit();
                    int changes = 0;
                    for (Map.Entry<String, String> entry : prefsmap.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        //            Log.d(TAG, "Saving preferences: " + key + " = " + value);
                        if (value.equals("true") || (value.equals("false"))) {
                            editor.putBoolean(key, Boolean.parseBoolean(value));
                            changes++;
                        } else if (!value.equals("null")) {
                            editor.putString(key, value);
                            changes++;
                        }
                    }
                    editor.apply();
                    refreshFragments();
                    ExtraLogTags.readPreference(Pref.getStringDefaultBlank("extra_tags_for_logging"));
                    Toast.makeText(getApplicationContext(), "Loaded " + Integer.toString(changes) + " preferences from QR code", Toast.LENGTH_LONG).show();
                    PlusSyncService.clearandRestartSyncService(getApplicationContext());
                    DesertSync.settingsChanged(); // refresh
                    InfoContentProvider.ping("pref");
                    if (prefs.getString("dex_collection_method", "").equals("Follower")) {
                        PlusSyncService.clearandRestartSyncService(getApplicationContext());
                        GcmActivity.last_sync_request = 0;
                        GcmActivity.requestBGsync();
                    }
                });

            } else {
                android.util.Log.e(TAG, "Got null prefsmap during decode");
            }
        } catch (Exception e) {
            Log.e(TAG, "Got exception installing preferences");
        }

    }

    public static String getMapKeysString(final Map<String, ?> prefsmap) {
        val sb = new StringBuilder();
        val keysSet = prefsmap.keySet();
        val keyList = new ArrayList<>(keysSet);
        Collections.sort(keyList);
        for (val entry : keyList) {
            sb.append(entry);
            sb.append("\n");
        }
        return sb.toString();
    }


    public static Boolean getBooleanPreferenceViaContextWithoutException(Context context, String key, Boolean defaultValue) {
        try {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);

        } catch (ClassCastException ex) {
            return defaultValue;
        }
    }


    @Override
    protected synchronized void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Let's reset variables just to be sure
        scanFormat = null;
        scanContents = null;
        scanRawBytes = null;
        if (requestCode == Constants.HEALTH_CONNECT_RESPONSE_ID) {
            if (HealthConnectEntry.enabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (JoH.ratelimit("health-connect-bump", 2)) {
                        HealthGamut.init(this);
                    }
                }
            }
        }

        if (requestCode == Constants.ZXING_FILE_REQ_CODE) { // If we are scanning an image file, not using the camera
            // The core of the following section, selecting the file, converting it into a bitmap, and then to a bitstream, is from:
            // https://stackoverflow.com/questions/55427308/scaning-qrcode-from-image-not-from-camera-using-zxing
            if (data == null || data.getData() == null) {
                Log.e("TAG", "No file was selected");
                return;
            }
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap == null) {
                    Log.e("TAG", "uri is not a bitmap," + uri.toString());
                    return;
                }
                int width = bitmap.getWidth(), height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                bitmap.recycle();
                bitmap = null;
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                MultiFormatReader reader = new MultiFormatReader();
                try {
                    Result result = reader.decode(bBitmap);
                    scanFormat = result.getBarcodeFormat().toString();
                    scanContents = result.getText(); // The text content  of the scanned file
                    scanRawBytes = result.getRawBytes();
                } catch (NotFoundException e) {
                    Log.e("TAG", "decode exception", e);
                }
            } catch (FileNotFoundException e) {
                Log.e("TAG", "can not open file" + uri.toString(), e);
            }
        } else if (requestCode == Constants.ZXING_CAM_REQ_CODE) { // If we are scanning from camera
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            scanFormat = scanResult.getFormatName();
            scanContents = scanResult.getContents(); // The text content of the scan from camera
            scanRawBytes = scanResult.getRawBytes();
        }
        // We now have scan format, scan text content, and scan raw bytes in the corresponding variables.
        // Everything after this is applied whether we scanned with camera or from a file.

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (scanContents == null) { // If we have no scan content
            UserError.Log.d(TAG, "No scan results ");
            return;
        }

        if (scanFormat.equals("QR_CODE")) { // The scan is a QR code

            if (QRcodeUtils.hasDecoderMarker(scanContents)) {
                installxDripPlusPreferencesFromQRCode(prefs, scanContents);
                return;
            }

            try {
                if (BlueJay.processQRCode(scanRawBytes)) {
                    refreshFragments();
                    return;
                }
            } catch (Exception e) {
                // meh
            }


            final NSBarcodeConfig barcode = new NSBarcodeConfig(scanContents);
            if (barcode.hasMongoConfig()) {
                if (barcode.getMongoUri().isPresent()) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("cloud_storage_mongodb_uri", barcode.getMongoUri().get());
                    editor.putString("cloud_storage_mongodb_collection", barcode.getMongoCollection().or("entries"));
                    editor.putString("cloud_storage_mongodb_device_status_collection", barcode.getMongoDeviceStatusCollection().or("devicestatus"));
                    editor.putBoolean("cloud_storage_mongodb_enable", true);
                    editor.apply();
                }
                if (barcode.hasApiConfig()) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("cloud_storage_api_enable", true);
                    editor.putString("cloud_storage_api_base", Joiner.on(' ').join(barcode.getApiUris()));
                    editor.apply();
                } else {
                  //  prefs.edit().putBoolean("cloud_storage_api_enable", false).apply(); // no need to disable
                }
            }
            if (barcode.hasApiConfig()) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("cloud_storage_api_enable", true);
                editor.putString("cloud_storage_api_base", Joiner.on(' ').join(barcode.getApiUris()));
                editor.apply();
            } else {
               // prefs.edit().putBoolean("cloud_storage_api_enable", false).apply(); // no need to disable
            }

            if (barcode.hasMqttConfig()) {
                if (barcode.getMqttUri().isPresent()) {
                    URI uri = URI.create(barcode.getMqttUri().or(""));
                    if (uri.getUserInfo() != null) {
                        String[] userInfo = uri.getUserInfo().split(":");
                        if (userInfo.length == 2) {
                            String endpoint = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort();
                            if (userInfo[0].length() > 0 && userInfo[1].length() > 0) {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("cloud_storage_mqtt_endpoint", endpoint);
                                editor.putString("cloud_storage_mqtt_user", userInfo[0]);
                                editor.putString("cloud_storage_mqtt_password", userInfo[1]);
                                editor.putBoolean("cloud_storage_mqtt_enable", true);
                                editor.apply();
                            }
                        }
                    }
                }
            } else {
              //  SharedPreferences.Editor editor = prefs.edit();
               // editor.putBoolean("cloud_storage_mqtt_enable", false); // no need to disable
              //  editor.apply();
            }

            try {
                final NightLiteQR barcode2 = new NightLiteQR(scanContents);
                if (barcode2.hasNsLiteConfig()) {
                    UserError.Log.d(TAG, "NightLite QR code detected");
                    if (NightLiteEntry.setApi(barcode2.getApiUris())) {
                        JoH.static_toast_long("NightLite enabled");
                        NightLiteClient.doUpload();
                    }
                }
            } catch (Exception e) {
                UserError.Log.e(TAG, "Error processing NightLite QR code: " + e);
            }


        } else if (scanFormat.equals("CODE_128")) {
            Log.d(TAG, "Setting serial number to: " + scanContents);
            prefs.edit().putString("share_key", scanContents).apply();
        }
        refreshFragments();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            setTheme(R.style.OldAppTheme);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set theme");
        }
        super.onCreate(savedInstanceState);

        refreshFragments(getIntent() != null ? getIntent().getAction() : null);
        processExtraData();

        // cannot be in onResume as we display dialog to set
        try {
            PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceFragment.lockListener.prefListener);
        } catch (Exception e) {
            Log.e(TAG,"Got exception registering lockListener: "+e+ " "+(preferenceFragment.lockListener == null));
        }

        mibandStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            final MiBandService.MIBAND_INTEND_STATES state = MiBandService.MIBAND_INTEND_STATES.valueOf(intent.getStringExtra("state"));
            switch (state) {
                case UPDATE_PREF_SCREEN:
                    preferenceFragment.updateMiBandScreen();
                    break;
                case UPDATE_PREF_DATA:
                    preferenceFragment.updateMibandPreferencesData();
                    break;
                }
            }
        };

        UiBasedCollector.onEnableCheckPermission(this);
    }

    @Override
    public void onStop() { // Everything here runs when xDrip is minimized or stopped.
        super.onStop();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            if (!prefs.getBoolean("engineering_mode", false)) { // If engineering mode has been disabled
                try {
                } catch (Exception e) {
                    //
                }
            }
        } catch (Exception e) {
            //
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getMenuInflater().inflate(R.menu.menu_preferences, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void showSearch(MenuItem item) {
        if (JoH.ratelimit("preference-search-button",1)) {
            this.preferenceFragment.showSearchFragment();
        }
    }


    private final SharedPreferences.OnSharedPreferenceChangeListener uiPrefListener = UiBasedCollector.getListener(this);

    private final SharedPreferences.OnSharedPreferenceChangeListener xDripCloudListener = (sharedPreferences, key) -> {
        if (key!= null && key.equals("use_xdrip_cloud_sync")) {
            Pusher.requestReconnect();
            CollectionServiceStarter.restartCollectionServiceBackground();
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(ActivityRecognizedService.prefListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && DexCollectionType.hasBluetooth() && !WholeHouse.isRpi()) {
            LocationHelper.requestLocationForBluetooth(this); // double check!
        }
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(LeFunEntry.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(Cpref.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(MiBandEntry.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(BroadcastService.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(BlueJayEntry.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(uiPrefListener);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(Registry.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(xDripCloudListener);
        LocalBroadcastManager.getInstance(this).registerReceiver(mibandStatusReceiver,
                new IntentFilter(Intents.PREFERENCE_INTENT));
    }

    @Override
    protected void onPause()
    {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(ActivityRecognizedService.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(LeFunEntry.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(Cpref.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(MiBandEntry.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(BroadcastService.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(BlueJayEntry.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(uiPrefListener);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(Registry.prefListener);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(xDripCloudListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mibandStatusReceiver);
        pFragment = null;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(preferenceFragment.lockListener.prefListener);
        } catch (Exception e) {
            //
        }
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        addPreferencesFromResource(R.xml.pref_general);

    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (AllPrefsFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public static boolean isManualTestModeEnabled() {
        return Pref.getBoolean("manual_test_mode_enabled", false);
    }

    public static double getManualTestValue() {
        try {
            return Double.parseDouble(Pref.getString("manual_test_value", "5.5"));
        } catch (Exception e) {
            return 5.5;
        }
    }
}
