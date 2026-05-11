package com.example.pr_6_3

import android.app.Application
import com.example.pr_6_3.di.AppContainer

class App : Application() {
    val container by lazy { AppContainer(this) }
}
