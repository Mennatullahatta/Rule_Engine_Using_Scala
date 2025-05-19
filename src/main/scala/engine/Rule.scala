package engine

import java.time.{LocalDate, LocalDateTime}
import io.circe.syntax._
import io.circe.generic.semiauto._

case class ProductDetails(
                           name: String,
                           expiryDate: LocalDate)

case class RawTransaction(
                           timestamp: LocalDateTime,
                           productName: String,
                           expiryDate: LocalDate,
                           quantity: Int,
                           unitPrice: Double,
                           channel: String,
                           paymentMethod: String)

case class Transaction(
                        id: String,
                        product: ProductDetails,
                        transactionDate: LocalDate,
                        quantity: Int,
                        price: Double,
                        channel: String,
                        paymentMethod: String)


case class DiscountReason(
                           ruleName: String,
                           discount: Double)


case class ProcessedTransaction(
                                 transaction: Transaction,
                                 appliedDiscounts: List[DiscountReason],
                                 calculatedDiscount: Double,
                                 finalPrice: Double
                               )
{

  // Converts discounts to JSON format for storage
  def discountsAsJson: String = {
    implicit val discountReasonEncoder = deriveEncoder[DiscountReason]
    appliedDiscounts.asJson.noSpaces
  }
}