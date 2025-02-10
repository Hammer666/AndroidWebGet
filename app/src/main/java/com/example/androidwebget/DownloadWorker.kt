package com.example.androidwebget

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class DownloadWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext,
    params
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result {
        val url = inputData.getString("URL")?: Result.failure()
        val protocol = inputData.getString("PROTOCOL")?: "Http://"
        val startNum = inputData.getInt("START_NUM",0)
        val endNum = inputData.getInt("END_NUM", 0)

        return withContext(Dispatchers.IO) {
            if (startNum == 0 || endNum == 0) {
                val success = wgetRequest(protocol+url, applicationContext, "default.txt")
                if (success) {
                    Result.success()
                } else {
                    Result.failure()
                }
            } else {
                val semaphore = kotlinx.coroutines.sync.Semaphore(5)
                val downloadJobs = mutableListOf<kotlinx.coroutines.Deferred<Boolean>>()

                for (i in startNum..endNum) {
                    val filename = i.toString()
                    val job = async {
                        semaphore.withPermit {
                            val result = wgetRequest(protocol+url, applicationContext, "$filename.txt")
                            result
                        }
                    }
                    downloadJobs.add(job)
                }

                for (job in downloadJobs) {
                    if (job.await()) {
                        Log.e("DownloadWorker", "Some jobs failed.")
                    }
                }

                Result.success()
            }
        }

    }

    private fun wgetRequest(url:String, context: Context, filename:String): Boolean {
        if (!url.lowercase().startsWith("http://") || !url.lowercase().startsWith("https://")) {
            return false
        }

        if (filename.contains("..")||filename.contains("/")) {
            return false
        }

        val request:Request
        val responseBody:InputStream

        try {
            request = Request.Builder()
                .url(url)
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("DownloadWorker", "Request failed: ${response.code} - ${response.message}", Exception())
                return false
            }
            responseBody = response.body?.byteStream()?:run {
                handleEmptyResponseBody()
                return false
            }
        } catch (e:Exception) {
            handleException("Exception occurred: ${e.message}", e)
            return false
        }

        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (downloadDir != null && !downloadDir.exists()) {
            if (!downloadDir.mkdirs()) {
                Log.e("DownloadWorker", "Failed to create directory ${downloadDir}.")
                return false
            }
        } else {
            Log.e("DownloadWorker", "Device path not exist.")
            return false
        }

        val file = File(downloadDir, filename)
        if (file.exists()) {
            file.delete()
        }

        try {
            responseBody.use {
                output -> FileOutputStream(file).use {
                    fos -> output.copyTo(fos)
                }
            }
        } catch (e:IOException) {
            handleException("IOException occurred: ${e.message}", e)
            return false
        }
        return true
    }

    // 处理空响应体的情况
    private fun handleEmptyResponseBody() {
        Log.e("DownloadWorker", "Empty response body")
    }

    private fun handleException(message:String, e:Exception) {
        Log.e("DownloadWorker", message, e)
    }
}

