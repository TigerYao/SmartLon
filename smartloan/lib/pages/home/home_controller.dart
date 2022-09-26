import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:get_storage/get_storage.dart';
import 'package:smartloan/common/base/base_provider.dart';
import 'package:smartloan/common/router/app_router.dart';
import 'package:smartloan/extension/foundation.dart';
import 'package:smartloan/pages/login/login_controller.dart';
import 'package:url_launcher/url_launcher.dart';

const keyToken = 'key_token';
const keyVersionCode = 'h5VersionCode';
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
      switch (call.method) {
        case "onTimeFileCallBack":
          var args = call.arguments as Map<dynamic, dynamic>;
          if (args['isSubmit']) {
            uploadFile(args['md5'], args['file'], args['orderNo']);
          }
          break;
        case "logout":
          storageBox.remove(keyToken);
          storageBox.erase().then((value) {
            isAccept.value = false;
            jumpPage();
          });
          break;
      }
    });
  }

  void jumpPage() {
    if (isLogin()) {
      startMainWebActivity();
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

  Future startWebActivity(url) {
    return channel.invokeMethod('startWebActivity', {"url": url});
  }

  void startMainWebActivity({String? token}) {
    channel.invokeMethod("loginSuccess",
        {"token": token ?? storageBox.read(keyToken)}).then((value) {
      startWebActivity('http://8.134.38.88:3003').then((value) {
        if (value == "ok") {
          Future.delayed(const Duration(seconds: 2), () => updateVersion());
        }
      });
    });
  }

  void getDeviceInfo() {
    channel.invokeMapMethod('deviceInfo').then((value) {
      deviceInfo = value;
      baseProvider.setHeader(deviceInfo?['afid']);
      storageBox.write("deviceInfo", deviceInfo);
      String? token = storageBox.read(keyToken);
      if(token != null) baseProvider.setToken(token);
      uploadDeviceInfo();
    });
  }

  bool isLogin() {
    String? token = storageBox.read(keyToken);
    print(
        "login: ${storageBox.getKeys().toString()}....${storageBox.getValues().toString()}");
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
    baseProvider.get('app/getNewVersion',
        query: {'packageName': deviceInfo?['packageName']}).then((value) {
      var data = value.body['data'];
      var code = deviceInfo?['versionCode'];
      if (code != null && data['versionCode'].toString().toInt() > code) {
        channel.invokeMethod("showUpdateDialog", json.encode(data));
      }
    }, onError: (err) {});
  }

  void uploadFile(String md5, String filePath, String orderNo) {
    var params = {
      "md5": md5,
      "orderNo": orderNo,
      "file": MultipartFile(File(filePath), filename: filePath.split("/").last)
    };

    baseProvider.post('/time/upload/zip6in1', FormData(params)).then((value) {
      print("callFlutter value =ok= ${value.hasError}");
      channel.invokeMethod('onTimeUpload', true);
    }, onError: (err) {
      print("callFlutter value == ${err.toString()}");
      channel.invokeMethod('onTimeUpload', false);
    });
  }

  void updateDialog(dynamic data) {
    var forced = data['forcedUpdate'];
    Widget dialogUpdate = Container(
      color: Colors.transparent,
      alignment: Alignment.center,
      padding: EdgeInsets.symmetric(horizontal: 40.pt),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(8.pt), color: Colors.white),
            height: 320.pt,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Image.asset('images/update_top_img.png'),
                Container(
                  padding: EdgeInsets.all(20.pt),
                  alignment: Alignment.topCenter,
                  child: FittedBox(
                    child: Text(
                      'Nueva versión disponible',
                      style: TextStyle(
                        decoration: TextDecoration.none,
                        fontWeight: FontWeight.bold,
                        fontSize: 20.pt,
                        color: const Color(0xff333333),
                      ),
                    ),
                  ),
                ),
                Container(
                  padding: EdgeInsets.symmetric(horizontal: 20.pt),
                  alignment: Alignment.center,
                  child: Text(
                    'Por favor, actualice a la última  versión',
                    style: TextStyle(
                      decoration: TextDecoration.none,
                      fontWeight: FontWeight.bold,
                      fontSize: 16.pt,
                      color: const Color(0xff666666),
                    ),
                  ),
                ),
                GestureDetector(
                  onTap: () {
                    String? link = data?['link'];
                    jumpMarket(link);
                  },
                  child: Container(
                    height: 50.pt,
                    alignment: Alignment.center,
                    margin:
                        EdgeInsets.only(top: 30.pt, left: 30.pt, right: 30.pt),
                    decoration: BoxDecoration(
                        borderRadius: BorderRadius.all(Radius.circular(25.pt)),
                        color: const Color(0xffFF6C6C)),
                    child: Text(
                      'Confirmar',
                      style: TextStyle(
                          decoration: TextDecoration.none,
                          fontSize: 20.pt,
                          color: Colors.white),
                    ),
                  ),
                )
              ],
            ),
          ),
          Offstage(
            offstage: forced,
            child: GestureDetector(
              onTap: () {
                Get.back();
              },
              child: Padding(
                  padding: EdgeInsets.only(top: 20.pt),
                  child: Align(
                    alignment: Alignment.bottomCenter,
                    child: Image.asset(
                      'images/delete_icon.png',
                      width: 32.pt,
                      height: 32.pt,
                    ),
                  )),
            ),
          )
        ],
      ),
    );
    Get.dialog(dialogUpdate, barrierDismissible: !forced).then((value) {
      jumpPage();
    });
  }

  void jumpMarket(String? link) {
    String packageName = deviceInfo?['packageName'];
    if (link == null) {
      _jumpMarket(packageName);
    } else if (link.startsWith('http') == true) {
      launchUrl(Uri.parse(link));
    } else {
      _jumpMarket(link);
    }
  }

  void _jumpMarket(String? name) {
    String browUrl = 'https://play.google.com/store/apps/details?id=$name';
    Uri uri = Uri.parse(browUrl);
    launchUrl(uri, mode: LaunchMode.externalApplication);
  }
}
