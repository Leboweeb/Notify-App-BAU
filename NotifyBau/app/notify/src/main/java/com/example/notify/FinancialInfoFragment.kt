package com.example.notify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Locale

class FinancialInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_financial_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRefresh(
            view,
            R.id.financial_info_refresh,
            requireContext()
        )
        postResponseToIconnect(
            requireContext(),
            viewLifecycleOwner
        ) { info ->

            val userAvatar = view.findViewById<ImageView>(R.id.user_image)
            val userName = view.findViewById<TextView>(R.id.user_name)
            val lbpRemaining = view.findViewById<TextView>(R.id.lbp_amount)
            val usdRemaining = view.findViewById<TextView>(R.id.usd_amount)
            val holdsLayout = view.findViewById<LinearLayout>(R.id.holds_layout)
            userAvatar.setImageBitmap(b64ToBitmap(info.studentImage))
            userName.text = info.studentName
            lbpRemaining.text = String.format(Locale.ROOT,"%d",info.remainingPaymentsLBP)
            usdRemaining.text = String.format(Locale.ROOT,"%d",info.remainingPaymentsUSD)
            for(hold in info.holds) {
                val childText = TextView(requireContext())
                childText.text = hold
                holdsLayout.addView(
                    childText
                )
            }

        }
    }


}