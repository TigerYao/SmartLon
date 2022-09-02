package com.mmt.smartloan.bridge

data class JSMessage(
    val action: String?,
    val callback: String?,
    var data: Any?,
    val id: String?,
    var msg: String? = "",
    var result: String? = "OK"
)

data class Data(
    var token: String?,
)