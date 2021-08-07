package com.danjodanjo.flutter_sms_listener;

import java.util.Date;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.danjodanjo.flutter_sms_listener.permissions.Permissions;

import org.json.JSONObject;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.embedding.android.FlutterActivity;

/** FlutterSmsListenerPlugin */
public class FlutterSmsListenerPlugin implements FlutterPlugin, ActivityAware, StreamHandler, PluginRegistry.RequestPermissionsResultListener {

  private EventChannel channel;
  private FlutterPluginBinding flutterPluginBinding;
  private Permissions permissions;

  private BroadcastReceiver receiver;

  private EventSink eventSink;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "com.danjodanjo/flutter_sms_listener", JSONMethodCodec.INSTANCE);
    this.flutterPluginBinding = flutterPluginBinding;
    channel.setStreamHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setStreamHandler(null);
  }

  // ActivityAware implementation
  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    binding.addRequestPermissionsResultListener(Permissions.getRequestsResultsListener());

    this.permissions = new Permissions((FlutterActivity)binding.getActivity());
    binding.addRequestPermissionsResultListener(this);
  }

  @Override
  public void onDetachedFromActivity() {
    this.permissions = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  // Stream handler implementation
  @Override
  public void onListen(Object arguments, EventSink events) {
    Log.d("SmsListener", "Listening....");
    this.receiver = createReceiver(events);
    flutterPluginBinding.getApplicationContext().registerReceiver(this.receiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
    this.eventSink = events;
    this.permissions.checkAndRequestPermission(new String[] {Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, Permissions.RECV_SMS_ID_REQ);
  }

  @Override
  public void onCancel(Object arguments) {
    flutterPluginBinding.getApplicationContext().unregisterReceiver(this.receiver);
    this.receiver = null;
  }

  private BroadcastReceiver createReceiver(final EventSink events) {
    return new BroadcastReceiver() {
      @TargetApi(Build.VERSION_CODES.KITKAT)
      @Override
      public void onReceive(Context context, Intent intent) {
        try {
          SmsMessage[] messages = readMessages(intent);
          if (messages == null) {
            return;
          }

          JSONObject obj = new JSONObject();
          obj.put("address", messages[0].getOriginatingAddress());
          obj.put("date", (new Date()).getTime());
          obj.put("date_sent", messages[0].getTimestampMillis());
          obj.put("read", (messages[0].getStatusOnIcc() == SmsManager.STATUS_ON_ICC_READ) ? 1 : 0);
          obj.put("thread_id", TelephonyCompat.getOrCreateThreadId(context, messages[0].getOriginatingAddress()));

          String body = "";
          for (SmsMessage message: messages) {
            body = body.concat(message.getMessageBody());
          }
          obj.put("body", body);

          events.success(obj);
        } catch (Exception e) {
          Log.d("FlutterSmsListener", e.toString());
        }
      }
    };
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  private SmsMessage[] readMessages(Intent intent) {
    return Telephony.Sms.Intents.getMessagesFromIntent(intent);
  }

  // Request Permission Result Listener
  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode != Permissions.RECV_SMS_ID_REQ) {
      return false;
    }
    boolean isOk = true;
    for (int res: grantResults) {
      if (res != PackageManager.PERMISSION_GRANTED) {
        isOk = false;
        break;
      }
    }
    if (isOk) {
      return true;
    }
    eventSink.endOfStream();
    return false;
  }
}
