package com.example.bettermoodle;

import static com.example.bettermoodle.LocalApiKt.getLocalApi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class NotificationsFragment extends Fragment implements MenuProvider {

    ListView listView;
    SearchView searchView;
    ArrayAdapter<String> arrayAdapter;
    JsonInterface jsonInterface;
    LocalApi localApi;
//    test value:
//    String[] notificationslist = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.jsonInterface = new JsonInterface(context);
        this.localApi = getLocalApi(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        // this.jsonInterface = new JsonInterface();
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    //List of Notification Code:
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = requireView().findViewById(R.id.listView);
        searchView = requireView().findViewById(R.id.SearchView);
        jsonInterface.postResponseToListener(localApi::getMoodleNotifications, (notificationsJson) -> {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Notification>>() {
            }.getType();
            return gson.fromJson(notificationsJson, listType);
        }, "moodle/notifications").observe(getViewLifecycleOwner(), notificationList -> {
            String[] notificationsStrings = notificationList.stream().map(Notification::toString).toArray(String[]::new);
            arrayAdapter = new ArrayAdapter<>(requireActivity(), R.layout.list_custometext, notificationsStrings);
            listView.setAdapter(arrayAdapter);
        });
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Intent intent = new Intent(getActivity(), IndividualAnnouncement.class);
            String notificationMessage = (String) listView.getItemAtPosition(position);
            intent.putExtra("message", notificationMessage);
            startActivity(intent);
        });
    }

    //Search Bar Code:
    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.notifcations_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.Search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                arrayAdapter.getFilter().filter(newText);
                return false;
            }
        });

    }

    //Function that takes you to the individual notification
    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

}