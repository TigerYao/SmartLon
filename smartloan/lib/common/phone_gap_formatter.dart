import 'dart:math';

import 'package:flutter/services.dart';

import 'utils.dart';

class CreditCardFormatter extends TextInputFormatter {
  final String separator;
  final int gap;
  final int maxFilterLength;

  CreditCardFormatter({this.separator = ' ', this.gap = 3, this.maxFilterLength = 8});

  @override
  TextEditingValue formatEditUpdate(
      TextEditingValue oldValue, TextEditingValue newValue) {
    return textManipulation(
      oldValue,
      newValue,
      textInputFormatter: FilteringTextInputFormatter.digitsOnly,
      formatPattern: (String filteredString) {
        int offset = 0;
        StringBuffer buffer = StringBuffer();
        for (int i = min(gap, filteredString.length);
        i <= filteredString.length;
        i += min(gap, max(1, filteredString.length - i))) {
          buffer.write(filteredString.substring(offset, i));
          if (i < maxFilterLength) {
            buffer.write(separator);
          }
          offset = i;
        }
        return buffer.toString();
      },
    );
  }
}
