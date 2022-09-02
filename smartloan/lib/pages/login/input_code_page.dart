import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:smartloan/extension/foundation.dart';
import 'package:smartloan/pages/base/base_login_page.dart';

class PhoneCodePage extends BaseLoginPage {
  PhoneCodePage({Key? key}) : super(showBackUp: true, key: key);
  final TextEditingController codeController = TextEditingController();

  @override
  Widget createBtn() {
    return GestureDetector(
      onTap: () {
        controller.clickCode(codeController.text);
      },
      child: Container(
        margin: EdgeInsets.only(top: 26.5.pt),
        width: 320.pt,
        height: 52.pt,
        alignment: Alignment.center,
        decoration: const BoxDecoration(
            image: DecorationImage(
                image: AssetImage('images/red_btn.png'), fit: BoxFit.fill)),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'SOLICITAR AHORA!',
              style: TextStyle(
                  color: Colors.white,
                  fontSize: 17.pt,
                  fontWeight: FontWeight.bold),
            ),
            Image.asset(
              'images/press_icon.png',
              width: 28.pt,
              height: 13.pt,
            )
          ],
        ),
      ),
    );
  }

  Widget createInputCode() {
    return Padding(
      padding: EdgeInsets.only(top: 100.pt, left: 35.pt, right: 35.pt),
      child: TextField(
        controller: codeController,
        autofocus: true,
        inputFormatters: [
          LengthLimitingTextInputFormatter(7,
              maxLengthEnforcement: MaxLengthEnforcement.enforced),
          FilteringTextInputFormatter.digitsOnly,
          TextInputFormatter.withFunction(
              (oldValue, newValue) => newValue.text.addSeparator(3))
        ],
        textAlign: TextAlign.center,
        keyboardType: TextInputType.number,
        decoration: InputDecoration(
          hintText: 'Codigo de verificacion',
          hintStyle: TextStyle(
            color: const Color(0xff484E76),
            fontSize: 14.pt,
          ),
          suffixIcon: SizedBox(
            width: 80.pt,
            height: 30.pt,
            child: GestureDetector(
                onTap: () {
                  controller.getVerifyCode();
                },
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    Image.asset(
                      'images/line_verti.png',
                      height: 12.5.pt,
                      width: 1.pt,
                    ),
                    Obx(() => Text(
                          controller.remainTimeStr.value,
                          style: TextStyle(
                              fontSize: 14.pt, color: const Color(0xff222A56)),
                        ))
                  ],
                )),
          ),
        ),
      ),
    );
  }

  Widget createTip() {
    return Padding(
      padding: EdgeInsets.only(top: 20.pt),
      child: Obx(() => controller.remainTimeStr.value != "Conseguir"
          ? const Text(
              "Le llamaremos dentro de 60 para notificarle el código de ",
              style: TextStyle(fontSize: 13, color: Colors.black),
            )
          : Text.rich(TextSpan(
              text: "  ¿No conseguió el código?",
              children: [
                TextSpan(
                    text: "  Llámame ",
                    style: TextStyle(fontSize: 15.pt, fontWeight: FontWeight.bold),
                    recognizer: TapGestureRecognizer()
                      ..onTap = () {
                        controller.getVerifyCode(notifyType: 2);
                      })
              ],
              style: TextStyle(fontSize: 13.pt, color: Colors.black)))),
    );
  }

  @override
  Widget createBody() {
    return Column(
      children: [createInputCode(), createTip()],
    );
  }
}
