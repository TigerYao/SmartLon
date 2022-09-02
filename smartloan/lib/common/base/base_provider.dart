import 'package:get/get.dart';

const serviceHost = "http://8.134.38.88:3003/api/";
class BaseProvider extends GetConnect{
  @override
  void onInit() {
    httpClient.baseUrl = serviceHost;
    httpClient.addRequestModifier<void>((request){
      request.headers['packageName'] = 'com.mmt.smartloan';
      request.headers['appName'] = 'SmartLoan';
      request.headers['lang'] = 'es';
      return request;
    });
    httpClient.addResponseModifier((request, response) {
      printError(info: "${request.url.path} \n ${response.body.toString()}");
      return response;
    });
  }

  void setHeader(String? afid){
    if(afid == null) return;
    httpClient.addRequestModifier<void>((request){
      request.headers['afid'] = afid;
      return request;
    });
  }

  void setToken(String token){
    httpClient.addAuthenticator<void>((request) {
      request.headers['Authorization'] = 'Bearer$token';
      return request;
    });
  }
}