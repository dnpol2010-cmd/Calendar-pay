package com.example.paymentschedule

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var etBankName: EditText
    private lateinit var etAmount: EditText
    private lateinit var btnPickDate: Button
    private lateinit var tvDateLabel: TextView
    private lateinit var btnAdd: Button
    private lateinit var rvPayments: RecyclerView
    private lateinit var tvTotal: TextView

    private val payments = mutableListOf<Payment>()
    private var selectedDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etBankName = findViewById(R.id.etBankName)
        etAmount = findViewById(R.id.etAmount)
        btnPickDate = findViewById(R.id.btnPickDate)
        tvDateLabel = findViewById(R.id.tvDateLabel)
        btnAdd = findViewById(R.id.btnAdd)
        rvPayments = findViewById(R.id.rvPayments)
        tvTotal = findViewById(R.id.tvTotal)

        rvPayments.layoutManager = LinearLayoutManager(this)
        rvPayments.adapter = PaymentAdapter()

        btnPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                val cal = Calendar.getInstance().apply { set(y, m, d) }
                selectedDate = cal.time
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                tvDateLabel.text = "Дата: ${sdf.format(selectedDate)}"
            }, year, month, day).show()
        }

        btnAdd.setOnClickListener {
            val bankName = etBankName.text.toString().trim()
            if (bankName.isEmpty()) {
                etBankName.error = "Укажите банк"
                return@setOnClickListener
            }
            val amountStr = etAmount.text.toString().trim()
            if (amountStr.isEmpty()) {
                etAmount.error = "Укажите сумму"
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull() ?: run {
                etAmount.error = "Некорректная сумма"
                return@setOnClickListener
            }
            if (selectedDate == null) {
                tvDateLabel.error = "Выберите дату"
                return@setOnClickListener
            }

            payments.add(Payment(bankName, amount, selectedDate!!))
            updateTotal()
            (rvPayments.adapter as? PaymentAdapter)?.notifyDataSetChanged()
            etBankName.text.clear()
            etAmount.text.clear()
            tvDateLabel.text = "Дата: не выбрана"
            selectedDate = null
        }
    }

    private fun updateTotal() {
        val total = payments.sumOf { it.amount }
        tvTotal.text = "Общая сумма задолженности: ${String.format(Locale.getDefault(), "%,.2f", total)} ₽"
    }

    inner class PaymentAdapter : RecyclerView.Adapter<PaymentViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment, parent, false)
            return PaymentViewHolder(view)
        }

        override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
            val p = payments[position]
            holder.tvBank.text = p.bankName
            holder.tvAmount.text = String.format(Locale.getDefault(), "Сумма: %, .2f ₽", p.amount)
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            holder.tvDate.text = "Дата: ${sdf.format(p.date)}"
        }

        override fun getItemCount() = payments.size
    }

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBank: TextView = itemView.findViewById(R.id.tvBankNameItem)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmountItem)
        val tvDate: TextView = itemView.findViewById(R.id.tvDateItem)
    }

    data class Payment(val bankName: String, val amount: Double, val date: Date)
}
