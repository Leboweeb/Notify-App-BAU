package com.example.notify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.Gson

class StudentInfoFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRefresh(
            view,
            R.id.student_info_refresh,
            requireContext()
        )
        val userName = view.findViewById<TextView>(R.id.student_name)
        val userId = view.findViewById<TextView>(R.id.student_id)
        val programValue = view.findViewById<TextView>(R.id.program_value)
        val cgpa = view.findViewById<TextView>(R.id.CGPA_value)
        val sgpa = view.findViewById<TextView>(R.id.SGPA_value)
        val creditsValue = view.findViewById<TextView>(R.id.Credits_value)
        val userAvatar = view.findViewById<ImageView>(R.id.def_user_photo)
        val localApi = LocalApi(requireContext())
        localApi.postResponseToListener(
                forceNetwork = false,
                localApi::getXFilesInfo,   {
                info : String ->
                    Gson().fromJson(info, XFilesInfo::class.java)
                }, "iconnect/xfiles"
        ).observe(getViewLifecycleOwner()) { xFilesInfo ->
            userName.text = xFilesInfo.studentName
            userId.text = xFilesInfo.studentID
            userAvatar.setImageBitmap(b64ToBitmap(xFilesInfo.studentImage))
            programValue.text = xFilesInfo.program
            cgpa.text = xFilesInfo.cgpa
            sgpa.text = xFilesInfo.sgpa
            creditsValue.text = xFilesInfo.creditsCompleted
        }
    }

}