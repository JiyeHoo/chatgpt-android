package org.yameida.asrassistant.utils

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import okhttp3.*
import org.yameida.asrassistant.config.Config
import org.yameida.asrassistant.model.Message
import org.yameida.asrassistant.model.StreamAiAnswer
import java.io.BufferedReader
import java.io.IOException
import java.lang.Exception
import kotlin.collections.ArrayList


object HttpUtil {

    val history: ArrayList<Message> = arrayListOf()
    val gptRequestJson = hashMapOf(
//        Pair("model", "gpt-3.5-turbo"),
        Pair("stream", true),
        Pair("messages", history)
    )

    /**
     * ChatGPT
     */
    fun chat(send: String, callback: CallBack) {
//        val url = "http://proxy.chat.carlife.host/v1/chat/completions"
        val url = "https://api.chatanywhere.tech/v1/chat/completions"
        // sk-I7bzfVwr4YF2HKToaDlAZLT1a8nJP4nXywL3lxeblTbr3nO5
        var apiKey = "Bearer ${Config.apiKey}"
        if (Config.apiKey == "1") {
//            apiKey = "Bearer sk-I7bzfVwr4YF2HKToaDlAZLT1a8nJP4nXywL3lxeblTbr3nO5"
            apiKey = "Bearer sk-lYPE5ZYSwWxLproWGTRNjdyNvJgiaLi5YPZjJThWbEluXoQq"
        }
        if (!Config.useContext) {
            history.clear()
        }
        history.add(Message().apply {
            role = "user"
            content = send
        })
        LogUtils.d("gptRequestJson", GsonUtils.toJson(gptRequestJson))
        val body = RequestBody.create(MediaType.parse("application/json"), GsonUtils.toJson(gptRequestJson))
        val request: Request = Request.Builder().url(url).method("POST", body)
            .addHeader("Authorization", apiKey)
            .addHeader("Content-Type", "application/json")
            .build()
        OkHttpUtil.okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                ToastUtils.showLong("网络请求出错 请检查网络")
            }
            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val message = Message().apply {
                            role = "assistant"
                            content = ""
                        }
                        history.add(message)
                        val bufferedReader = BufferedReader(responseBody.charStream())
                        var line = bufferedReader.readLine()
                        var index = 0
                        val sb = StringBuilder()
                        while (line != null) {
                            val msg = convert(line, "1", index++)
                            if (msg != null) {
                                sb.append(msg.content)
                                message.content = sb.toString()
                                callback.onCallBack(sb.toString(), false)
                            }
                            line = bufferedReader.readLine()
                        }
                        callback.onCallBack(sb.toString(), true)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastUtils.showLong("网络请求出错 请检查配置")
                }
            }
        })
    }

    fun convert(answer: String, questionId: String, index: Int): Message? {
        val msg = Message()
        msg.content = ""
        msg.messageType = "normal"
        msg.id = questionId
        if ("data: [DONE]" != answer) {
            val beanStr = answer.replaceFirst("data: ", "", false)
            try {
                val aiAnswer =
                    GsonUtils.fromJson(beanStr, StreamAiAnswer::class.java) ?: return null
                val choices = aiAnswer.choices
                if (choices.isEmpty()) {
                    return null
                }
                val stringBuffer = StringBuffer()
                for (choice in choices) {
                    if (choice.finish_reason != "stop") {
                        if (choice.delta.content != null) {
                            stringBuffer.append(choice.delta.content)
                        } else {
                            return null
                        }
                    }
                }
                msg.content = stringBuffer.toString()
                if (index == 0) {
                    if (msg.content == "\n\n") {
                        LogUtils.e("发现开头有两次换行,移除两次换行")
                        return null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            msg.type = "stop"
        }
        msg.index = index
        return msg
    }

    interface CallBack {
        fun onCallBack(result: String, isLast: Boolean)
    }
}
