package com.starmax.sdkdemo.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import com.starmax.bluetoothsdk.BmpUtils
import com.starmax.bluetoothsdk.Utils.Companion.int2byte
import java.nio.ByteBuffer
import androidx.core.graphics.scale

object BmpUtils {
    // 3字节格式（A8-R8-G8-B8，舍弃B的低3位）
    fun convertTo3Bytes(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): ByteArray {
        val scaledBitmap = bitmap.scale(targetWidth, targetHeight)
        
        val pixels = IntArray(scaledBitmap.width * scaledBitmap.height)
        scaledBitmap.getPixels(pixels, 0, scaledBitmap.width, 0, 0, 
                             scaledBitmap.width, scaledBitmap.height)
        
        val buffer = ByteBuffer.allocate(pixels.size * 3) // 每个像素3字节
        
        for (pixel in pixels) {
            // 分解ARGB通道
            val alpha = (pixel shr 24) and 0xFF  // 8位
            val red = (pixel shr 16) and 0xFF    // 8位
            val green = (pixel shr 8) and 0xFF   // 8位
            val blue = pixel and 0xFF            // 取高5位

            val bmp16 = bmp24to16(blue, green, red)
            val bmp16Data = int2byte(bmp16)
            
            // 转换为3字节格式：A-R-G (B舍弃低3位)
            buffer.put(bmp16Data[0])
            buffer.put(bmp16Data[1])
            buffer.put(alpha.toByte())
            // 注意：这里舍弃Blue通道，根据实际需求调整

        }
        
        return buffer.array()
    }

    fun convertTo3BytesChangeColor(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): ByteArray {
//        val scaledBitmap = bitmap.scale(targetWidth, targetHeight)

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0,
            bitmap.width, bitmap.height)

        val buffer = ByteBuffer.allocate(pixels.size * 3) // 每个像素3字节
        Log.d("colorText","处理图片")
        for (pixel in pixels) {
            // 分解ARGB通道
            val alpha = (pixel shr 24) and 0xFF  // 8位
            var red = (pixel shr 16) and 0xFF    // 8位
            var green = (pixel shr 8) and 0xFF   // 8位
            var blue = pixel and 0xFF            // 取高5位
            //Log.d("colorText","$alpha  $red   $green   $blue")
            if(alpha != 0){
                red = 172
                green = 182
                blue = 38
            }

            val bmp16 = bmp24to16(blue, green, red)
            val bmp16Data = int2byte(bmp16)

            // 转换为3字节格式：A-R-G (B舍弃低3位)
            buffer.put(bmp16Data[0])
            buffer.put(bmp16Data[1])
            buffer.put(alpha.toByte())
            // 注意：这里舍弃Blue通道，根据实际需求调整
        }

        return buffer.array()
    }

    fun argbConvertColor(bytes: ByteArray,red: Int,green: Int,blue: Int): ByteArray {
        for(i in 4 until bytes.size - 2 step 3){
            val alpha = bytes[i+2]
            if(alpha != 0.toByte()){
                val bmp16 = bmp24to16(blue, green, red)
                val bmp16Data = int2byte(bmp16)
                bytes[i] = bmp16Data[0]
                bytes[i+1] = bmp16Data[1]
                bytes[i+2] = alpha
            }
        }

        return bytes
    }

    @Deprecated("Use convertTo3Bytes instead", ReplaceWith("convertTo3Bytes(bitmap, width, height)"))
    fun convert(bitmap: Bitmap, width: Int, height: Int): ByteArray {
        return convertTo3Bytes(bitmap, width, height)
    }

    fun convertColor(bitmap: Bitmap, width: Int, height: Int): ByteArray {
        return convertTo3BytesChangeColor(bitmap, width, height)
    }

    fun bmp24to16(blue: Int, green: Int, red: Int): Int {
        val B = blue shr 3 and 0x001F
        val G = green shr 2 shl 5 and 0x07E0
        val R = red shr 3 shl 11 and 0xF800
        return R or G or B
    }

    fun convertSize(srcBmp: Bitmap,width:Int,height:Int): Bitmap {
        /*改变图片大小*/
        val originWidth = srcBmp.width //原始宽度
        val originHeight = srcBmp.height //原始高度
        val scaleWidth = width.toFloat() / originWidth
        val scaleHeight = height.toFloat() / originHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight) //  缩放图片
        val newBmp = Bitmap.createBitmap(srcBmp, 0, 0, originWidth, originHeight, matrix, true)
        return newBmp //获取最终修改完成之后的Bitmap对象
    }
}