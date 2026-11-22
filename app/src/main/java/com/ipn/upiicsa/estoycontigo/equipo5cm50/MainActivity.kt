package com.ipn.upiicsa.estoycontigo.equipo5cm50

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipn.upiicsa.estoycontigo.equipo5cm50.data.local.EstoyContigoDatabase
import com.ipn.upiicsa.estoycontigo.equipo5cm50.data.repository.MoodRepository
import com.ipn.upiicsa.estoycontigo.equipo5cm50.ui.mood.MoodScreen
import com.ipn.upiicsa.estoycontigo.equipo5cm50.ui.mood.MoodViewModel
import com.ipn.upiicsa.estoycontigo.equipo5cm50.ui.theme.EstoyContigoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = EstoyContigoDatabase.getInstance(applicationContext)
        val repository = MoodRepository(database.moodDao())

        setContent {
            EstoyContigoTheme {
                val moodViewModel: MoodViewModel = viewModel(
                    factory = MoodViewModel.Factory(repository)
                )
                MoodScreen(viewModel = moodViewModel)
            }
        }
    }
}