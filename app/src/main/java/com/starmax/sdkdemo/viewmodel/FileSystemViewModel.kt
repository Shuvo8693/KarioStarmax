package com.starmax.sdkdemo.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starmax.bluetoothsdk.BleFileSender.dataSize
import com.starmax.bluetoothsdk.Notify.GetFileV2
import com.starmax.bluetoothsdk.StarmaxBleClient
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream

class FileSystemViewModel: ViewModel(), KoinComponent {
    var selectedApp by mutableStateOf(1)

    val defaultApps = intArrayOf(
        0,1,2,3,4
    )

    val context : Context by inject()

    fun getFileSystem(savePath: String){

        StarmaxBleClient.instance.getFileHeader(selectedApp)?.subscribe({
            if(it !is GetFileV2){
                return@subscribe
            }
            if(it.isOpen == 1){
                val fileName = it.fileName
                val destinationDir = File(savePath + "/file_system/")
                if(!destinationDir.exists()){
                    destinationDir.mkdirs()
                }

                val currentFile = File(savePath + "/file_system/" + it.fileName.replace("\\","/"))
                val relativePath = destinationDir.path + "/" + currentFile.relativeTo(destinationDir).path.replace(
                    File.separator, "/")
                val newFile  = File(destinationDir.path+"/"+relativePath.split("/").last())

                fun getFileRefresh(index:Int,len: Int){
                    if(index > len){
                        return
                    }
                    StarmaxBleClient.instance.getFileContent(index)
                        .subscribe({ response ->
                            val fos = FileOutputStream(newFile,if(index == 0)  false else true)
                            fos.write(response.data.toByteArray())
                            fos.close()

                            getFileRefresh(index + 1, len)
                        },{

                        }).let {  }
                }

                dataSize = if (it.fileSize % 4096 == 0) {
                    (it.fileSize / 4096)
                } else {
                    (it.fileSize / 4096) + 1
                }
                getFileRefresh(0, dataSize - 1)


            }
        },{

        }).let {  }
    }
}