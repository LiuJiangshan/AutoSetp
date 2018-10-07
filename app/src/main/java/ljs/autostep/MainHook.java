package ljs.autostep;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    static XSharedPreferences mXSharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
    }

    @Override
    public void initZygote(StartupParam startupParam) {
    }
}
