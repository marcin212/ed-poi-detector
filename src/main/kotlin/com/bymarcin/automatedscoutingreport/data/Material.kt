package com.bymarcin.automatedscoutingreport.data

import com.bymarcin.automatedscoutingreport.format

class Material {
    var Name: String = ""
    var Percent: Double = 0.0
    override fun toString(): String {
        return "${Name.capitalize()}: [${Percent.format(2)}]"
    }
}