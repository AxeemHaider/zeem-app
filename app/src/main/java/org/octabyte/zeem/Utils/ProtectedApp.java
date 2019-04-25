package org.octabyte.zeem.Utils;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.exoplayer2.util.Util;

import org.octabyte.zeem.R;

import java.util.Arrays;
import java.util.List;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

public class ProtectedApp {

    private static List<Intent> POWERMANAGER_INTENTS = Arrays.asList(
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart"))
    );

    public static void startPowerSaverIntent(final Context context) {
        SharedPreferences settings = context.getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        boolean skipMessage = settings.getBoolean("skipProtectedAppCheck", false);
        if (!skipMessage) {
            final SharedPreferences.Editor editor = settings.edit();
            boolean foundCorrectIntent = false;
            for (final Intent intent : POWERMANAGER_INTENTS) {
                if (isCallable(context, intent)) {
                    foundCorrectIntent = true;

                    try {
                        new AlertDialog.Builder(context)
                                .setTitle(Build.MANUFACTURER + " Energy Saver")
                                .setMessage(String.format("%s requires to be enabled in 'Energy Saver' to function properly. If you already added ignore this message.", context.getString(R.string.app_name)))
                                .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            context.startActivity(intent);
                                        } catch (Exception e) {
                                            Toast.makeText(context, "Unable to open Setting, Please do it manualy", Toast.LENGTH_LONG).show();
                                            editor.putBoolean("skipProtectedAppCheck", true);
                                            editor.apply();
                                            e.printStackTrace();
                                        }
                                    }
                                })
                                .setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        editor.putBoolean("skipProtectedAppCheck", true);
                                        editor.apply();
                                    }
                                })
                                .show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            if (!foundCorrectIntent) {
                editor.putBoolean("skipProtectedAppCheck", true);
                editor.apply();
            }
        }
    }

    private static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

}
