import 'package:decimal/decimal.dart';
String lastString = "";
bool isEmptyText(String? str) {
  return str == null || str.isEmpty;
}

extension StringExtension on String {
  int toInt() {
    return Decimal.parse(this).toInt();
  }

  String setSeparator(int gap, {String separator = " "}) {
    ///移除了分隔符
    var removeSeparator = replaceAll(separator, "");
    var list = removeSeparator.split("");
    int separatorCount = 0;
    for (var i = 0; i < removeSeparator.length; i = i + gap) {
      if (i == 0) continue;
      if (i + separatorCount > 8) break;
      list.insert(i + separatorCount, separator);
      separatorCount++;
    }
    var endText = list.join("");
    return endText;
  }
}
