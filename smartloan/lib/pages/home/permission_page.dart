import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:smartloan/extension/foundation.dart';
import 'package:smartloan/pages/home/home_controller.dart';

class PermissionPage extends GetView<HomeController> {
  const PermissionPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey,
      body: Container(
        margin: EdgeInsets.only(left: 20.pt, right: 20.pt, top: 48.pt,bottom: 20.pt),
        padding: EdgeInsets.symmetric(horizontal: 20.pt, vertical: 20.pt),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.all(Radius.circular(10.pt)),
          color: Colors.white,
        ),
        child: ListView(
          children: [
            Align(
              alignment: Alignment.topCenter,
              child: Image.asset(
                "images/permission_head_img.png",
                width: 67.5.pt,
                height: 57.pt,
              ),
            ),
            title('SMS'),
            content(
                'Recopilar y monitorear los mensajes de texto financieros y no personales para obtener detalles y montos de transacciones. Se utilizarán para valorar la caificación y evaluar el riesgo del cliente. Otros mensajes no serán evaluados. Verificamos y rastreamos las transacciones financieras de los usuarios analizando sus mensajes en segundo plano y tomamos decisiones inteligentes sobre los límites de crédito basados en la evaluación de su presupuesto de gastos y capacidad de pago. No recopilaremos, leeremos ni almacenaremos sus mensajes de texto personales de su bandeja de entrada h5.mxdinero.com'),
            title('Lista de contactos'),
            content(
                'Cuando nos conceda permisos de libreta de direcciones, recopilaremos todos sus contactos de libreta de direcciones, incluyendo el nombre del contacto telefónico, el número de teléfono, la fecha de adición del contacto.Para detectar materiales de referencia fiables y realizar análisis de riesgos. Al mismo tiempo, la información de contacto se utilizará para rellenar el formulario de solicitud y la información de contacto se rellenará automáticamente.Además, esta información se utilizará para servicios antifraude. La información de contacto se cifrará y se cargará en nuestro servidor, la dirección es h5.mxdinero.com que solo se utiliza para servicios antifraude y evaluación crediticia. No venderemos, intercambiaremos ni alquilaremos sus comunicaciones a ningún tercero.'),
            title('Aplicaciones instaladas'),
            content(
                'También leeremos una lista de aplicaciones instaladas en su dispositivo, incluyendo el nombre de la aplicación, el tiempo de instalación, la versión instalada, etc. Esto se utilizará para detectar la presencia de malware, trampas, manteniendo así la seguridad del entorno del sistema móvil y el servicio de préstamo.La dirección del servidor es h5.mxdinero.com, que solo se utiliza para servicios antifraude y evaluación crediticia. No venderemos, intercambiaremos ni alquilaremos la información de su lista de aplicaciones a ningún tercero.'),
            title('Ubicació'),
            content(
                'Necesitamos la autorización de ubicación de su dispositivo para recopilar información de ubicación, que incluye información relacionada con la ubicación, como el método de ubicación del dispositivo del usuario, la hora de ubicación, la longitud, la latitud, la ubicación, el código de área de ubicación, etc.Esto se utilizará para confirmar su calificación crediticia y aumentar la seguridad de su cuenta a través de la información de su ubicación. Le notificaremos de inmediato cuando se detecte una anomalía. La información de ubicación se cifrará y se cargará en nuestro servidor en h5.mxdinero.com, que solo se utiliza para servicios antifraude y evaluación crediticia. No venderemos, intercambiaremos ni alquilaremos su información de ubicación a ningún tercero.'),
            title('Almacenamiento'),
            content(
                'Para toda la información recopilada, la almacenaremos en el servidor de Tala Dinero de una manera altamente protegida. La dirección del servidor es h5.mxdinero.com. No venderemos, intercambiaremos ni alquilaremos su información a ningún tercero.'),
            title('Cámara'),
            content(
                'Utilizar cámaras para tomar los documentosy/ o las fotografías necesarios para el proceso de solicitud y evaluación.'),
            title('Información de dispositivo'),
            content(
                'Permiso para recopilar y monitorear información específica sobre su dispositivo, incluido el nombre de su dispositivo, modelo, configuración de región e idioma, código de identificación del dispositivo, información de hardware y software del dispositivo, estado, hábitos de uso, IMEI y número de serie y otros identificadores únicos de dispositivo para poder identificar de forma única el Dispositivo y así asegurarse que los dispositivos no autorizados no puedan actuar en su nombre, previniendo cualquier tipo de fraude.El dispositivo de información de ubicación se cifrará y se cargará en nuestro servidor h5.mxdinero.com, que solo se utiliza para servicios antifraude y evaluación crediticia. No venderemos, comercializamos o alquilamos la información de su dispositivo a terceros.'),
            content(
                'Para evaluar su elegibilidad y acelerar los pagos de su préstamo, necesitamos estas licencias. La evaluación de riesgos requiere toda la información. Esperamos que apruebe estos permisos, de lo contrario no podemos evaluar el riesgo de su préstamo.Solo cuando usted esté autorizado, recopilaremos la siguiente información de su dispositivo cuando solicite un préstamo. No recopilaremos información si usted no lo permite.Nuestro seguimiento de datos es un servidor con alta seguridad informática y certificados SSL que garantiza una conectividad segura y protege los datos personales recopilados de la corrupción, pérdida, cambio o uso, acceso o procesamiento no autorizados. '),
            GestureDetector(
              onTap: () {
               controller.storageBox.write('accept_permission', true);
               controller.isAccept.value = true;
               controller.jumpPage();
              },
              child: Container(
                margin: EdgeInsets.symmetric(vertical: 10.pt),
                alignment: Alignment.center,
                width: 320.pt,
                height: 52.pt,
                decoration: const BoxDecoration(
                    image: DecorationImage(
                        image: AssetImage('images/red_btn.png'),
                        fit: BoxFit.fill)),
                child: Text(
                  'ACEPTO',
                  style: TextStyle(
                      color: Colors.white,
                      fontSize: 17.pt,
                      fontWeight: FontWeight.bold),
                ),
              ),
            )
          ],
        ),
      ),
    );
  }

  Widget title(String title) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          title,
          style: TextStyle(
              color: Colors.black,
              fontSize: 12.pt,
              fontWeight: FontWeight.bold),
        ),
        Image.asset(
          "images/checkbox_checked.png",
          width: 12.pt,
          height: 12.pt,
          fit: BoxFit.scaleDown,
        )
      ],
    );
  }

  Widget content(String content) {
    return Padding(
      padding: EdgeInsets.symmetric(vertical: 10.pt),
      child: Text(
        content,
        style: TextStyle(fontSize: 10.pt, color: const Color(0xff5A5A5B)),
      ),
    );
  }
}
