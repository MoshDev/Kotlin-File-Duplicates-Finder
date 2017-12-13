package com.ersan.space

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest


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

    fun deletedDuplicates() {
        inputDir.deleteDuplicates()
    }
}

private fun File.deleteDuplicates() {
    val format = "Deleting: %-150s %s"
    var savedSize = 0L
    var deletedCount = 0
    println("Listing files...")
    val duplicates = listOf(this).duplicates().sortedByDescending { it.length() }
    println("${duplicates.size} duplicated file(s) found...")
    duplicates.forEach {
        val deleted = it.delete()
        if (deleted) {
            deletedCount = deletedCount.inc()
            savedSize = savedSize.plus(it.length())
            println(format.format(it.path, it.length().readableSize()))
        }
    }

    println("\nTotal Saving: ${savedSize.readableSize()} , for $deletedCount file(s)")

}

private fun List<File>.duplicates(): List<File> =
        flatMap { it.allFilesIn() }.distinct()
                // Find lists of files having the same size
                .groupBy { it.length() }.values.filter { it.size > 1 }
                // Group these files by md5 hash, ignoring the first
                .flatMap { it.groupBy { it.md5() }.values }
                .map { it.drop(1) }
                .flatten()

fun File.allFilesIn(): List<File> = when {
    isFile -> listOf(this)
    else -> listFiles().flatMap { it.allFilesIn() }
}

fun Long.readableSize(): String {
    val unit = 1024.0
    if (this < unit) return "$this B"
    val bytes = this.toDouble()
    val exp = (Math.log(bytes) / Math.log(unit)).toInt()
    return "%.1f ${"KMGTPE"[exp - 1]}iB".format(bytes / Math.pow(unit, exp.toDouble()))
}

fun File.md5(): String = with(MessageDigest.getInstance("MD5")) {
    forEachBlock { buffer, bytesRead ->
        update(buffer, 0, bytesRead)
    }
    BigInteger(1, digest()).toString(16)
}
