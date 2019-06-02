package com.lightningkite.konvenience.gradle

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.userauth.method.AuthMethod
import net.schmizz.sshj.userauth.method.AuthPassword
import net.schmizz.sshj.userauth.method.AuthPublickey
import net.schmizz.sshj.userauth.password.PasswordFinder
import net.schmizz.sshj.userauth.password.Resource
import net.schmizz.sshj.xfer.FileSystemFile
import net.schmizz.sshj.xfer.InMemorySourceFile
import net.schmizz.sshj.xfer.LocalSourceFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.*
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile
import java.lang.StringBuilder
import kotlin.math.absoluteValue


fun ssh(toSshPropertiesFile: File, action: SSHClient.() -> Unit) {
    if (!toSshPropertiesFile.exists()) {
        println("No properties file found at $toSshPropertiesFile for doing SSH - please add one.")
        return
    }
    val properties = Properties()
    properties.load(toSshPropertiesFile.inputStream())
    SSHClient().use { ssh ->
        val knownHostsLocation = properties.getProperty("hosts") as? String
        if (knownHostsLocation == "standard")
            ssh.loadKnownHosts()
        else if (knownHostsLocation != null)
            ssh.loadKnownHosts(File(knownHostsLocation))
        properties.getProperty("fingerprint")?.let { ssh.addHostKeyVerifier(it) }
        ssh.connect(properties.getProperty("hostname")!!)
        val username = properties.getProperty("username")!!
        val authMethods = ArrayList<AuthMethod>()
        properties.getProperty("key")?.let {
            println("Authenticating with key from $it")
            val keyFile = PKCS8KeyFile()
            keyFile.init(File(it))
            authMethods.add(AuthPublickey(keyFile))
        }
        properties.getProperty("password")?.let {
            println("Authenticating with password")
            authMethods.add(AuthPassword(object : PasswordFinder {
                override fun reqPassword(resource: Resource<*>?): CharArray = it.toCharArray()
                override fun shouldRetry(resource: Resource<*>?): Boolean = false
            }))
        }
        ssh.auth(username, authMethods)
        ssh.action()
        ssh.disconnect()
    }
}

fun SSHClient.exec(command: String): String {
    return startSession().use {
        val cmd = it.exec(command)
        val totalResult = StringBuilder()
        println("Executing: $command")
        val a = Thread {
            cmd.errorStream.use { it.copyTo(System.err) }
        }
        val b = Thread {
            cmd.inputStream.reader().use {
                val outWriter = System.out.writer()
                var charsCopied: Long = 0
                val buffer = CharArray(1024)
                var chars = it.read(buffer)
                while (chars >= 0) {
                    outWriter.write(buffer, 0, chars)
                    totalResult.append(buffer, 0, chars)
                    charsCopied += chars
                    chars = it.read(buffer)
                }
            }
        }

        a.start()
        b.start()
        cmd.join()
        a.join()
        b.join()
        totalResult.toString()
    }
}

private fun SSHClient.sourceFromByteArray(byteArray: ByteArray, name: String = "name") = object : InMemorySourceFile() {
    override fun getLength(): Long = byteArray.size.toLong()
    override fun getName(): String = name
    override fun getInputStream(): InputStream = ByteArrayInputStream(byteArray)
}

private fun SSHClient.sourceFromString(string: String, name: String = "name") = sourceFromByteArray(string.toByteArray(), name = name)

fun SSHClient.upload(directory: File, remotePath: String) {
    val fixedRemotePath = if(remotePath.contains('~')){
        remotePath.replace("~", exec("echo ~").trim()).also { println("Fixed path to $it") }
    } else {
        remotePath
    }
    newSFTPClient().use { sftp ->
        directory.walkTopDown().forEach {
            val relative = it.toRelativeString(directory).replace('\\', '/')
            val remote = "$fixedRemotePath/$relative"
            if (it.isDirectory) {
                sftp.mkdir(remote)
//                exec("mkdir -p $remote")
            } else {
                val attrs = try {
                    sftp.stat(remote)
                } catch (t: Throwable) {
//                    t.printStackTrace()
                    null
                }
                val local = it.lastModified() / 1000
                if (attrs == null) {
                    println("Uploading $relative to $remote...")
                    sftp.put(FileSystemFile(it), remote)
                } else if (local > attrs.mtime) {
                    println("Updating $relative to $remote...")
                    sftp.rm(remote)
                    sftp.put(FileSystemFile(it), remote)
                }
            }
        }
    }
}

fun SSHClient.uploadText(string: String, remotePath: String) {
    exec("mkdir -p $remotePath")
    println("Uploading text $string...")
    newSCPFileTransfer().upload(sourceFromString(string), remotePath)
}

fun SSHClient.uploadTextSudo(string: String, remotePath: String) {
    val tempPath = "~/toSyncFromSsh/text.txt"
    exec("mkdir -p $tempPath")
    uploadText(string, tempPath)
    exec("sudo cp -rlf $tempPath $remotePath")
}

fun SSHClient.uploadSudo(directory: File, remotePath: String) {
    val tempPath = "~/toSyncFromSsh/f${directory.absolutePath.hashCode().absoluteValue}"
    exec("mkdir -p $tempPath")
    upload(directory, tempPath)
    exec("sudo cp -rlf $tempPath/* $remotePath")
}