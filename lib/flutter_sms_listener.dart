import 'dart:async';

import 'package:flutter/services.dart';

class FlutterSmsListener {
  static const EventChannel _channel = EventChannel(
      'com.danjodanjo/flutter_sms_listener', const JSONMethodCodec());
  Stream<SmsMessage>? _messageStream;

  Stream<SmsMessage>? get onSmsReceived {
    if (_messageStream != null) {
      return _messageStream;
    }

    _messageStream = _channel
        .receiveBroadcastStream()
        .where((event) => event is Map<String, dynamic>)
        .map((msgJson) => SmsMessage.fromJson(msgJson));

    return _messageStream;
  }
}

class SmsMessage extends Comparable<SmsMessage> {
  int? id;
  int? threadId;
  String? address;
  String? body;
  bool? read;
  DateTime? date;
  DateTime? dateSent;

  SmsMessage(
      {this.id,
      this.threadId,
      this.address,
      this.body,
      this.read,
      this.date,
      this.dateSent});

  static SmsMessage fromJson(Map<String, dynamic> json) {
    return SmsMessage(
        id: json['id'],
        threadId: json['thread_id'],
        address: json['address'],
        body: json['body'],
        read: json['read'] != null ? (json['read'] == 1) : null,
        date: json['date'] != null
            ? DateTime.fromMillisecondsSinceEpoch(json['date'])
            : null,
        dateSent: json['date_sent'] != null
            ? DateTime.fromMillisecondsSinceEpoch(json['date_sent'])
            : null);
  }

  @override
  int compareTo(SmsMessage other) {
    // if either one of the id is null, compare by date instead
    if (this.id == null || other.id == null) {
      return this.date!.compareTo(other.date!);
    }

    return ((this.id ?? 0) - (other.id ?? 0));
  }
}
