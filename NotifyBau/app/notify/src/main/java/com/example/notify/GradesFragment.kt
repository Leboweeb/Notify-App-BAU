package com.example.notify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment

class GradesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grades, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRefresh(
            view,
            R.id.grades_refresh,
            requireContext()
        )
        postResponseToIconnect(requireContext(), viewLifecycleOwner) { info ->
            val composeView = view.findViewById<ComposeView>(R.id.gradesComposeLayout)
            val itemAmount = 10
            val menuItems = info.grades.map {
                Triple(it.course, it.credits, it.grade)
            }
            composeView.setContent {

                SimpleTable(
                    menuItems = menuItems.take(itemAmount)
                )
            }
        }
    }
}


@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    alignment: TextAlign = TextAlign.Center,
    title: Boolean = false
) {
    Text(
        text = text,
        Modifier
            .weight(weight)
            .padding(10.dp),
        fontWeight = if (title) FontWeight.Bold else FontWeight.Normal,
        textAlign = alignment,
    )
}


@Composable
fun SimpleTable(menuItems: List<Triple<String, Int, String>>) {

    // Each cell of a column must have the same weight.
    // eg val column1Weight = .7f which is 70% and so on
    val column1Weight = .3333f
    val column2Weight = .3333f
    val column3Weight = .3333f
    // The LazyColumn will be our table

    LazyColumn(
        Modifier
            .padding(16.dp)
            .fillMaxHeight(),
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4872A)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableCell(
                    text = "Course",
                    weight = column1Weight,
                    title = true
                )
                TableCell(text = "Credits", weight = column2Weight, title = true)
                TableCell(text = "Grade", weight = column3Weight, title = true)
            }
            Divider(
                color = Color.LightGray,
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
        }

        itemsIndexed(menuItems) { _, item ->

            val (title, credits, grade) = item

            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableCell(
                    text = title,
                    weight = column1Weight,
                )
                TableCell(text = credits.toString(), weight = column2Weight)
                TableCell(
                    text = grade,
                    weight = column3Weight,
                )
            }
            Divider(
                color = Color.LightGray,
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
        }

    }
}





