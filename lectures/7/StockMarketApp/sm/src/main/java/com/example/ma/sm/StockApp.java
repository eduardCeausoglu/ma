package com.example.ma.sm;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.example.ma.sm.net.ClientConnection;
import com.example.ma.sm.service.StockManager;

import javax.annotation.Nullable;
import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public class StockApp extends Application {
  @Inject
  StockManager manager;
  @Inject
  ClientConnection client;
  private Injector injector;

  @Override
  public void onCreate() {
    super.onCreate();
    injector = DaggerInjector.builder()
        .appModule(new AppModule(this)).build();
    injector.inject(this);
    manager.setServerListener(client);

    // https://www.fabric.io/kits/android/crashlytics/summary
    CrashlyticsCore core = new CrashlyticsCore.Builder()
        .disabled(BuildConfig.DEBUG)
        .build();
    Fabric.with(this, new Crashlytics.Builder().core(core).build());
    if (BuildConfig.DEBUG) {
      Timber.plant(new DebugTree());
    } else {
      Timber.plant(new CrashlyticsTree());
    }
    Timber.v("onCreate");
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    if (manager != null)
      manager.cancelCall();
    Timber.v("onTerminate done");
  }

  public StockManager getManager() {
    Timber.v("getManager done");
    return manager;
  }

  public Injector injector() {
    return injector;
  }

  public class CrashlyticsTree extends Timber.Tree {
    private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
    private static final String CRASHLYTICS_KEY_TAG = "tag";
    private static final String CRASHLYTICS_KEY_MESSAGE = "message";

    @Override
    protected void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable t) {
      if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
        return;
      }

      Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
      Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);
      Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

      if (t == null) {
        Crashlytics.logException(new Exception(message));
      } else {
        Crashlytics.logException(t);
      }
    }
  }
}