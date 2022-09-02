import 'dart:io';
import 'dart:math';
import 'dart:ui' as ui;

import 'package:decimal/decimal.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

double? _unit;
double? _vUnit;
double? _hUnit;

extension DoubleExtension on double {
  static Map<int, NumberFormat> formatCache = <int, NumberFormat>{};

  static double get safeTop => MediaQueryData.fromWindow(ui.window).padding.top;

  static double get safeBottom => MediaQueryData.fromWindow(ui.window).padding.bottom;

  static double get safeLeft => MediaQueryData.fromWindow(ui.window).padding.left;

  static double get safeRight => MediaQueryData.fromWindow(ui.window).padding.right;

  static double get screenWidth => MediaQueryData.fromWindow(ui.window).size.width;

  static double get screenHeight => MediaQueryData.fromWindow(ui.window).size.height;

  static double get singlePixel {
    final MediaQueryData mediaQuery = MediaQueryData.fromWindow(ui.window);
    return 1.0 / mediaQuery.devicePixelRatio;
  }

  static double? get unit {
    return _unit;
  }

  double get pt {
    if (_unit == null) {
      final MediaQueryData mediaQuery = MediaQueryData.fromWindow(ui.window);
      if (kIsWeb) {
        // 桌面版不做像素转换逻辑，如果以后要做，在这里处理
        _unit = 1.0;
      } else if (Platform.isIOS || Platform.isAndroid) {
        if (mediaQuery.size.width >= 375) {
          _unit = 1.0;
        } else if (mediaQuery.size.width > 0) {
          _unit = mediaQuery.size.width / 375;
        }
      } else {
        _unit = 1.0;
      }
    }
    return this * (_unit ?? 1.0);
  }

  double get vpt {
    if (_vUnit == null) {
      final MediaQueryData mediaQuery = MediaQueryData.fromWindow(ui.window);
      _vUnit = mediaQuery.size.height / 812;
    }
    return this * (_vUnit ?? 1.0);
  }

  double get hpt {
    if (_hUnit == null) {
      final MediaQueryData mediaQuery = MediaQueryData.fromWindow(ui.window);
      _hUnit = mediaQuery.size.width / 375;
    }
    return this * (_hUnit ?? 1.0);
  }

  /// 将double类型转换为[Decimal]类型，如果转换失败则返回值为0的Decimal
  Decimal get decimal => Decimal.tryParse(toString()) ?? Decimal.zero;

  int get decimalPlaces {
    // 最大小数8位
    const int maxDecimal = 8;
    for (int i = 0; i <= maxDecimal; i++) {
      if (this * pow(10, i) == (this * pow(10, i)).truncateToDouble()) {
        return i;
      }
    }
    return maxDecimal;
  }

  /// 截取尾部防止四舍五入
  /// 1.0(0) -> '1'
  /// 1.0(1) -> '1.0'
  /// 1.123456789(5) -> '1.12345'
  String cutStringAsFixed(int fixed) =>
      ((this * pow(10, fixed)).truncateToDouble() / pow(10, fixed)).toStringAsFixed(fixed);

  /// 大数据简写
  /// 123456789.0 -> '123.57M'
  /// 1234.0 -> '1.23K'
  String get bigDecimalFormatString {
    if (abs() >= 1e9) {
      return (this / 1e9).toStringAsFixed(2) + 'B';
    } else if (abs() >= 1e6) {
      return (this / 1e6).toStringAsFixed(2) + 'M';
    } else if (abs() >= 1e3) {
      return (this / 1e3).toStringAsFixed(2) + 'K';
    } else {
      return cutStringAsFixed(3);
    }
  }

  /// 截取有效位，不四舍五入
  String subDotLength(int dotLength) {
    if (isNaN || isInfinite) return '0';

    // 先多保留5位,防止0.999995
    final String info = toStringAsFixed(dotLength + 5);

    if (info.length < 5) {
      return '0';
    }

    final String result = info.substring(0, info.length - 5);
    if (result.endsWith('.')) {
      return result.substring(0, result.length - 1);
    }
    return result;
  }

  int toE2() {
    return (double.parse(subDotLength(2)) * 1e2).round();
  }

  int toE4() {
    return (double.parse(subDotLength(4)) * 1e4).round();
  }

  int toE8() {
    return (double.parse(subDotLength(8)) * 1e8).round();
  }

  String get klineDecimalString {
    if (this > 1e9) {
      return (this / 1e9).toStringAsFixed(1) + 'B';
    } else if (this > 1e6) {
      return (this / 1e6).toStringAsFixed(1) + 'M';
    } else if (this > 10) {
      return toStringAsFixed(2);
    } else if (this > 1) {
      return toStringAsFixed(3);
    }
    return toStringAsFixed(4);
  }

  /// 千分位显示
  String currencyFormat({int? decimalDigits}) {
    final int fixed = decimalDigits ?? 2;
    formatCache[fixed] ??= NumberFormat.currency(locale: 'en_US', symbol: '', decimalDigits: fixed);
    return formatCache[fixed]!.format(this);
  }

  /// 先乘以需要的小数的位数，再ceil, 再除于需要的小数的位数
  double ceilToDecimal(int decimal) {
    return (this * pow(10, decimal)).ceil() / pow(10, decimal);
  }

  /// 先乘以需要的小数的位数，再floor, 再除于需要的小数的位数
  double floorToDecimal(int decimal) {
    return (this * pow(10, decimal)).floor() / pow(10, decimal);
  }
}
