import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:smartloan/common/dialog/dialog.dart';
import 'package:smartloan/pages/home/home_controller.dart';

class HomePage extends StatelessWidget {
  HomePage({Key? key}) : super(key: key);
  final HomeController controller = Get.put(HomeController());

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: LoadingDialog(),
    );
  }
}
