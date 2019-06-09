package com.lightningkite.konvenience.gradle

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.util.logging.Level


fun File.checkSum(): String? {
    try {
        val fis = FileInputStream(path)
        val md = MessageDigest.getInstance("MD5")

        val buffer = ByteArray(8192)
        var numOfBytesRead = 0
        while (true) {
            numOfBytesRead = fis.read(buffer)
            if (numOfBytesRead > 0) {
                md.update(buffer, 0, numOfBytesRead)
            } else {
                break
            }
        }
        val hash = md.digest()
        return BigInteger(1, hash).toString(16)
    } catch (ex: IOException) {
        return null
    }
}

fun File.copyToIfDifferent(to: File): Boolean {
    if (to.exists() && to.length() == this.length() && this.checkSum() == to.checkSum()) {
        return false
    } else {
        this.copyTo(to, true)
        return true
    }
}

/**
 * Copies this file with all its children to the specified destination [target] path.
 * If some directories on the way to the destination are missing, then they will be created.
 *
 * If this file path points to a single file, then it will be copied to a file with the path [target].
 * If this file path points to a directory, then its children will be copied to a directory with the path [target].
 *
 * The operation doesn't preserve copied file attributes such as creation/modification date, permissions, etc.
 *
 * If any errors occur during the copying, then further actions will depend on the result of the call
 * to `onError(File, IOException)` function, that will be called with arguments,
 * specifying the file that caused the error and the exception itself.
 * By default this function rethrows exceptions.
 *
 * Exceptions that can be passed to the `onError` function:
 *
 * - [NoSuchFileException] - if there was an attempt to copy a non-existent file
 * - [FileAlreadyExistsException] - if there is a conflict
 * - [AccessDeniedException] - if there was an attempt to open a directory that didn't succeed.
 * - [IOException] - if some problems occur when copying.
 *
 * Note that if this function fails, then partial copying may have taken place.
 *
 * @return `false` if the copying was terminated, `true` otherwise.
 */
fun File.copyRecursivelyIfDifferent(
        target: File,
        onError: (File, IOException) -> OnErrorAction = { _, exception -> throw exception }
): Boolean {
    if (!exists()) {
        return onError(this, NoSuchFileException(file = this, reason = "The source file doesn't exist.")) !=
                OnErrorAction.TERMINATE
    }
    try {
        // We cannot break for loop from inside a lambda, so we have to use an exception here
        for (src in walkTopDown().onFail { f, e -> if (onError(f, e) == OnErrorAction.TERMINATE) throw TerminateException(f) }) {
            if (!src.exists()) {
                if (onError(src, NoSuchFileException(file = src, reason = "The source file doesn't exist.")) ==
                        OnErrorAction.TERMINATE)
                    return false
            } else {
                val relPath = src.toRelativeString(this)
                val dstFile = File(target, relPath)

                if (src.isDirectory) {
                    dstFile.mkdirs()
                } else {
                    src.copyToIfDifferent(dstFile)
                    if (dstFile.length() != src.length()) {
                        if (onError(src, IOException("Source file wasn't copied completely, length of destination file differs.")) == OnErrorAction.TERMINATE)
                            return false
                    }
                }
            }
        }
        return true
    } catch (e: TerminateException) {
        return false
    }
}
private class TerminateException(val file: File) : Exception()