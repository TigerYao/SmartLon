import 'package:get/get.dart';
import 'package:smartloan/common/router/app_router.dart';
import 'package:smartloan/pages/home/permission_page.dart';
import 'package:smartloan/pages/login/input_code_page.dart';

import '../../pages/login/input_number_page.dart';

class RoutersConfig {
  static const initial = AppRouters.initial;
  static final List<GetPage> routes = [
    GetPage(name: AppRouters.permission, page: () => const PermissionPage()),
    GetPage(name: AppRouters.signIn, page: () => PhoneNumberPage()),
    GetPage(name: AppRouters.signUpCode, page: () => PhoneCodePage()),
  ];
}
