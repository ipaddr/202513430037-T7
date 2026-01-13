package com.contoh.eticketbus2025.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.eticketbus2025.R
import com.contoh.eticketbus2025.data.model.BusModel
import com.contoh.eticketbus2025.data.source.FirestoreHelper
import com.contoh.eticketbus2025.ui.booking.SeatSelectionActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class BusListActivity : AppCompatActivity() {

    private var isRoundTrip = false
    private var selectedDepartBus: BusModel? = null
    private var activeRawList: List<BusModel> = listOf()
    private var selectedSortType = "PRICE_ASC"
    private var selectedClassFilter = "ALL"

    private var originCity = ""
    private var destCity = ""

    // UI Components
    private lateinit var rvBusList: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmpty: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_bus_list)

        // Init UI
        rvBusList = findViewById(R.id.rvBusList)
        progressBar = findViewById(R.id.progressBar)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        rvBusList.layoutManager = LinearLayoutManager(this)

        originCity = intent.getStringExtra("ORIGIN") ?: "Padang"
        destCity = intent.getStringExtra("DESTINATION") ?: "Jakarta"
        val dateDepart = intent.getStringExtra("DATE") ?: "-"
        val dateReturn = intent.getStringExtra("DATE_RETURN") ?: "-"
        val passengers = intent.getStringExtra("PASSENGERS") ?: "-"
        isRoundTrip = intent.getBooleanExtra("IS_ROUND_TRIP", false)

        findViewById<TextView>(R.id.tvSummaryRoute).text = "$originCity → $destCity"
        findViewById<TextView>(R.id.tvSummaryDateDepart).text = dateDepart
        findViewById<TextView>(R.id.tvSummaryPassengers).text = passengers

        val rowReturn = findViewById<LinearLayout>(R.id.rowReturnDate)
        if (isRoundTrip) {
            rowReturn.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvSummaryDateReturn).text = dateReturn
        } else {
            rowReturn.visibility = View.GONE
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { handleBackNavigation() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        })

        findViewById<ImageButton>(R.id.btnFilter).setOnClickListener {
            showFilterBottomSheet()
        }

        // Load Data
        loadBusFromCloud(isDepartStep = true)
    }

    private fun loadBusFromCloud(isDepartStep: Boolean) {
        val tvTitle = findViewById<TextView>(R.id.tvPageTitle)
        val searchOrigin = if (isDepartStep) originCity else destCity
        val searchDest = if (isDepartStep) destCity else originCity

        tvTitle.text = if (isDepartStep) "Pilih Bus Pergi" else "Pilih Bus Pulang"

        progressBar.visibility = View.VISIBLE
        rvBusList.visibility = View.GONE
        layoutEmpty.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            var buses = FirestoreHelper.searchBuses(searchOrigin, searchDest)
            if (buses.isEmpty()) {
                buses = FirestoreHelper.getAllBuses() // Fallback data
            }

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE

                if (buses.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    rvBusList.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    rvBusList.visibility = View.VISIBLE
                    activeRawList = buses
                    applyFilterAndSort()
                }
            }
        }
    }

    private fun updateHeaderRoute(from: String, to: String) {
        findViewById<TextView>(R.id.tvSummaryRoute).text = "$from → $to"
    }

    private fun handleBackNavigation() {
        if (isRoundTrip && selectedDepartBus != null) {
            selectedDepartBus = null
            updateHeaderRoute(originCity, destCity)
            findViewById<View>(R.id.cardSelectedDepart).visibility = View.GONE
            loadBusFromCloud(isDepartStep = true)

            rvBusList.animate().translationX(rvBusList.width.toFloat()).alpha(0f).setDuration(200)
                .withEndAction {
                    rvBusList.translationX = -rvBusList.width.toFloat()
                    rvBusList.animate().translationX(0f).alpha(1f).setDuration(200).start()
                }.start()

            Toast.makeText(this, "Kembali ke bus keberangkatan", Toast.LENGTH_SHORT).show()
        } else {
            finish()
        }
    }

    private fun handleBusSelection(bus: BusModel) {
        if (isRoundTrip) {
            if (selectedDepartBus == null) {
                selectedDepartBus = bus
                rvBusList.animate().translationX(-rvBusList.width.toFloat()).alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        updateHeaderRoute(destCity, originCity)
                        showSelectedDepartCard(bus)
                        loadBusFromCloud(isDepartStep = false)
                        selectedSortType = "PRICE_ASC"
                        selectedClassFilter = "ALL"
                        rvBusList.translationX = rvBusList.width.toFloat()
                        rvBusList.animate().translationX(0f).alpha(1f).setDuration(300).start()
                    }.start()
            } else {
                proceedToNextBatch(selectedDepartBus!!, bus)
            }
        } else {
            proceedToNextBatch(bus, null)
        }
    }

    private fun showSelectedDepartCard(bus: BusModel) {
        val cardSelected = findViewById<View>(R.id.cardSelectedDepart)
        val tvName = findViewById<TextView>(R.id.tvSelectedDepartName)
        val tvTime = findViewById<TextView>(R.id.tvSelectedDepartTime)

        tvName.text = "${bus.operatorName} - ${bus.busClass}"
        tvTime.text = bus.departTime

        cardSelected.alpha = 0f
        cardSelected.visibility = View.VISIBLE
        cardSelected.animate().alpha(1f).setDuration(300).start()
    }

    private fun proceedToNextBatch(departBus: BusModel, returnBus: BusModel?) {
        val intent = Intent(this, SeatSelectionActivity::class.java)
        intent.putExtra("BUS_DATA", departBus)
        if (returnBus != null) {
            intent.putExtra("BUS_NEXT_DATA", returnBus)
        }
        intent.putExtra("PASSENGERS", this.intent.getStringExtra("PASSENGERS"))
        intent.putExtra("ORIGIN", originCity)
        intent.putExtra("DESTINATION", destCity)
        intent.putExtra("DATE", this.intent.getStringExtra("DATE"))
        intent.putExtra("DATE_RETURN", this.intent.getStringExtra("DATE_RETURN"))
        intent.putExtra("IS_ROUND_TRIP", isRoundTrip)
        startActivity(intent)
    }

    private fun applyFilterAndSort() {
        var processedList = if (selectedClassFilter == "ALL") {
            activeRawList
        } else {
            activeRawList.filter { it.busClass.contains(selectedClassFilter, ignoreCase = true) }
        }

        processedList = when (selectedSortType) {
            "PRICE_ASC" -> processedList.sortedBy { it.price }
            "TIME_ASC" -> processedList.sortedBy { it.departTime }
            "TIME_DESC" -> processedList.sortedByDescending { it.departTime }
            else -> processedList
        }
        updateRecyclerView(processedList)
    }

    private fun updateRecyclerView(data: List<BusModel>) {
        val adapter = BusAdapter(data) { bus ->
            handleBusSelection(bus)
        }
        rvBusList.adapter = adapter
    }

    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_filter, null)
        dialog.setContentView(view)

        val chipGroupSort = view.findViewById<ChipGroup>(R.id.chipGroupSort)
        val chipGroupClass = view.findViewById<ChipGroup>(R.id.chipGroupClass)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilter)
        val btnReset = view.findViewById<TextView>(R.id.btnResetFilter)

        btnReset.setOnClickListener {
            chipGroupSort.check(R.id.chipSortPriceAsc)
            chipGroupClass.check(R.id.chipClassAll)
        }

        btnApply.setOnClickListener {
            selectedSortType = when (chipGroupSort.checkedChipId) {
                R.id.chipSortTimeAsc -> "TIME_ASC"
                R.id.chipSortTimeDesc -> "TIME_DESC"
                else -> "PRICE_ASC"
            }
            selectedClassFilter = when (chipGroupClass.checkedChipId) {
                R.id.chipClassExecutive -> "Executive"
                R.id.chipClassRoyal -> "Royal"
                R.id.chipClassSutan -> "Sutan"
                else -> "ALL"
            }
            applyFilterAndSort()
            dialog.dismiss()
        }
        dialog.show()
    }

    // =================================================================
    // BUS ADAPTER (FIXED FOR item_bus.xml)
    // =================================================================

    class BusAdapter(
        private val busList: List<BusModel>,
        private val onClick: (BusModel) -> Unit
    ) : RecyclerView.Adapter<BusAdapter.BusViewHolder>() {

        inner class BusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            // MAPPING ID SESUAI item_bus.xml
            val tvOperator: TextView = view.findViewById(R.id.tvOperatorName)
            val tvClass: TextView = view.findViewById(R.id.tvClass)
            val tvRating: TextView = view.findViewById(R.id.tvRating)
            val tvTimeDepart: TextView = view.findViewById(R.id.tvTimeDepart)
            val tvTimeArrive: TextView = view.findViewById(R.id.tvTimeArrive)
            val tvDuration: TextView = view.findViewById(R.id.tvDuration)

            val chipContainer: LinearLayout =
                view.findViewById(R.id.layoutFacilities) // ID sesuai XML

            val tvPrice: TextView = view.findViewById(R.id.tvPrice) // ID sesuai XML
            val tvSeat: TextView = view.findViewById(R.id.tvSeats)  // ID sesuai XML
            val btnBook: Button = view.findViewById(R.id.btnBook)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
            // PENTING: Gunakan layout item_bus.xml
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus, parent, false)
            return BusViewHolder(view)
        }

        override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
            val bus = busList[position]
            val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            holder.tvOperator.text = bus.operatorName
            holder.tvClass.text = bus.busClass
            holder.tvRating.text = bus.rating.toString()
            holder.tvTimeDepart.text = bus.departTime
            holder.tvTimeArrive.text = bus.arriveTime
            holder.tvDuration.text = bus.duration
            holder.tvPrice.text = formatRp.format(bus.price)
            holder.tvSeat.text = "Sisa ${bus.seatAvailable} Kursi"

            // Setup Fasilitas (Chip)
            holder.chipContainer.removeAllViews()
            bus.facilities.take(3).forEach { facility ->
                val textView = TextView(holder.itemView.context)
                textView.text = facility
                textView.textSize = 10f
                textView.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.primary_blue
                    )
                )
                textView.background =
                    ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_facility_chip)
                textView.setPadding(12, 4, 12, 4)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = 8
                textView.layoutParams = params

                holder.chipContainer.addView(textView)
            }

            holder.itemView.setOnClickListener { onClick(bus) }
            holder.btnBook.setOnClickListener { onClick(bus) }
        }

        override fun getItemCount() = busList.size
    }
}