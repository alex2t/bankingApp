package com.example.atm_osphere.view.postLogin
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atm_osphere.model.TransactionWithPayee
import java.util.Locale
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt

@Composable
fun TransactionItem(
    transaction: TransactionWithPayee,
    onTransactionSelected: (TransactionWithPayee) -> Unit,
    onTransactionDeleted: (TransactionWithPayee) -> Unit
) {
    // Define colors based on transaction type (debit or credit)
    val transactionColor = if (transaction.transactionType == "credit") Color(0xFF4CAF50) else Color(0xFFF44336)

    // State to track swipe offset
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = with(LocalDensity.current) { 100.dp.toPx() } // 100dp

    // Detect swipe gesture
    val modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp, horizontal = 4.dp)
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    Log.d("TransactionItem", "TransactionTem before if: $transaction")
                    if (offsetX < -swipeThreshold) {
                        Log.d("TransactionItem", "TransactionItem: ${transaction}")
                        onTransactionDeleted(transaction)
                    }
                    offsetX = 0f // Reset offset
                },
                onHorizontalDrag = { _, dragAmount ->
                    offsetX += dragAmount
                    Log.d("TransactionItem", "offsetX: $offsetX")
                }
            )
        }
        .clickable {
            Log.d("TransactionItem", "clickable")
            onTransactionSelected(transaction)
        }

    // Card composable for better visual styling
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .offset { IntOffset(offsetX.roundToInt(), 0) }, // Apply swipe offset
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction name
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = transaction.payeeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = transaction.transactionType.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = transactionColor,
                        fontSize = 14.sp
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Transaction amount
            Text(
                text = "$${transaction.amount}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = transactionColor,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }
    }
}

