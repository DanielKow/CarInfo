package me.daniel.carinfo

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.EnergyLevel
import androidx.car.app.hardware.info.EnergyProfile
import androidx.car.app.hardware.info.Mileage
import androidx.car.app.hardware.info.Model
import androidx.car.app.hardware.info.Speed
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import kotlin.random.Random

class MainScreen(carContext: CarContext) : Screen(carContext) {

    private var car: String = "Car"
    private var fuelTypes: String = ""
    private var fuelLevel: String = ""
    private var mileage: String = ""

    override fun onGetTemplate(): Template {
        val carInfo = carContext.getCarService(CarHardwareManager::class.java).carInfo
        val modelListener = OnCarDataAvailableListener<Model> { data ->
            car = data.year.value.toString() + " " + data.manufacturer.value + " " + data.name.value
            invalidate()
        }
        val energyProfileListener = OnCarDataAvailableListener<EnergyProfile> { data ->
            fuelTypes = data.fuelTypes.value?.joinToString(separator = ", ") { FUELS[it] ?: "" } ?: ""
            invalidate()
        }
        val speedListener = OnCarDataAvailableListener<Speed> { data ->
            val metersPerSecond = data.rawSpeedMetersPerSecond.value ?: 0f
            val kilometersPerHour = metersPerSecond * 3.6f

            if (kilometersPerHour > 0) {
                screenManager.push(DrivingScreen(carContext))
            }

            invalidate()
        }
        val mileageListener = OnCarDataAvailableListener<Mileage> { data ->
            val traveledMeters = data.odometerMeters.value ?: 0f
            val traveledKilometers = traveledMeters / 1000
            mileage = "$traveledKilometers km"

            invalidate()
        }
        val energyLevelListener = OnCarDataAvailableListener<EnergyLevel> {  data ->
            val fuelPercentage = data.fuelPercent.value ?: 0f
            fuelLevel = "$fuelPercentage%"

            if (data.energyIsLow.value != false) {
                fuelLevel += " You should visit gas station!"
            }

            invalidate()
        }

        carInfo.fetchModel(carContext.mainExecutor, modelListener)
        carInfo.fetchEnergyProfile(carContext.mainExecutor, energyProfileListener)
        carInfo.addSpeedListener(carContext.mainExecutor, speedListener)
        carInfo.addMileageListener(carContext.mainExecutor, mileageListener)
        carInfo.addEnergyLevelListener(carContext.mainExecutor, energyLevelListener)

        var fuelsRowBuilder = Row.Builder().setTitle("Fuel").addText(fuelTypes).addText(fuelLevel)
        val fuelsRow = fuelsRowBuilder.build()
        val mileageRow = Row.Builder().setTitle("Mileage").addText(mileage).build()
        val pane = Pane.Builder().addRow(fuelsRow).addRow(mileageRow).build()
        return PaneTemplate.Builder(pane).setHeaderAction(Action.APP_ICON).setTitle(car).build()
    }

    companion object{
        private val FUELS = mapOf(
            0 to "-",
            1 to "Unleaded gasoline",
            2 to "Leaded gasoline",
            3 to "First grade diesel",
            4 to "Second grade diesel",
            5 to "Biodiesel",
            6 to "E85",
            7 to "LPG",
            8 to "CNG",
            9 to "LNG",
            10 to "Electric",
            11 to "Hydrogen",
            12 to "Other"
        )
    }

}