import 'dart:async';
import 'dart:collection';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:get/get.dart';
import 'package:smartloan/common/router/app_router.dart';
import 'package:smartloan/extension/foundation.dart';
import 'package:smartloan/pages/home/home_controller.dart';

class LoginController extends GetxController {
  HomeController homeController = Get.find();
  Timer? _timer;
  final remainTimeStr = '60s'.obs;
  final isChecked = true.obs;
  late String mobileNumber;
  int type = 0; //type为1（登录），反之则type为2（注册）；
  final showLoadingDialog = false.obs;

  String? isRightCode(String? words) {
    String? result;
    if (words == null || words.isEmpty) {
      result = 'Código de verificación vacío';
    } else if (words.length != 6) {
      result = 'Codigo de verificacion invalido, envia nuevamente';
    }
    return result;
  }

  void openService() {
    homeController.startWebActivity('http://8.134.38.88:3003/#/termsCondition');
  }

  void openProvicyService() {
    homeController.startWebActivity('http://8.134.38.88:3003/#/provicy');
  }

  void clickPhone(String? phoneNum) {
    String result = '';
    if (phoneNum == null || phoneNum.isEmpty) {
      result = 'número telefónico vacío';
    } else if (!GetUtils.isPhoneNumber(phoneNum)) {
      result = 'Formato erróneo de número telefónico';
    }
    if (!isEmptyText(result)) {
      Fluttertoast.showToast(msg: result);
    } else {
      mobileNumber = phoneNum!.replaceAll(" ", '');
      _checkTelephone(mobileNumber);
    }
  }

  void clickCode(String? code) {
    if (isChecked.isFalse) {
      Fluttertoast.showToast(msg: "no aceptar el término y condiciones!");
      return;
    }
    code = code?.replaceAll(" ", '');
    String? result = isRightCode(code);
    if (!isEmptyText(result)) {
      Fluttertoast.showToast(msg: result!);
    } else {
      if (type == 1) {
        login(code!, true);
      } else {
        register(code!);
      }
    }
  }

  void startCountDown(int time) {
    if (_timer == null || !_timer!.isActive) {
      _startCountDown(time);
    }
  }

  void _startCountDown(int time) {
    // 重新计时的时候要把之前的清除掉
    if (_timer != null) {
      if (_timer!.isActive) {
        _timer!.cancel();
        _timer = null;
      }
    }
    if (time <= 0) {
      return;
    }
    var countTime = time;
    const repeatPeriod = Duration(seconds: 1);
    _timer = Timer.periodic(repeatPeriod, (timer) {
      if (countTime <= 0) {
        timer.cancel();
        remainTimeStr.value = 'Conseguir';
        _timer?.cancel();
        _timer = null;
        return;
      }
      countTime--;
      remainTimeStr.value = '${countTime}s';
    });
  }

  void _checkTelephone(String mobile) {
    showLoading(true);
    homeController.baseProvider.get('security/existsByMobile',
        query: {'mobile': mobile}).then((value) {
      showLoading(false);
      if (value.isOk && value.body['code'] == 0) {
        var isHas = value.body['data']['existed'] == true;
        type = isHas ? 1 : 2;
        Get.toNamed(AppRouters.signUpCode);
        getVerifyCode();
      } else {
        Fluttertoast.showToast(msg: value.body['msg']);
      }
    }, onError: (err) {
      Fluttertoast.showToast(msg: err.toString());
      showLoading(false);
    });
  }

  void getVerifyCode({int notifyType = 1}) {
    if (_timer != null) return;
    showLoading(true);
    homeController.baseProvider.post('security/getVerifyCode', {
      "mobile": mobileNumber,
      "type": type,
      "androidId": homeController.deviceInfo?['androidId'],
      "versionCode": homeController.deviceInfo?['versionCode'],
      "notifyType": notifyType
    }).then((value) {
      showLoading(false);
      startCountDown(59);
    }, onError: (err) {
      showLoading(false);
    });
  }

  Future<bool> clickBack() {
    Get.back();
    return Future.value(false);
  }

  void showLoading(bool show) {
    showLoadingDialog.value = show;
  }

  void login(String verifyCode, bool verified) {
    showLoading(true);
    Map device = homeController.deviceInfo ?? HashMap<String, dynamic>();
    device['mobile'] = mobileNumber;
    device['verifyCode'] = verifyCode;
    device['verified'] = verified;
    FormData formData = FormData(device.map((key, value) {
      return MapEntry(key.toString(), value);
    }));
    homeController.baseProvider
        .post('security/login', formData)
        .then(_loginSuccess, onError: _loginFail);
  }

  void register(String verifyCode) {
    showLoading(true);
    Map device = homeController.deviceInfo ?? HashMap<String, dynamic>();
    device['mobile'] = mobileNumber;
    device['verifyCode'] = verifyCode;
    device['verified'] = true;
    homeController.baseProvider
        .post('security/register', device)
        .then(_loginSuccess, onError: _loginFail);
  }

  void _loginSuccess(Response<dynamic> value) {
    showLoading(false);
    if (value.isOk) {
      var data = value.body;
      if (data['code'] == 0) {
        var token = data['data']['token'];
        homeController.storageBox.write(keyToken, token);
        homeController.storageBox.write('userId', data['data']['userId']);
        Get.back();
        homeController.channel.invokeMethod("loginSuccess", {"token": token}).then((value) =>  homeController.startWebActivity('http://8.134.38.88:3003'));
      } else {
        Fluttertoast.showToast(msg: data['msg']);
      }
    }
  }

  void _loginFail(err) {
    showLoading(false);
  }
}
