package com.example.vault;

import android.app.Application;

import com.example.vault.util.ServiceLocator;

public class VaultApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ServiceLocator.init(this);
    }
}
