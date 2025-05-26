package com.example.notify

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ir.androidexception.datatable.DataTable
import ir.androidexception.datatable.model.DataTableHeader
import ir.androidexception.datatable.model.DataTableRow
import java.util.Random

class FinancialInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view?.let { populateTable(requireContext(), it) }
        return inflater.inflate(R.layout.fragment_financial_info, container, false)
    }


    private fun populateTable(context: Context,view: View) {

        val dataTable: DataTable = view.findViewById(R.id.data_table)

        val header: DataTableHeader = DataTableHeader.Builder()
            .item("field name",1)
            .item("field name",1)
            .item("field name",1)
            .item("field name",1)
            .build()

        val rows = ArrayList<DataTableRow>()

        // define 200 fake rows for table
        for (i in 0..199) {
            val r: Random = Random()
            val random: Int = r.nextInt(i + 1)
            val randomDiscount: Int = r.nextInt(20)
            val row = DataTableRow.Builder()
                .value("Product #$i")
                .value(random.toString())
                .value((random * 1000).toString() + "$")
                .value("$randomDiscount%")
                .build()
            rows.add(row)
        }

        dataTable.header = header
        dataTable.rows = rows
        dataTable.inflate(context)
    }
}