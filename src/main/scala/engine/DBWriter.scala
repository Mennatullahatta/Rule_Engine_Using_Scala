package engine

import java.sql.{Connection, PreparedStatement, Date}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.circe.syntax._
import io.circe.generic.auto._
import scala.util.Using

// Handling database operations
object DBWriter {
  private val config = new HikariConfig() {
    {

      setJdbcUrl("jdbc:oracle:thin:@//localhost:1521/XE")
      setUsername("hr")
      setPassword("hr")
      // Connection pool settings
      setMaximumPoolSize(20)
      setConnectionTimeout(30000)
    }
  }

  // Connection pool instance
  private val dataSource = new HikariDataSource(config)

  private val insertSQL =
    """
      INSERT INTO transactions (
        transaction_id, transaction_date, product_name, product_expiry,
        quantity, unit_price, total_price, sales_channel, payment_method,
        discounts_applied, effective_discount, final_price
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

  //  Main save operation for processed transactions
  def save(pt: ProcessedTransaction): Unit = {
    // Using.Manager ensures automatic resource cleanup
    Using.Manager { use =>
      // Get connection from pool
      val conn = use(dataSource.getConnection)
      val stmt = use(conn.prepareStatement(insertSQL))

      //  Setting parameter values from transaction data
      stmt.setString(1, pt.transaction.id)
      stmt.setDate(2, Date.valueOf(pt.transaction.transactionDate))
      stmt.setString(3, pt.transaction.product.name)
      stmt.setDate(4, Date.valueOf(pt.transaction.product.expiryDate))
      stmt.setInt(5, pt.transaction.quantity)
      stmt.setDouble(6, pt.transaction.price / pt.transaction.quantity)
      stmt.setDouble(7, pt.transaction.price)
      stmt.setString(8, pt.transaction.channel)
      stmt.setString(9, pt.transaction.paymentMethod)
      stmt.setString(10, pt.discountsAsJson)
      stmt.setDouble(11, pt.calculatedDiscount)
      stmt.setDouble(12, pt.finalPrice)

      //  insert and log results
      val rowsAffected = stmt.executeUpdate()
      Logger.log("INFO", pt.transaction.id,
        s"DB save successful. Rows affected: $rowsAffected"
      )
    }.recover {
      // database errors
      case e: Exception =>
        Logger.log("ERROR", pt.transaction.id,
          s"DB Error [${e.getMessage}]"
        )
        throw e
    }
  }

  //  handling application shutdown
  sys.addShutdownHook {
    if (!dataSource.isClosed) {
      Logger.log("INFO", "Closing database connection pool")
      dataSource.close()
    }
  }

  // 7. Resource management
  private implicit class UsingManager[T <: AutoCloseable](val resource: T) extends AnyVal {
    // Automatically closes resources after use
    def use[R](block: T => R): R = Using.resource(resource)(block)
  }
}