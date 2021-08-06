package com.danjodanjo.flutter_sms_listener.permissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

class PermissionsRequest {
    private int id;
    private Activity activity;
    private String[] permissions;

    PermissionsRequest(int id, String[] permissions, Activity activity) {
        this.id = id;
        this.permissions = permissions;
        this.activity = activity;
    }

    int getId() {
        return this.id;
    }

    @TargetApi(Build.VERSION_CODES.M)
    void execute() {
        this.activity.requestPermissions(this.permissions, this.id);
    }
}

