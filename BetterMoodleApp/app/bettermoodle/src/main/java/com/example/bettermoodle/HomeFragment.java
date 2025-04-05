package com.example.bettermoodle;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

public class HomeFragment extends Fragment {

    CardView StudentInfoCard;
    CardView RecentCard;
    CardView AssignmentsCard;
    CardView CoursesCard;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView userName = view.findViewById(R.id.student_name);
        TextView userId = view.findViewById(R.id.student_id);
        TextView programValue = view.findViewById(R.id.program_value);
        TextView cgpa = view.findViewById(R.id.CGPA_value);
        TextView sgpa = view.findViewById(R.id.SGPA_value);
        TextView creditsValue = view.findViewById(R.id.Credits_value);
        ImageView userAvatar = view.findViewById(R.id.def_user_photo);
        LocalApi localApi = new LocalApi(requireContext());
        localApi.postResponseToListener(
                localApi::getXFilesInfo, (xfilesinfoString) -> {
                    Gson gson = new Gson();
                    return gson.fromJson(xfilesinfoString, XFilesInfo.class);
                }, "iconnect/xfiles"
        ).observe(getViewLifecycleOwner(), xFilesInfo -> {
            userName.setText(xFilesInfo.getStudentName());
            userId.setText(xFilesInfo.getStudentID());
            userAvatar.setImageBitmap(UtilsKt.b64ToBitmap(xFilesInfo.getStudentImage()));
            programValue.setText(xFilesInfo.getProgram());
            cgpa.setText(xFilesInfo.getCgpa());
            sgpa.setText(xFilesInfo.getSgpa());
            creditsValue.setText(xFilesInfo.getCreditsCompleted());
        });
    }


}