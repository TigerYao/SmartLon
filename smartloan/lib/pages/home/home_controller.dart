import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:get_storage/get_storage.dart';
import 'package:smartloan/common/base/base_provider.dart';
import 'package:smartloan/common/router/app_router.dart';
import 'package:smartloan/extension/foundation.dart';
import 'package:smartloan/pages/login/login_controller.dart';

const keyToken = 'key_token';
const showPhone = 1;
const showCode = 2;
const showMainPage = 3;

class HomeController extends GetxController with StateMixin {
  final channel = const MethodChannel("smartloan_plugin");
  final GetStorage storageBox = GetStorage();
  final isAccept = false.obs;
  final BaseProvider baseProvider = Get.put(BaseProvider());
  late Map<dynamic, dynamic>? deviceInfo;

  @override
  void onInit() {
    super.onInit();
    Get.lazyPut(() => LoginController());
    isAccept.value = storageBox.read<bool>('accept_permission') ?? false;
    getDeviceInfo();
    channel.setMethodCallHandler((call) async {
      print("callFlutter ${call.arguments}");
        switch(call.method){
          case "onTimeFileCallBack":
            var args = call.arguments as Map<dynamic, dynamic>;
            if(args['result']){
              uploadFile(args['md5'], args['file'], args['orderNo']);
            }
            break;
          case "logout":
            storageBox.remove(keyToken);
            storageBox.erase().then((value){
              isAccept.value = false;
              jumpPage();
            });
            break;
        }
    });
  }

  void jumpPage() {
    if (isLogin()) {
      channel.invokeMethod("loginSuccess", {"token": storageBox.read(keyToken)});
      startWebActivity('http://8.134.38.88:3003');
    } else if (isAccept.isTrue) {
      Get.toNamed(AppRouters.signIn);
    } else {
      Get.toNamed(AppRouters.permission);
    }
  }

  void collectMessage() {
    var data = {
      "action": "timeSDK",
      "id": "0.6656999111432877",
      "data": {
        "orderNo": "0a729c8f-f434-4232-96c4-ba38fc963c5b",
        "userId": "1423538032933470208",
        "isSubmit": false,
        "appList": false,
        "sms": false,
        "exif": false,
        "device": false,
        "contact": false,
        "location": false
      },
      "callback": "webViewToTime"
    };
    channel.invokeMethod('collectMessage', json.encode(data));
  }

  void startWebActivity(url) {
    channel.invokeMethod('startWebActivity', {"url": url});
  }

  void getDeviceInfo() {
    channel.invokeMapMethod('deviceInfo').then((value) {
      deviceInfo = value;
      baseProvider.setHeader(deviceInfo?['afid']);
      storageBox.write("deviceInfo", deviceInfo);
      uploadDeviceInfo();
    });
  }

  bool isLogin() {
    String? token = storageBox.read(keyToken);
    print("login: ${storageBox.getKeys().toString()}....${storageBox.getValues().toString()}");
    return !isEmptyText(token);
  }

  void uploadDeviceInfo() {
    var body = deviceInfo;
    body?.remove('imei');
    baseProvider.httpClient
        .post("user/device/addActive", body: jsonEncode(body))
        .then((value) {
      jumpPage();
    });
  }

  void updateVersion() {
    baseProvider.get('app/getNewVersion', query: {
      'packageName': deviceInfo?['packageName']
    }).then((value) {}, onError: (err) {});
  }

  void uploadFile(String md5, String filePath, String orderNo){
    var params = {
      "md5": md5,
      "orderNo": orderNo,
      "file": MultipartFile(File(filePath), filename: filePath.split("/").last)
    };
    baseProvider.post('/time/upload/zip6in1', FormData(params)).then((value){

    }, onError: (err){

    });
  }
}
