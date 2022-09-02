import 'package:decimal/decimal.dart';
import 'package:smartloan/extension/double.dart';


Decimal _e4Decimal = Decimal.parse('1e4');
Decimal _e6Decimal = Decimal.parse('1e6');
Decimal _e8Decimal = Decimal.parse('1e8');

extension IntExtension on int {
  double get pt => toDouble().pt;
  double get vpt => toDouble().vpt;
  double get hpt => toDouble().hpt;
  String get bigDecimalFormatString => toDouble().bigDecimalFormatString;

  /// 将int类型转换为[Decimal]类型，如果转换失败则返回值为0的Decimal
  Decimal get decimal => Decimal.fromInt(this);

  /// 将int类型转换为[Decimal]类型，并将其除以1e4，对应instrument中的E4类价格
  Decimal get decimalDivE4 => decimal / _e4Decimal;

  /// 将int类型转换为[Decimal]类型，并将其除以1e6，对应instrument中的E6类价格
  Decimal get decimalDivE6 => decimal / _e6Decimal;

  /// 将int类型转换为[Decimal]类型，并将其除以1e8，对应instrument中的E8类价格
  Decimal get decimalDivE8 => decimal / _e8Decimal;
}