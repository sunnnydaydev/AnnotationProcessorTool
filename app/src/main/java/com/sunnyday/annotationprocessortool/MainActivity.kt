package com.sunnyday.annotationprocessortool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.sunnyday.library.BindView
import com.sunnyday.library.CheckGetter

@CheckGetter // 测试定义类上的注解
class MainActivity : AppCompatActivity() {

    //@CheckGetter // 测试定义在字段上的注解
    @BindView(R.id.tv_text)
    val textView: TextView? = null

    //@CheckGetter// 测试定义在方法上的注解
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
