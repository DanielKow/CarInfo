package me.daniel.carinfo

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.EnergyLevel
import androidx.car.app.hardware.info.Mileage
import androidx.car.app.hardware.info.Speed
import androidx.car.app.model.CarIcon
import androidx.car.app.model.GridItem
import androidx.car.app.model.GridTemplate
import androidx.car.app.model.ItemList
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat


class DrivingScreen(carContext: CarContext) : Screen(carContext) {
    private var fuelLevel: Float? = null
    private var speed: Float? = null
    private var mileage: String = ""
    private var energyIsLow: Boolean = false

    override fun onGetTemplate(): Template {
        val carInfo = carContext.getCarService(CarHardwareManager::class.java).carInfo

        val speedListener = OnCarDataAvailableListener<Speed> { data ->
            val metersPerSecond = data.rawSpeedMetersPerSecond.value
            if (metersPerSecond != null) {
                val kilometersPerHour = metersPerSecond * 3.6f
                speed = kilometersPerHour

                if (speed == 0f) {
                    screenManager.pop()
                }
            }

            invalidate()
        }
        val mileageListener = OnCarDataAvailableListener<Mileage> { data ->
            val traveledMeters = data.odometerMeters.value ?: 0f
            val traveledKilometers = traveledMeters / 1000
            mileage = "$traveledKilometers km"

            mileage = "12213 km"

            invalidate()
        }
        val energyLevelListener = OnCarDataAvailableListener<EnergyLevel> { data ->
            fuelLevel = data.fuelPercent.value
            energyIsLow = data.energyIsLow.value ?: false

            invalidate()
        }

        carInfo.addSpeedListener(carContext.mainExecutor, speedListener)
        carInfo.addMileageListener(carContext.mainExecutor, mileageListener)
        carInfo.addEnergyLevelListener(carContext.mainExecutor, energyLevelListener)

        val itemListBuilder = ItemList.Builder().setNoItemsMessage("Waiting for data...")
        if (speed != null) {

            val speedGridItem = GridItem.Builder()
                .setImage(prepareCarIcon(R.drawable.speed))
                .setTitle(speed.toString()).setText("km/h")
                .build()

            itemListBuilder.addItem(speedGridItem)
        }


        if (fuelLevel != null) {
            val fuelLevelGridItem = GridItem.Builder()
                .setImage(prepareCarIcon(R.drawable.gas))
                .setTitle(fuelLevel.toString())
                .setText("%")
                .build()

            itemListBuilder.addItem(fuelLevelGridItem)
        }

        if (mileage.isNotBlank()) {
            val mileageGridItem = GridItem.Builder()
                .setImage(prepareCarIcon(R.drawable.mileage))
                .setTitle("Mileage")
                .setText(mileage)
                .build()

            itemListBuilder.addItem(mileageGridItem)
        }

        if (energyIsLow) {
            val energyIsLowGridItem = GridItem.Builder()
                .setImage(CarIcon.ERROR)
                .setTitle("Low fuel level!")
                .setText("Visit gas station")
                .build()
            itemListBuilder.addItem(energyIsLowGridItem)
        }


        val gridItems = itemListBuilder.build()
        return GridTemplate.Builder().setSingleList(gridItems).setTitle("Driving").build()
    }

    private fun prepareCarIcon(resource: Int): CarIcon {
        return CarIcon.Builder(IconCompat.createWithResource(carContext, resource)).build()
    }
}