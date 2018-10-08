package ljs.autostep;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    static XSharedPreferences mXSharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);

    static final String YUEDONG = "com.yuedong.sport";

    static String userId = mXSharedPreferences.getString("userid", "");
    static Context context = null;
    static long addValue;

    public void bindReceicver() {
        try {
            context = (Context) XposedHelpers.callMethod(XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread", new Object[0]), "getSystemContext", new Object[0]);
        } catch (Throwable e) {
            e.printStackTrace();
            context = null;
        }
    }

    public static void initData() {
        mXSharedPreferences.reload();
        mXSharedPreferences.makeWorldReadable();
        addValue = Long.valueOf(mXSharedPreferences.getString("addvalue", "0")).intValue();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (context == null) bindReceicver();
        if (YUEDONG.equals(loadPackageParam.packageName)) {
            try {
                Class<?> openSignEL = XposedHelpers.findClass("com.yuedong.common.utils.OpenSign", loadPackageParam.classLoader);
                if (openSignEL != null) {
                    handleYDAddNum(openSignEL);
                }
                Class<?> ydConfigs = XposedHelpers.findClass("com.yuedong.sport.common.Configs", loadPackageParam.classLoader);
                if (ydConfigs != null) {
                    handleYDGetSignkey(ydConfigs);
                }
                Class<?> Account = XposedHelpers.findClass("com.yuedong.sport.controller.account.Account", loadPackageParam.classLoader);
                if (Account != null) {
                    handleYDGetXyy(Account);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void handleYDAddNum(Class<?> openSignEL) {
        XposedBridge.hookAllMethods(openSignEL, "makeSig", new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                MainHook.initData();
                HashMap<String, String> hashMap = (HashMap<String, String>) param.args[2];
                if (hashMap != null) {
                }
                if (MainHook.addValue >= 1 && TextUtils.equals("/sport/report_runner_info_step_batch", param.args[1].toString()) && hashMap != null && hashMap.containsKey("steps_array_json")) {
                    JSONArray jsonArray = new JSONArray(hashMap.get("steps_array_json"));
                    if (jsonArray.length() == 1) {
                        if (!TextUtils.isEmpty(MainHook.userId)) {
                            hashMap.put("user_id", MainHook.userId);
                            hashMap.put("client_user_id", MainHook.userId);
                        }
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        jsonObject.remove("step");
                        jsonObject.put("step", MainHook.addValue);
                        hashMap.put("steps_array_json", "[" + jsonObject.toString() + "]");
                        XposedBridge.log("newhashMap==" + hashMap.toString());
                        MainHook.addValue = 0;
                        Intent intent = new Intent("com.anjoyo.xyl.run.SETTING_CHANGED");
                        intent.putExtra("content", "0");
                        intent.putExtra("type", 1);
                        if (MainHook.context != null) {
                            MainHook.context.sendBroadcast(intent);
                        }
                    }
                }
            }
        });
    }

    public void handleYDGetSignkey(Class<?> ydConfigs) {
        XposedBridge.hookAllMethods(ydConfigs, "getSignkey", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String signkey = param.getResult().toString();
                Intent intent = new Intent("com.anjoyo.xyl.run.yd_info");
                intent.putExtra("action", 0);
                intent.putExtra("signkey", signkey);
                if (MainHook.context != null) MainHook.context.sendBroadcast(intent);
            }
        });
    }

    public void handleYDGetXyy(Class<?> ydAccount) {
        XposedBridge.hookAllMethods(ydAccount, "xyy", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Integer uid = (Integer) param.thisObject.getClass().getDeclaredMethod("uid", new Class[0]).invoke(param.thisObject, new Object[0]);
                String xyy = param.getResult().toString();
                Intent intent = new Intent("com.anjoyo.xyl.run.yd_info");
                intent.putExtra("action", 1);
                intent.putExtra("uid", uid);
                intent.putExtra("xyy", xyy);
                if (MainHook.context != null) {
                    MainHook.context.sendBroadcast(intent);
                    return;
                }
                Context context = (Context) XposedHelpers.callMethod(XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread", new Object[0]), "getSystemContext", new Object[0]);
                if (context != null) {
                    context.sendBroadcast(intent);
                }
            }
        });
    }
}
