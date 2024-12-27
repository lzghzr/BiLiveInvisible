package io.github.lzghzr.biliveinvisible.hooks;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class cHook extends mHook {

  public cHook(XC_LoadPackage.LoadPackageParam lpparam) {
    super(lpparam, "国内版");
  }
}