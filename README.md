# flutter_sms_listener

A plugin to listen incoming sms in Android. This package is an extracted SmsReceiver class from a full blown plugin called [sms](https://pub.dev/packages/sms). 

Main reason is original plugin is no longer maintained and therefore a null-safety version need to be made.

Credits to original [author](https://github.com/babariviere) for providing the base code.

## What this is and What this isn't about
This plugin uses android's Telephony and prompts for user's permission to access messages. 

This is NOT a plugin that depend on third party API such as Google SMS Retriever API which bypass permission. If you wish to use such an API, consider [android_sms_retriever](https://pub.dev/packages/android_sms_retriever) package.

## Getting Started

Adding package to project

```yaml
dependencies:
    flutter_sms_listener: ^0.1.1
```

Import

```dart
import 'package:flutter_sms_listener/flutter_sms_listener';
```

## Usage

```dart
void main() {
    FlutterSmsListener smsListener = FlutterSmsListener();

    smsListener.onSmsReceived((SmsMessage) {
        // Do something with sms message
    });
}
```

