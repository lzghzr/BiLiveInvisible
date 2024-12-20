package io.github.lzghzr.biliveinvisible.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class cHook extends mHook {
  public cHook(XC_LoadPackage.LoadPackageParam lpparam) {
    super(lpparam);
  }

  @Override
  protected void userApi() throws Throwable {
    XposedHelpers.findAndHookMethod(
        "com.bilibili.bililive.api.user.UserApi",
        lpparam.classLoader,
        "d",
        long.class,
        int.class,
        int.class,
        int.class,
        int.class,
        int.class,
        String.class,
        "kotlin.coroutines.Continuation",
        new XC_MethodHook() {
          @Override
          protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            param.args[5] = 1;
          }
        });
  }
}