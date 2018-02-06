package com.qyd.plugin

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup

class MainActivity : AppCompatActivity() {
    internal var apiUrl = "地址"
    val handle = object : Handler() {
        override fun handleMessage(msg: Message?) {
            msg.let {
                val toString = msg?.obj.toString()
                textviews.setText(toString)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttons.setOnClickListener(View.OnClickListener {


            Thread(object : Runnable {
                override fun run() {
                    val document = Jsoup.connect(apiUrl).get()
                    val elements = document.select("table[class=help-page-table] > tbody > tr")

                    val list = ArrayList<String>()
                    for (element in elements) {

                        val select = element.select("td[class=api-documentation] a")
                        for (element1 in select) {
                            val url = element1.select("a")
                                    .attr("href")
                                    .replace("?help&amp;m2", "&down")
                                    .replace("?help&m2", "&down")
                                    .replace("&amp;dll", "&dll")
                            val text = element1.select("a").text()
                            list.add(text + "::" + url);
                        }
                    }
                    var text = ""
                    val msg = Message.obtain()
                    for (s in list) {
                        text = text + s + "\n"
                    }
                    msg.obj = text
                    //返回主线程
                    handle.sendMessage(msg)
                }
            }).start()


        })
    }
}
