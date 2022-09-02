import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:get_storage/get_storage.dart';
import 'package:smartloan/common/router/router_config.dart';
import 'package:smartloan/location/message.dart';
import 'package:smartloan/pages/home/home_controller.dart';
import 'package:smartloan/pages/home/home_page.dart';

void main() async {
  await GetStorage.init();
  runApp(GetMaterialApp(
    navigatorKey: Get.key,
    locale: Get.deviceLocale,
    translations: Message(),
    fallbackLocale: const Locale('en', 'US'),
    debugShowCheckedModeBanner: false,
    title: 'title'.tr,
    getPages: RoutersConfig.routes,
    initialRoute: RoutersConfig.initial,
    home: HomePage(),
  ));
  if (Platform.isAndroid) {
    SystemUiOverlayStyle systemUiOverlayStyle = const SystemUiOverlayStyle(
        statusBarColor: Colors.transparent,
        systemNavigationBarColor: Colors.transparent);
    SystemChrome.setSystemUIOverlayStyle(systemUiOverlayStyle);
  }
}
