package com.appdev.posheesh

import PrintActivity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.appdev.posheesh.Classes.FragmentChangeListener
import com.appdev.posheesh.databinding.ActivityMainBinding
import com.appdev.posheesh.ui.sales.ExcelHandler


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
                val file = ExcelHandler().exportToExcel(this, database)
                val fileString = file.toString()
                val uri: Uri = Uri.parse(fileString)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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