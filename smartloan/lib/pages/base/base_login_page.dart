import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:smartloan/common/dialog/dialog.dart';
import 'package:smartloan/extension/foundation.dart';
import 'package:smartloan/pages/login/login_controller.dart';
import 'package:smartloan/widget/roundcheckbox.dart';

abstract class BaseLoginPage extends GetView<LoginController> {
  const BaseLoginPage({this.showBackUp = false, Key? key}) : super(key: key);
  final bool showBackUp;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: false,
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          Container(
            height: double.infinity,
            decoration: const BoxDecoration(color: Colors.white),
            child: Image.asset(
              'images/main_bg.png',
              fit: BoxFit.fill,
            ),
          ),
          WillPopScope(
            onWillPop: showBackUp
                ? () {
                    Get.back();
                    return Future.value(false);
                  }
                : null,
            child: Container(
              padding:
                  EdgeInsets.only(top: 112.pt, left: 27.pt, right: 27.pt),
              child: Column(
                children: [
                  createTop(),
                  createBody(),
                  createBtn(),
                  createAgreement(),
                ],
              ),
            ),
          ),
          Container(
            width: double.infinity,
            height: 50.pt,
            alignment: Alignment.topLeft,
            margin: EdgeInsets.symmetric(vertical: 48.pt, horizontal: 27.pt),
            child: Offstage(
              offstage: !showBackUp,
              child: createHeader(),
            ),
          ),
          Obx(() => Offstage(
                offstage: controller.showLoadingDialog.isFalse,
                child: const LoadingDialog(),
              )),
        ],
      ),
    );
  }

  Widget createHeader() {
    return GestureDetector(
        onTap: () {
          Get.back();
        },
        child: const Icon(
          Icons.arrow_back_ios,
          color: Colors.black,
        ));
  }

  Widget createTop() {
    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 10.5.pt),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
              child: Column(
            mainAxisAlignment: MainAxisAlignment.start,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: EdgeInsets.only(top: 10.pt),
                child: RichText(
                    text: TextSpan(
                      text: 'app_desc_one'.tr,
                      children: [
                        TextSpan(
                          text: 'app_desc_two'.tr,
                          style: TextStyle(
                              fontSize: 22.pt,
                              color: Colors.black,
                              fontWeight: FontWeight.bold),
                        ),
                        TextSpan(
                          text: 'app_desc_three'.tr,
                        ),
                      ],
                      style: TextStyle(
                          fontSize: 20.pt,
                          color: Colors.black,),
                    ),
                    softWrap: true),
              ),
              SizedBox(
                height: 2.pt,
              ),
              Image.asset(
                "images/blue_line.png",
                height: 4.5.pt,
              ),
              SizedBox(
                height: 2.pt,
              ),
              Image.asset(
                "images/red_line.png",
                height: 4.5.pt,
              ),
            ],
          )),
          SizedBox(
            width: 117.pt,
            height: 127.pt,
            child: Image.asset(
              'images/login_tip_img.png',
              fit: BoxFit.fitHeight,
            ),
          )
        ],
      ),
    );
  }

  Widget createBody();

  Widget createBtn();

  Widget createAgreement() {
    return Container(
      width: 320.pt,
      padding: EdgeInsets.only(top: 30.5.pt),
      alignment: Alignment.topLeft,
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            alignment: Alignment.topLeft,
            margin: EdgeInsets.only(top: 3.pt, right: 5.pt),
            width: 15.pt,
            height: 15.pt,
            child: Obx(() => RoundCheckBox(
                  isChecked: controller.isChecked.value,
                  onTap: (bool? isChecked) {
                    controller.isChecked.value = isChecked ?? false;
                  },
                  checkedWidget: Image.asset(
                    'images/checkbox_checked.png',
                    width: 12.pt,
                    height: 12.pt,
                    fit: BoxFit.fill,
                  ),
                  uncheckedWidget: Image.asset(
                    'images/checkbox_uncheck.png',
                    width: 12.pt,
                    height: 12.pt,
                    fit: BoxFit.fill,
                  ),
                )),
          ),
          Expanded(
            child: GestureDetector(
              onTap: () {
                controller.isChecked.value = controller.isChecked.isFalse;
              },
              child: Text.rich(
                TextSpan(
                  text: 'AI continuar,acepta nuestros﹤',
                  style: TextStyle(
                      color: const Color(0xff242B57),
                      fontSize: 11.pt,
                      fontWeight: FontWeight.normal),
                  children: [
                    TextSpan(
                        text: 'Terminos de Servicin',
                        style: TextStyle(
                            color: const Color(0xff242B57),
                            fontSize: 11.pt,
                            fontWeight: FontWeight.bold),
                        recognizer: TapGestureRecognizer()
                          ..onTap = controller.openService),
                    TextSpan(
                      text: '﹥,﹤',
                      style: TextStyle(
                          color: const Color(0xff242B57),
                          fontSize: 11.pt,
                          fontWeight: FontWeight.normal),
                    ),
                    TextSpan(
                        text: 'Politica de privacidad',
                        style: TextStyle(
                            color: const Color(0xff242B57),
                            fontSize: 11.pt,
                            fontWeight: FontWeight.bold),
                        recognizer: TapGestureRecognizer()
                          ..onTap = controller.openProvicyService),
                    TextSpan(
                      text: '﹥y recibe avisos por SMS y correo electronico.',
                      style: TextStyle(
                          color: const Color(0xff242B57),
                          fontSize: 11.pt,
                          fontWeight: FontWeight.normal),
                    ),
                  ],
                ),
              ),
            ),
          )
        ],
      ),
    );
  }
}
