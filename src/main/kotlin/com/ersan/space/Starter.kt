package com.ersan.space

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicLong


data class Args(val inputDir: String)

data class ExFile(val file: File, val md5: String)


/**
 * Created by M.Ersan on 10/12/2017.
 */
class Starter(args: Args) {

    private val inputDir: File = File(args.inputDir).apply {
        if (!exists() || !isDirectory) {
            throw IllegalArgumentException("$absolutePath doesn't exists or not a directory")
        }
    }

    fun findDuplicates() {

        val md5Digest = MessageDigest.getInstance("MD5")
        val savedSize = AtomicLong(0)

        println("Listing files...")
        val deepFilesList = listFiles(inputDir)
        println("${deepFilesList.size} files collected...")

        println("Grouping files by size...")
        val groupedFiles = deepFilesList.groupBy { it.length() }.filterValues { it.isNotEmpty() && it.size >= 2 }.values.flatten()

        println("Generating files MD5's....")
        val mappedFiles = groupedFiles.map { ExFile(it, it.getMd5(md5Digest)) }

        println("Grouping again based on MD5...")
        val groupedExFiles = mappedFiles.groupBy { it.md5 }.filterValues { it.isNotEmpty() && it.size >= 2 }.mapValues { it.value.take(it.value.size - 1) }.values.flatten()

        println("Sorting by size...")
        val sortedExFiles = groupedExFiles.sortedByDescending { it.file.length() }

        println("${sortedExFiles.size} duplicated files found...")

        val format = "Deleting: %-150s %s"
        if (sortedExFiles.isNotEmpty()) {
            println("Deleting...")
            sortedExFiles.forEach {
                savedSize.addAndGet(it.file.length())

                println(format.format(it.file.path, it.file.length().readableSize()))
                it.file.delete()
            }
            println("\nTotal Saving: ${savedSize.get().readableSize()}")
        } else {
            println("Sorry, couldn't find any duplicated files...")
        }


    }

    private fun listFiles(dir: File): List<File> {
        if (dir.isFile) {
            return emptyList()
        }

        val files = dir.listFiles()

        val filesToReturn = mutableListOf<File>()
        files.forEach {
            when {
                it.isFile -> filesToReturn.add(it)
                it.isDirectory -> filesToReturn.addAll(listFiles(it))
            }
        }

        return filesToReturn
    }

}

fun File.readableSize(): String {
    return this.length().readableSize()
}

fun Long.readableSize(): String {
    val bytes = this
    val unit = 1024
    if (bytes < unit) return bytes.toString() + " B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
    val pre = ("KMGTPE")[exp - 1] + "i"
    return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
}

fun File.getMd5(messageDigest: MessageDigest): String {
    messageDigest.reset()

    this.forEachBlock { buffer, bytesRead ->
        messageDigest.update(buffer, 0, bytesRead)
    }
    val bigInteger = BigInteger(1, messageDigest.digest())
    return bigInteger.toString(16)
}