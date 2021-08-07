import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:intl/intl.dart';
import 'package:flutter_sms_listener/flutter_sms_listener.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static const String LISTEN_MSG = 'Listening to sms...';
  static const String NEW_MSG = 'Captured new message!';
  String _status = LISTEN_MSG;

  FlutterSmsListener _smsListener = FlutterSmsListener();
  List<SmsMessage> _messagesCaptured = <SmsMessage>[];

  final _dateFormat = DateFormat('E, ').add_jm();

  @override
  void initState() {
    super.initState();

    if (!Platform.isAndroid) {
      return;
    }

    WidgetsBinding.instance!.addPostFrameCallback((timeStamp) {
      _beginListening();
    });
  }

  void _beginListening() {
    _smsListener.onSmsReceived!.listen((message) {
      _messagesCaptured.add(message);

      setState(() {
        _status = NEW_MSG;
      });

      Future.delayed(Duration(seconds: 5)).then((_) {
        setState(() {
          _status = LISTEN_MSG;
        });
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Sms Listener'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              SizedBox(
                height: 12,
              ),
              Text(_status),
              _messagesCaptured.isEmpty
                  ? Center(
                      child: Text('No message found'),
                    )
                  : ListView.separated(
                      itemCount: _messagesCaptured.length,
                      itemBuilder: (context, index) => ListTile(
                        contentPadding: EdgeInsets.all(4),
                        title: Text(
                          _messagesCaptured[index].address ?? '',
                          style: TextStyle(fontSize: 14, color: Colors.black),
                        ),
                        subtitle: Text(
                          _messagesCaptured[index].body ?? '',
                          style: TextStyle(fontSize: 12, color: Colors.black87),
                          overflow: TextOverflow.ellipsis,
                        ),
                        trailing: Text(_dateFormat.format(
                            _messagesCaptured[index].date ?? DateTime.now())),
                      ),
                      shrinkWrap: true,
                      separatorBuilder: (context, _) => SizedBox(
                        height: 8,
                      ),
                    ),
            ],
          ),
        ),
      ),
    );
  }
}
