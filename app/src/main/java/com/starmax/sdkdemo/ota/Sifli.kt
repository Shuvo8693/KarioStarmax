package com.starmax.sdkdemo.ota

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.BIND_AUTO_CREATE
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sifli.siflidfu.DFUImagePath
import com.sifli.siflidfu.ISifliDFUService
import com.sifli.siflidfu.Protocol.IMAGE_ID_CTRL
import com.sifli.siflidfu.Protocol.IMAGE_ID_EX
import com.sifli.siflidfu.Protocol.IMAGE_ID_FONT
import com.sifli.siflidfu.Protocol.IMAGE_ID_HCPU
import com.sifli.siflidfu.Protocol.IMAGE_ID_RES
import com.sifli.siflidfu.SifliDFUService
import com.sifli.siflidfu.SifliDFUService.BROADCAST_DFU_LOG
import com.sifli.siflidfu.SifliDFUService.BROADCAST_DFU_PROGRESS
import com.sifli.siflidfu.SifliDFUService.BROADCAST_DFU_STATE
import com.sifli.siflidfu.SifliDFUService.EXTRA_DFU_PROGRESS
import com.sifli.siflidfu.SifliDFUService.EXTRA_DFU_PROGRESS_TYPE
import com.sifli.siflidfu.SifliDFUService.EXTRA_DFU_STATE
import com.sifli.siflidfu.SifliDFUService.EXTRA_DFU_STATE_RESULT
import com.sifli.siflidfu.SifliDFUService.EXTRA_LOG_MESSAGE
import com.sifli.siflidfu.SifliDFUService.SifliDFUBinder
import com.sifli.watchfacelibrary.SifliWatchfaceService
import com.starmax.sdkdemo.viewmodel.OtaViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Timer
import java.util.TimerTask
import java.util.zip.ZipInputStream

class Sifli(val otaViewModel: OtaViewModel) {
    val TAG = "Sifli"

    private var otaTask : TimerTask? = null;
    private var otaTimer : Timer? = null;
    private var nBinder: SifliDFUBinder? = null;
    private var sifliDFUService : ISifliDFUService? = null
    private var sifliWatchfaceBinder: SifliWatchfaceService.SifliWatchfaceBinder? =null
    private var sifliWatchfaceService : SifliWatchfaceService? = null
    private var isBound = false
    private var isWatchFaceBound = false

    class LocalListenerReceiver(val otaViewModel: OtaViewModel) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BROADCAST_DFU_PROGRESS -> {
                    val progress = intent.getIntExtra(EXTRA_DFU_PROGRESS,0)
                    val type = intent.getIntExtra(EXTRA_DFU_PROGRESS_TYPE,0)
                    otaViewModel.otaMessage.postValue("当前进度：${progress}/100,type:${type}")
                    if(progress == 100){
                        otaViewModel.sifli?.stopTimer()
                    }else{
                        otaViewModel.sifli?.startTimer()
                    }

                }

                BROADCAST_DFU_LOG -> {
                    val DFULog = intent.getStringExtra(EXTRA_LOG_MESSAGE)
                    otaViewModel.otaMessage.postValue("状态：${DFULog}")
                }

                BROADCAST_DFU_STATE -> {
                    val dfuState = intent.getIntExtra(EXTRA_DFU_STATE,0)
                    val dfuStateResult = intent.getIntExtra(EXTRA_DFU_STATE_RESULT, 0)

                    otaViewModel.otaMessage.postValue("dfuState：${dfuState},dfuStateResult:${dfuStateResult}")
                }
            }
        }
    }

    class WatchFaceLocalListenerReceiver(val otaViewModel: OtaViewModel) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                SifliWatchfaceService.BROADCAST_WATCHFACE_STATE -> {
                    val state = intent.getIntExtra(SifliWatchfaceService.EXTRA_WATCHFACE_STATE,-1)
                    val resp = intent.getIntExtra(SifliWatchfaceService.EXTRA_WATCHFACE_STATE_RSP,0)
                    otaViewModel.otaMessage.postValue("state：${state},resp:${resp}")
                }

                SifliWatchfaceService.BROADCAST_WATCHFACE_PROGRESS -> {
                    val progress = intent.getIntExtra(SifliWatchfaceService.EXTRA_WATCHFACE_PROGRESS,-1)
                    otaViewModel.otaMessage.postValue("当前进度：${progress}/100")
                }
            }
        }
    }

    private var serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            nBinder = p1 as SifliDFUBinder;
            sifliDFUService = nBinder?.dfuService;
            isBound = true;
            Log.i(TAG,"onServiceConnected");
            registerDfuLocalBroadcast()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.i(TAG,"onServiceDisconnected");
            isBound = false;
        }
    }

    private var watchfaceServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            sifliWatchfaceBinder = p1 as SifliWatchfaceService.SifliWatchfaceBinder;
            sifliWatchfaceService = sifliWatchfaceBinder?.service as SifliWatchfaceService?;
            isWatchFaceBound = true;
            Log.i(TAG,"onServiceConnected");
            registerWatchFaceLocalBroadcast()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.i(TAG,"onServiceDisconnected");
            isWatchFaceBound = false;
        }
    }

    init {
        val serviceIntent = Intent(otaViewModel.context, SifliDFUService::class.java)
        otaViewModel.context.bindService(serviceIntent,serviceConnection, BIND_AUTO_CREATE)
        val watchFaceIntent = Intent(otaViewModel.context, SifliWatchfaceService::class.java)
        otaViewModel.context.bindService(watchFaceIntent,watchfaceServiceConnection, BIND_AUTO_CREATE)
    }

    private fun registerDfuLocalBroadcast(){
        val intentFilter = IntentFilter()
        intentFilter.addAction(BROADCAST_DFU_LOG)
        intentFilter.addAction(BROADCAST_DFU_STATE)
        intentFilter.addAction(BROADCAST_DFU_PROGRESS)
        LocalBroadcastManager.getInstance(otaViewModel.context).registerReceiver(LocalListenerReceiver(otaViewModel),intentFilter)
    }

    private fun registerWatchFaceLocalBroadcast(){
        val intentFilter = IntentFilter()
        intentFilter.addAction(SifliWatchfaceService.BROADCAST_WATCHFACE_STATE)
        intentFilter.addAction(SifliWatchfaceService.BROADCAST_WATCHFACE_PROGRESS)
        LocalBroadcastManager.getInstance(otaViewModel.context).registerReceiver(WatchFaceLocalListenerReceiver(otaViewModel),intentFilter)
    }

    fun startOta(){
        if(isBound){
            val zipFile = File(otaViewModel.localPath)

            var basepath = otaViewModel.context.getExternalFilesDir(null)?.path
            if(basepath == null){
                basepath = Environment.getExternalStorageDirectory().absolutePath
            }

            val destinationDir = File(basepath + "/SDKDemo/Device_update/zip/")
            val filePaths = unzipAndGetFilePaths(zipFile, destinationDir)
            val paths = ArrayList<DFUImagePath>()

            filePaths.forEach {
                if(it.endsWith("ctrl_packet.bin")){
                    paths.add(DFUImagePath(it,null,IMAGE_ID_CTRL));
                }else if(it.endsWith("outapp.bin")){
                    paths.add(DFUImagePath(it,null, IMAGE_ID_HCPU));
                }else if(it.endsWith("outfont.bin")){
                    paths.add(DFUImagePath(it,null, IMAGE_ID_FONT));
                }else if(it.endsWith("outres.bin")){
                    paths.add(DFUImagePath(it,null, IMAGE_ID_RES));
                }else if(it.endsWith("outroot.bin")){
                    paths.add(DFUImagePath(it,null, IMAGE_ID_EX));
                }
            }

            if(filePaths.isNotEmpty()){
                filePaths.forEach {
                    println(it)
                }
                sifliDFUService?.startActionDFUNorExt(
                    otaViewModel.context,
                    otaViewModel.mac,
                    paths,
                    1,0)
            }


        }
    }

    fun startWatchFace(){
        if(isWatchFaceBound){
            sifliWatchfaceService?.startActionWatchface(otaViewModel.context,otaViewModel.localPath,otaViewModel.mac,SifliWatchfaceService.FILE_TYPE_WATCHFACE_JS)
        }
    }

    fun unzipAndGetFilePaths(zipFile: File, destinationDir: File): List<String> {
        val filePathsList = mutableListOf<String>()
        val buffer = ByteArray(1024)
        val zipInputStream = ZipInputStream(FileInputStream(zipFile))

        if(!destinationDir.exists()){
            destinationDir.mkdirs()
        }

        var zipEntry = zipInputStream.nextEntry
        while (zipEntry != null) {
            val newFile = File(destinationDir, zipEntry.name)
            filePathsList.add(newFile.absolutePath) // 添加文件路径到列表中
            if (zipEntry.isDirectory) {
                newFile.mkdirs()
            } else {
                newFile.parentFile?.mkdirs()
                val fileOutputStream = FileOutputStream(newFile)
                var len: Int
                while (zipInputStream.read(buffer).also { len = it } > 0) {
                    fileOutputStream.write(buffer, 0, len)
                }
                fileOutputStream.close()
            }
            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.closeEntry()
        zipInputStream.close()

        return filePathsList
    }

    fun startTimer(){
        stopTimer()
        otaTimer = Timer()
        // 超时任务
        otaTask = object : TimerTask() {
            override fun run() {
                println("超时任务执行")
                startOta()
            }
        }

        // 启动定时器，延迟15秒后执行超时任务
        otaTimer?.schedule(otaTask, 15000,15000)
    }

    fun stopTimer(){
        otaTimer?.cancel()
        otaTimer = null
        otaTask = null
    }


}