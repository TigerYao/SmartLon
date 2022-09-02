import 'dart:math' as math;

import 'package:decimal/decimal.dart';

import 'int.dart';

extension DecimalExt on Decimal {
  /// 当前Decimal为0
  bool get isZero => this == Decimal.zero;

  /// 当前Decimal不为0
  bool get isNotZero => !isZero;

  /// 转换Decimal为[fractionDigits]指定精度的String，直接丢弃精度之后的值，不会发生四舍五入。
  ///
  /// 默认不丢弃小数部分无效的0，例如`(12.3400.decimal).toStringByFixed(4)` => 12.3400。
  /// 如果你希望删除结果最后的无效0，可以设置[cutInvalidZero]=true，例如`(12.3400.decimal).toStringByFixed(4, true)` => 12.34。
  String cutStringAsFixed(int fractionDigits, [bool cutInvalidZero = false]) {
    assert(fractionDigits >= 0);
    if (fractionDigits == 0) {
      return truncate().toStringAsFixed(fractionDigits);
    }
    // 先将Decimal乘以10的fractionDigits次方，然后丢弃小数部分，再除以10的fractionDigits次方
    final Decimal times = (math.pow(10, fractionDigits) as int).decimal;
    Decimal result = this * times;
    result = result.truncate() / times;
    String dcm = result.toStringAsFixed(fractionDigits);

    if (!cutInvalidZero) {
      return dcm;
    }

    // 如果包含点，则删除后面无效的0和.
    while (dcm.contains('.') && (dcm.endsWith('0') || dcm.endsWith('.'))) {
      dcm = dcm.substring(0, dcm.length - 1);
    }

    return dcm;
  }
}
