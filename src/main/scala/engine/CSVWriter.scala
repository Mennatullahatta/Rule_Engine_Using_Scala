package engine

import java.io.{File, PrintWriter, FileOutputStream}
import java.time.format.DateTimeFormatter

// Handles CSV output generation for processed transactions
object CSVWriter {

  private val outputFile = new File("output/processedOrders.csv")
  private val dateFormatter = DateTimeFormatter.ISO_DATE

  def writeHeader(): Unit = {
    Logger.log("INFO", "Initializing CSV file")

    // Column names matching database structure
    val header = "TransactionID,TransactionDate,ProductName,ExpiryDate," +
      "Quantity,UnitPrice,TotalPrice,Channel,PaymentMethod," +
      "AppliedDiscounts,EffectiveDiscount,FinalPrice\n"


    val pw = new PrintWriter(outputFile)
    try pw.write(header)
    finally pw.close()
  }

  // Appends transactions to CSV file
  def appendTransaction(pt: ProcessedTransaction): Unit = {
    Logger.log("DEBUG", pt.transaction.id, "Writing to CSV")

    // CSV record
    val line = {
      s"${pt.transaction.id}," +
        s"${pt.transaction.transactionDate.format(dateFormatter)}," +
        s"${escapeCsv(pt.transaction.product.name)}," +
        s"${pt.transaction.product.expiryDate.format(dateFormatter)}," +
        s"${pt.transaction.quantity}," +
        s"${pt.transaction.price / pt.transaction.quantity}," +
        s"${pt.transaction.price}," +
        s"${pt.transaction.channel}," +
        s"${pt.transaction.paymentMethod}," +
        s""""${pt.appliedDiscounts.map(d => s"${d.ruleName}:${d.discount}%").mkString(";")}",""" +
        s"${pt.calculatedDiscount}," +
        s"${pt.finalPrice}\n"
    }

    // Append to file with error handling
    val pw = new PrintWriter(new FileOutputStream(outputFile, true))
    try {
      pw.write(line)
      Logger.log("INFO", pt.transaction.id,
        s"CSV write successful. ${pt.transaction.product.name}"
      )
    } catch {
      case e: Exception =>
        Logger.log("ERROR", pt.transaction.id,
          s"CSV write failed: ${e.getMessage}\nLine: $line"
        )
    } finally {
      pw.close()
    }
  }

  // Formats text fields for CSV safety
  private def escapeCsv(value: String): String = {
    if (value.contains(",") || value.contains("\""))
      s""""${value.replace("\"", "\"\"")}""""
    else
      value
  }
}