package com.bymarcin.automatedscoutingreport

import java.net.URLEncoder

fun String.encodeURL(): String {
    return URLEncoder.encode(this, "UTF-8")
}

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)