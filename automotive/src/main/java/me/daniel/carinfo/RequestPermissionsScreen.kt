package me.daniel.carinfo

import androidx.car.app.CarContext
import androidx.car.app.OnRequestPermissionsListener
import androidx.car.app.Screen
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class RequestPermissionsScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val requestPermissionListener = OnRequestPermissionsListener {grantedPermissions, rejectedPermissions ->
            screenManager.push(MainScreen(carContext))
        }
        carContext.requestPermissions(listOf("com.google.android.gms.permission.CAR_FUEL", "com.google.android.gms.permission.CAR_SPEED", "com.google.android.gms.permission.CAR_MILEAGE"), requestPermissionListener)

        val waitingRow = Row.Builder().setTitle("Waiting for permissions...").build()
        val pane = Pane.Builder().addRow(waitingRow).build()
        return PaneTemplate.Builder(pane).setTitle("Permissions").build()
    }

}