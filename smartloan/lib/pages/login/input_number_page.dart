import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:smartloan/extension/foundation.dart';
import 'package:smartloan/pages/base/base_login_page.dart';

class PhoneNumberPage extends BaseLoginPage {
  PhoneNumberPage({Key? key}) : super(key: key);
  final TextEditingController editingController = TextEditingController();

  @override
  Widget createBtn() {
    return GestureDetector(
      onTap: () {
        controller.clickPhone(editingController.text);
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

  Widget createInput() {
    return Padding(
      padding: EdgeInsets.only(top: 100.pt, left: 35.pt, right: 35.pt),
      child: TextField(
        autofocus: true,
        controller: editingController,
        inputFormatters: [
          LengthLimitingTextInputFormatter(12,
              maxLengthEnforcement: MaxLengthEnforcement.enforced),
          FilteringTextInputFormatter.digitsOnly,
          TextInputFormatter.withFunction(
              (oldValue, newValue) => newValue.text.addSeparator(3))
        ],
        textAlign: TextAlign.center,
        keyboardType: TextInputType.number,
        decoration: InputDecoration(
          hintText: 'numero de telefono',
          hintStyle: TextStyle(
            color: const Color(0xff484E76),
            fontSize: 14.pt,
          ),
          icon: Image.asset(
            'images/sign_head_icon.png',
            width: 18.pt,
            height: 20.pt,
          ),
          prefixIcon: SizedBox(
            width: 50.pt,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                GestureDetector(
                    onTap: () {},
                    child: Text(
                      '+52',
                      style: TextStyle(
                          fontSize: 14.pt, color: const Color(0xff222A56)),
                    )),
                Image.asset(
                  'images/line_verti.png',
                  height: 12.5.pt,
                  width: 1.pt,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  Widget createBody() {
    return createInput();
  }
}
