package data

class Material {
    var Name: String = ""
    var Percent: Double = 0.0
    override fun toString(): String {
        return "$Name: $Percent%"
    }
}