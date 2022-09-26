package com.mmt.smartloan.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

//自定义方法在下面
fun Uri.getRealPathFromUri(context: Context): String? {

    val scheme = scheme //得到Uri的scheme

    var realPath: String? = null

    if (scheme == null) {
        realPath = path //如果scheme为空
    } else if (ContentResolver.SCHEME_FILE == scheme) {
        realPath = path //如果得到的scheme以file开头
    } else if (ContentResolver.SCHEME_CONTENT == scheme) {
        //得到的scheme以content开头
        val cursor = context.contentResolver.query(
            this,
            arrayOf(MediaStore.Images.ImageColumns.DATA),
            null, null, null
        )
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                if (index > -1) {
                    realPath = cursor.getString(index)
                }
            }
            cursor.close() //必须关闭
        }
    }

//经过上面转换得到真实路径之后,判断一下这个路径,如果还是为空的话,说明有可能文件存在于外置sd卡上,不是内置sd卡.
    if (TextUtils.isEmpty(realPath)) {
        val uriString = toString()
        val index = uriString.lastIndexOf("/") //匹配 / 在一个路径中最后出现位置

        val imageName = uriString.substring(index)
        //通过得到的最后一个位置,然后截取这个位置后面的字符串, 这样就可以得到文件名字了

        var storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ) //查看外部储存卡公共照片的文件

        val file = File(storageDir, imageName)
        //自己创建成文件,

        if (file.exists()) {
            realPath = file.getAbsolutePath()
        } else {
//  //那么存储在了外置sd卡的应用缓存file中
            storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file1 = File(storageDir, imageName)
            realPath = file1.absolutePath
        }
    }
    return realPath
}

fun Uri?.toBitmap(context: Context) = if (this == null) null else BitmapFactory.decodeStream(
    context.contentResolver.openInputStream(this)
)

fun Bitmap?.saveToFile(file: File, callBack: (result: Boolean) -> Unit) {
    if (this == null) {
        callBack.invoke(false)
        return
    }
    kotlin.runCatching {
        val bos = BufferedOutputStream(FileOutputStream(file))
        compress(Bitmap.CompressFormat.JPEG, 100, bos)
        bos.flush()
        bos.close()
    }.onSuccess {
        callBack.invoke(true)
    }.onFailure {
        callBack.invoke(false)
    }

}

