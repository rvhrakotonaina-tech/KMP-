import androidx.room.TypeConverter
import com.example.moneytracker.data.model.TransactionType
import com.example.moneytracker.data.model.LoanDebtType
import com.example.moneytracker.data.model.LoanDebtStatus

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }

    @TypeConverter
    fun fromLoanDebtType(value: LoanDebtType?): String? = value?.name

    @TypeConverter
    fun toLoanDebtType(value: String?): LoanDebtType? = value?.let { LoanDebtType.valueOf(it) }

    @TypeConverter
    fun fromLoanDebtStatus(value: LoanDebtStatus?): String? = value?.name

    @TypeConverter
    fun toLoanDebtStatus(value: String?): LoanDebtStatus? = value?.let { LoanDebtStatus.valueOf(it) }
}
