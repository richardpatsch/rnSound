package com.rnsound;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

public class MyTaskService extends HeadlessJsTaskService {
    @Override
    protected
    @Nullable
    HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Log.d("MyTaskService", "task invoked in java");
        Bundle extras = intent.getExtras();

        if (extras != null) {
            return new HeadlessJsTaskConfig(
                    "SomeTaskName",
                    Arguments.fromBundle(extras),
                    5000);
        }
        return null;
    }
}