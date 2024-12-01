package com.ph32395.staynow.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.ph32395.staynow.R

class MessageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)

        // Ánh xạ CardView
        val fchatwithadmin: CardView = view.findViewById(R.id.fchatwithadmin)

        fchatwithadmin.setOnClickListener {
            //chuyển đến màn Chat
        }

        return view
    }
}
