package pl.margoj.editor2.editor

class DebuggingProfile(val profiles: List<String>)
{
    constructor(string: String) : this(string.split(","))

    private val all = this.profiles.any { it == "all" }

    operator fun contains(element: String): Boolean
    {
        return all || this.profiles.contains(element)
    }
}