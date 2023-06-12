package me.daniel.carinfo

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class MySession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return RequestPermissionsScreen(carContext)
    }
}