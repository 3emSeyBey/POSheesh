package com.appdev.posheesh

import PrintActivity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.appdev.posheesh.Classes.FragmentChangeListener
import com.appdev.posheesh.databinding.ActivityMainBinding
import com.appdev.posheesh.ui.sales.ExcelHandler
import com.squareup.picasso.BuildConfig
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity(), FragmentChangeListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var currentMenu: Menu? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_dashboard, R.id.nav_sales, R.id.nav_inventory, R.id.nav_reports, R.id.nav_about), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
    override fun onFragmentChanged(fragmentTag: String) {
        if (fragmentTag == "Sales") {
            currentMenu?.clear() // Clear existing menu items
            menuInflater.inflate(R.menu.sales_menu, currentMenu)
        } else {
            currentMenu?.clear() // Clear existing menu items
            menuInflater.inflate(R.menu.dashboard_menu, currentMenu)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        // Save the reference to the menu object
        currentMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item selection
        when (item.itemId) {
            R.id.sales_menu_export -> {
                val dbHelper = DatabaseHandler(this)
                val database = dbHelper.writableDatabase
                val excelHandler = ExcelHandler()

                // Export the Excel file to internal storage
                val file = excelHandler.exportToExcel(this, database)

                // Copy the file to external storage
                val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val copiedFile = File(externalDir, "data.xlsx")
                try {
                    file.copyTo(copiedFile, true)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.println(Log.ERROR, "Error", "Error copying file to external storage"+e.message)
                    Toast.makeText(this, "Error copying file to external storage", Toast.LENGTH_SHORT).show()
                    return true
                }

                // Get the URI of the copied file
                val uri: Uri = FileProvider.getUriForFile(this, "com.appdev.posheesh.fileprovider", copiedFile)

                // Create an intent to view the Excel file
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                // Start the activity to view the Excel file
                startActivity(intent)
            }

            R.id.sales_menu_print -> {
                val printExcelActivity = PrintActivity()
                val dbHelper = DatabaseHandler(this)
                val database = dbHelper.writableDatabase
                val file = ExcelHandler().exportToExcel(this, database)
                printExcelActivity.printExcelFile(this, file)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}