package engine

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.io.Source

object Main extends App {

  // Initialize application logging
  Logger.log("INFO", "Application started")

  // Date/time format configurations for timezone (e.g., 'Z' for UTC) and standard date format (yyyy-MM-dd)
  private val timestampFormatter: DateTimeFormatter =
    DateTimeFormatter.ISO_OFFSET_DATE_TIME

  private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ISO_DATE

  private def parseCSV(): List[RawTransaction] = {
    Logger.log("INFO", "Reading CSV file TRX1000.csv")

    try {
      // Process CSV lines after header row
      val transactions = Source.fromResource("TRX1000.csv")
        .getLines()
        .drop(1)
        .map { line =>
          val cols = line.split(",").map(_.trim)

          // Parse timestamp with timezone information and convert to local datetime without timezone
          val timestamp = ZonedDateTime.parse(cols(0), timestampFormatter)
            .toLocalDateTime()

          // Parse expiration date
          val expiryDate = LocalDate.parse(cols(2), dateFormatter)

          // Create raw transaction from CSV data and convert it to list
          RawTransaction(
            timestamp = timestamp,
            productName = cols(1),
            expiryDate = expiryDate,
            quantity = cols(3).toInt,
            unitPrice = cols(4).toDouble,
            channel = cols(5),
            paymentMethod = cols(6)
          )
        }.toList

      Logger.log("INFO", s"Successfully parsed ${transactions.size} transactions")
      transactions

    } catch {
      case e: Exception =>
        Logger.log("ERROR", s"CSV parsing failed: ${e.getMessage}")
        throw e
    }
  }

  // Converts raw CSV data to transaction model
  private def transform(raw: RawTransaction): Transaction = {
    Logger.log("DEBUG", s"Transforming transaction ${raw.timestamp}")

    Transaction(
      id = UUID.randomUUID().toString, // Generate unique transaction ID
      product = ProductDetails(
        name = raw.productName,
        expiryDate = raw.expiryDate
      ),
      transactionDate = raw.timestamp.toLocalDate,
      quantity = raw.quantity,
      price = raw.unitPrice * raw.quantity,
      channel = raw.channel,
      paymentMethod = raw.paymentMethod
    )
  }

  // Initialize output CSV with column headers
  CSVWriter.writeHeader()

  // Process all transactions from CSV input
  parseCSV().foreach { raw =>
    try {
      val transaction = transform(raw)
      Logger.log("INFO", transaction.id, "Starting processing")

      // Apply discount rules and calculations
      val processed = RuleEngine.processTransaction(transaction)

      // Log results
      Logger.log("INFO", transaction.id,
        s"Applied ${processed.appliedDiscounts.size} discounts. " +
          s"Final price: ${processed.finalPrice}"
      )

      // results to output files and database
      CSVWriter.appendTransaction(processed)
      DBWriter.save(processed)

    } catch {
      case e: Exception =>
        Logger.log("ERROR", s"Failed to process transaction: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  Logger.log("INFO", "Application finished successfully")
}