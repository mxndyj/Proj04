| Member           |
| ---------------- |
| <u>memberID</u>  |
| name             |
| phone            |
| email            |
| dateOfBirth      |
| emergencyContact |

| Ski Pass       |
| -------------- |
| <u>passID</u>  |
| memberID       |
| totalUses      |
| remainingUses  |
| purchaseTime   |
| experationDate |

| Equipment          |
| ------------------ |
| <u>equipmentID</u> |
| type               |
| size               |

| Rental          |
| --------------- |
| <u>rentalID</u> |
| memberID        |
| skiPassID       |
| equipmentID     |
| rentalTime      |
| returnStatus    |

| Lift        |
| ----------- |
| <u>name</u> |
| ability     |
| openTime    |
| closeTime   |
| status      |

| Trail         |
| ------------- |
| <u>name</u>   |
| startLocation |
| endLocation   |
| status        |
| difficulty    |
| category      |

| Lift-Trail       |
| ---------------- |
| <u>liftName</u>  |
| <u>trailName</u> |

| Lift Entry       |
| ---------------- |
| <u>skiPassID</u> |
| liftName         |
| <u>entryTime</u> |

| Ski Lesson      |
| --------------- |
| <u>lessonID</u> |
| type            |
| instructorID    |
| certifiedLevel  |
| isPrivate       |
| time            |

| Lesson Purchase   |
| ----------------- |
| <u>orderID</u>    |
| memberID          |
| lessonID          |
| totalSessions     |
| remainingSessions |

| Employee          |
| ----------------- |
| <u>employeeID</u> |
| position          |
| startDate         |
| salary            |
| name              |
| age               |
| sex               |
| ethnicity         |

| Property               |
| ---------------------- |
| <u>propertyID</u>      |
| type                   |
| dailyIncome            |

