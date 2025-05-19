package engine

import java.time.Instant
import java.io.{File, PrintWriter, FileOutputStream}

object Logger {

  private val logFile = new File("output/rules_engine.log")

  // Log transaction-specific events with three components(severity level (INFO, DEBUG and so on), transaction ID and message :

  def log(level: String, transactionId: String, message: String): Unit = {
    val timestamp = Instant.now()

    // Format: [TIMESTAMP] [LEVEL] [TX:ID] Message
    val logEntry = s"$timestamp ${level.padTo(5, ' ')} [TX:$transactionId] $message\n"
    writeToFile(logEntry)
  }

  // Log system events
  def log(level: String, message: String): Unit = {
    val timestamp = Instant.now()
    // Format: [TIMESTAMP] [LEVEL] [SYSTEM] Message
    val logEntry = s"$timestamp ${level.padTo(5, ' ')} [SYSTEM] $message\n"
    writeToFile(logEntry)
  }

  // file writer that appends to existing log file

  private def writeToFile(content: String): Unit = {
    val pw = new PrintWriter(new FileOutputStream(logFile, true))
    try {
      pw.write(content)
    } finally {
      pw.close()
    }
  }
}