#!/bin/bash
adb install -r SmsMail-debug-unaligned.apk
adb logcat -s "smsmail:*"
