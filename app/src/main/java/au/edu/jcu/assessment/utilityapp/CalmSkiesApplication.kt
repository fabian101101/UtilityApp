package au.edu.jcu.assessment.utilityapp

import android.app.Application
import au.edu.jcu.assessment.utilityapp.di.AppContainer
import au.edu.jcu.assessment.utilityapp.di.DefaultAppContainer

/** Application class that owns the dependency container for the whole process. */
class CalmSkiesApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
