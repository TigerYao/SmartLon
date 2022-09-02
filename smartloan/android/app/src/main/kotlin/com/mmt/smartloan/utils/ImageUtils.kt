package com.mmt.smartloan.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException

const val minSize = 256f
const val maxSize = 2048f

fun String.compressImageFromFile(): Bitmap? {
    val newOpts: BitmapFactory.Options = BitmapFactory.Options()
    newOpts.inJustDecodeBounds = false // 只读边,不读内容
    var bitmap: Bitmap = BitmapFactory.decodeFile(this, null)
    newOpts.inJustDecodeBounds = false
    val w: Int = newOpts.outWidth
    val h: Int = newOpts.outHeight
    val hh = 800f //
    val ww = 480f //
    var be = 1
    if (w > h && w > ww) {
        be = (newOpts.outWidth / ww).toInt()
    } else if (w < h && h > hh) {
        be = (newOpts.outHeight / hh).toInt()
    }
    if (be <= 0) be = 1
    newOpts.inSampleSize = be // 设置采样率

    // newOpts.inPreferredConfig = Config.ARGB_8888;//该模式是默认的,可不设
    newOpts.inPurgeable = true // 同时设置才会有效
    newOpts.inInputShareable = true // 。当系统内存不够时候图片自动被回收
    bitmap = BitmapFactory.decodeFile(this, newOpts)
    // return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩
    // 其实是无效的,大家尽管尝试
    return bitmap
}

fun Bitmap.readBitmap(): ByteArray {
    val baos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 60, baos)
    try {
        baos.flush()
        baos.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return baos.toByteArray()
}


fun Bitmap.convertToThumb(): Bitmap? {
    val bmpWidth = this.width
    val bmpHeight = this.height
    var scaleRadio = 1f
    val origSize = bmpWidth * bmpHeight
    if(origSize > maxSize){
        scaleRadio = maxSize/origSize
    }else if (origSize < minSize){
        scaleRadio = minSize/origSize
    }else{
        return this
    }
    val matrix = Matrix()
    matrix.postScale(scaleRadio, scaleRadio);// 产生缩放后的Bitmap对象
    val resizeBitmap = Bitmap.createBitmap(this, 0, 0, bmpWidth, bmpHeight, matrix, false);
    this.recycle();
    return resizeBitmap
}


/**
 * bitmap转为base64
 * @param bitmap
 * @return
 */
fun Bitmap?.bitmapToBase64(): String? {
    var result: String? = null
    var baos: ByteArrayOutputStream? = null
    try {
        if (this != null) {
            baos = ByteArrayOutputStream()
            //将bitmap放入字节数组流中
            //参数2：压缩率，40表示压缩掉60%; 如果不压缩是100，表示压缩率为0
            this.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            //将bos流缓存在内存中的数据全部输出，清空缓存
            baos.flush()
            baos.close()
            val bitmapBytes: ByteArray = baos.toByteArray()
            result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            if (baos != null) {
                baos.flush()
                baos.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return result
}

/**
 * base64转为bitmap
 * @param base64Data
 * @return
 */
fun String.base64ToBitmap(): Bitmap? {
    val bytes: ByteArray = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
