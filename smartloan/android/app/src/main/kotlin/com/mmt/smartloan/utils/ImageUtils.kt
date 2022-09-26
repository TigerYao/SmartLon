package com.mmt.smartloan.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException

const val minSize = 256.0
const val maxSize = 2048.0

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


fun Bitmap?.convertToThumb(): Bitmap? {
    if(this == null) return null
    return imageZoom(this)
}

private fun imageZoom(bitMap:Bitmap): Bitmap {
    var newBitmap:Bitmap = bitMap
    //将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
    val baos = ByteArrayOutputStream()
    bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val b = baos.toByteArray()
    //将字节换成KB
    val mid = b.size / 1024.toDouble()
    //判断bitmap占用空间是否大于允许最大空间 如果大于则压缩 小于则不压缩
    if (mid > maxSize) {
        //获取bitmap大小 是允许最大大小的多少倍
        val i = mid / maxSize
        //开始压缩 此处用到平方根 将宽带和高度压缩掉对应的平方根倍 （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
        newBitmap = zoomImage(
            bitMap, bitMap.getWidth() / Math.sqrt(i),
            bitMap.getHeight() / Math.sqrt(i)
        )
    }else if(mid < minSize){
        //获取bitmap大小 是允许最大大小的多少倍
        val i = mid / minSize
        //开始压缩 此处用到平方根 将宽带和高度压缩掉对应的平方根倍 （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
        newBitmap = zoomImage(
            bitMap, bitMap.getWidth() / Math.sqrt(i),
            bitMap.getHeight() / Math.sqrt(i)
        )
    }
    return newBitmap
}


fun zoomImage( bgimage: Bitmap, newWidth: Double,newHeight: Double): Bitmap {
    // 获取这个图片的宽和高
    val width = bgimage.width.toFloat()
    val height = bgimage.height.toFloat()
    // 创建操作图片用的matrix对象
    val matrix = Matrix()
    // 计算宽高缩放率
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    // 缩放图片动作
    matrix.postScale(scaleWidth, scaleHeight)
    return Bitmap.createBitmap(
        bgimage, 0, 0, width.toInt(),
        height.toInt(), matrix, true
    )
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
