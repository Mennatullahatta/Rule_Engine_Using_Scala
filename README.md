#  Rule-Based Discount Engine

A functional Scala engine for processing retail transactions and applying business discount rules in a pure, predictable, and composable way.
This project is part of a functional programming journey focused on building reliable, readable, and pure logic in Scala.

---

##  Problem Statement

A major retail store requires a discount engine that can automatically evaluate transactions based on predefined business rules and calculate the final discounted price with precision and consistency.

---

##  Business Rules

### 1. Expiration Discount
If the product will expire within 30 days from the transaction date:
- 29 days remaining → 1% discount  
- 28 days remaining → 2% discount  
- ...

### 2. Cheese and Wine Sale
- Cheese → 10% discount  
- Wine → 5% discount

### 3. March 23 Special
- Transactions on **March 23** get **50% discount**.

### 4. Bulk Purchase Discount
- 6–9 units → 5%  
- 10–14 units → 7%  
- 15+ units → 10%

### 5. App Usage Discount
If sold via App:
- Quantity rounded up to the nearest 5  
- Each 5 units → 5%  
  - (e.g. quantity = 1–5 → 5%, 6–10 → 10%, etc.)

### 6. Visa Payment Discount
- If paid by **Visa** → 5% discount

---

## Discount Logic

- A transaction that qualifies for **more than one** discount will get the **top 2 discounts averaged**.
- If no rules are matched, the transaction gets **0% discount**.
- Final price = original price - calculated discount.

---

## Tech Highlights

- Pure functional Scala
- No `var`, no mutable data, no loops
- Predictable behavior with clean and readable code
- Transaction logging in `rules_engine.log`  
- Format: `TIMESTAMP | LEVEL | MESSAGE`

---

## Project Structure

```bash
src/
├── main/
│   ├── scala/
│   │   └── engine/
│   │       ├── CSVWriter.scala        # Handles CSV output
│   │       ├── DBWriter.scala         # Placeholder for DB logic
│   │       ├── Logger.scala           # Logging utilities
│   │       ├── Main.scala             # Entry point
│   │       ├── Rule.scala             # Rule definition trait
│   │       └── RuleEngine.scala       # Rule evaluation logic
│   └── resources/
│       └── TRX1000.csv                # Input CSV file with transactions
output/ 
└── rule_engine.log                    # Log file generated during execution
└── processedOrders.csv                # Final CSV file with discounted transactions
```

---



## Output

Processed transactions include:
- Final calculated discount
- Final price after applying the discount
- Applied discount reasons (if any)
- Results can be saved into a database
- Engine logs every operation in `rules_engine.log`

---

##  How to Run

1. Open the project using **IntelliJ IDEA**
2. Make sure Scala and SBT are set up
3. Navigate to the main application file (e.g., `Main.scala`)
4. Run the application
5. Check:
   - Output in the processedOrders file or even DB
   - Logs in the `rules_engine.log` file

---


### Author
Developed by [Mennatullah Atta](https://github.com/Mennatullahatta)



