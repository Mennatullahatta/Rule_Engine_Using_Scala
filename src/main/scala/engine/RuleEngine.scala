package engine

import java.time.temporal.ChronoUnit
import java.time.Month

// discount rule logic
object RuleEngine {

  type Condition = Transaction => Boolean
  type DiscountCalculation = Transaction => Double

  // --------------------------
  // Conditions
  // --------------------------

  // Checks if product expires within 30 days
  private val expirationCondition: Condition = t =>
    ChronoUnit.DAYS.between(t.transactionDate, t.product.expiryDate) < 30 &&
      ChronoUnit.DAYS.between(t.transactionDate, t.product.expiryDate) >= 0


  // Identifies cheese and wine products
  private val cheeseAndWineCondition: Condition = t =>
    t.product.name.toLowerCase match {
      case name if name.contains("cheese") || name.contains("wine") => true
      case _ => false
    }

  // discount for March 23rd sales
  private val march23Condition: Condition = t =>
    t.transactionDate.getMonth == Month.MARCH &&
      t.transactionDate.getDayOfMonth == 23

  // Bulk purchase threshold (>5 items)
  private val bulkPurchaseCondition: Condition = t => t.quantity > 5

  // Mobile app usage
  private val appUsageCondition: Condition = t =>
    t.channel.trim.equalsIgnoreCase("app")

  // Visa card payment check
  private val visaCondition: Condition = t =>
    t.paymentMethod.replaceAll("[^a-zA-Z]", "").equalsIgnoreCase("visa")

  // --------------------------
  // Discount Calculations
  // --------------------------

  // 1% per day until expiry (max 30%)
  private val expirationDiscount: DiscountCalculation = t =>
    30 - ChronoUnit.DAYS.between(t.transactionDate, t.product.expiryDate)

  // Fixed discounts for specific product categories
  private val cheeseAndWineDiscount: DiscountCalculation = t =>
    t.product.name.toLowerCase match {
      case name if name.contains("cheese") => 10.0  // Cheese = 10%
      case name if name.contains("wine") => 5.0     // Wine = 5%
      case _ => 0.0
    }

  // 50% discount for March 23rd
  private val march23Discount: DiscountCalculation = _ => 50.0

  // Quantity based on bulk discounts
  private val bulkPurchaseDiscount: DiscountCalculation = t =>
    t.quantity match {
      case q if q >= 6 && q <= 9  => 5.0   // 6-9 units: 5%
      case q if q >= 10 && q <=14 => 7.0   // 10-14 units: 7%
      case q if q >= 15           => 10.0  // 15+ units: 10%
      case _ => 0.0
    }

  // App usage discount: 5% per 5 items
  private val appUsageDiscount: DiscountCalculation = t => {
    val multiplier = Math.ceil(t.quantity / 5.0).toInt
    multiplier * 5.0
  }

  // 5% discount for Visa payments
  private val visaDiscount: DiscountCalculation = _ => 5.0

  // --------------------------
  // Rule Configuration
  // --------------------------

  // Active discount rules with their conditions, calculations
  private val discountRules = List(
    (expirationCondition, expirationDiscount, "Expiration Discount"),
    (cheeseAndWineCondition, cheeseAndWineDiscount, "Cheese/Wine Discount"),
    (march23Condition, march23Discount, "March 23 Special"),
    (bulkPurchaseCondition, bulkPurchaseDiscount, "Bulk Purchase Discount"),
    (appUsageCondition, appUsageDiscount, "App Usage Promotion"),
    (visaCondition, visaDiscount, "Visa Card Promotion")
  )

  // --------------------------
  //  Processing Logic
  // --------------------------

  // Applies all rules to a transaction and collects eligible discounts
  def collectAllDiscounts(t: Transaction): List[DiscountReason] = {
    discountRules.flatMap { case (cond, calc, name) =>
      if (cond(t)) {
        val discount = calc(t)
        Logger.log("DEBUG", t.id, s"Applied $name: ${discount}%")
        Some(DiscountReason(name, discount))
      } else {
        Logger.log("TRACE", t.id, s"Skipped $name")
        None
      }
    }
  }

  // Calculates effective discount from top two highest discounts
  def calculateEffectiveDiscount(discounts: List[Double]): Double =
    discounts.sorted.reverse match {
      case top1 :: top2 :: _ => (top1 + top2) / 2  // Average of top two
      case top :: _ => top                         // Single discount
      case Nil => 0.0                              // No discounts
    }

  // Main processing pipeline for a transaction
  def processTransaction(t: Transaction): ProcessedTransaction = {
    Logger.log("INFO", t.id, "Processing started")

    // Apply discount rules
    val discounts = collectAllDiscounts(t)

    // Calculate final discount
    val effectiveDiscount = calculateEffectiveDiscount(discounts.map(_.discount))

    // Compute final price
    val finalPrice = t.price * (1 - (effectiveDiscount / 100.0))

    Logger.log("INFO", t.id,
      s"Final discount: $effectiveDiscount%, Final price: $finalPrice"
    )

    ProcessedTransaction(t, discounts, effectiveDiscount, finalPrice)
  }
}