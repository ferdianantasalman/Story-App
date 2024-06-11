package com.example.androidintermediate

import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

object MockLog {
    init {
        Mockito.mockStatic(android.util.Log::class.java).apply {
            `when`<Int> { android.util.Log.d(anyString(), anyString()) }.thenReturn(0)
            `when`<Int> { android.util.Log.e(anyString(), anyString()) }.thenReturn(0)
            `when`<Int> { android.util.Log.i(anyString(), anyString()) }.thenReturn(0)
            `when`<Int> { android.util.Log.v(anyString(), anyString()) }.thenReturn(0)
            `when`<Int> { android.util.Log.w(anyString(), anyString()) }.thenReturn(0)
            `when`<Boolean> { android.util.Log.isLoggable(anyString(), anyInt()) }.thenReturn(true)
        }
    }
}