package com.bymarcin.automatedscoutingreport.data

class Event() {
    var event: String = ""
    var StarSystem: String = ""

    var ScanType: String = ""
    var BodyName: String = ""
    var PlanetClass: String = ""
    var Materials: List<Material> = mutableListOf()
    fun isFSDJump(): Boolean = event == "FSDJump"
    fun isDetailedScan(): Boolean = event == "Scan" && ScanType == "Detailed"
}