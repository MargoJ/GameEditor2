package pl.margoj.editor2.utils

import java.io.FilterInputStream
import java.io.InputStream

class ProgressReportingInputStream(inputStream: InputStream, val progressCallback: (Long) -> Unit) : FilterInputStream(inputStream)
{
    private var totalRead = 0L

    private fun updateProgress(read: Long): Long
    {
        if (read > 0)
        {
            this.totalRead += read
            this.progressCallback(this.totalRead)
        }

        return read
    }

    override fun skip(n: Long): Long
    {
        return updateProgress(super.skip(n))
    }

    override fun reset()
    {
        throw UnsupportedOperationException()
    }

    override fun mark(readlimit: Int)
    {
        throw UnsupportedOperationException()
    }

    override fun markSupported(): Boolean
    {
        return false
    }

    override fun read(): Int
    {
        val b = super.read()
        updateProgress(1)
        return b
    }

    override fun read(b: ByteArray?): Int
    {
        return updateProgress(super.read(b).toLong()).toInt()
    }

    override fun read(b: ByteArray?, off: Int, len: Int): Int
    {
        return updateProgress(super.read(b, off, len).toLong()).toInt()
    }
}