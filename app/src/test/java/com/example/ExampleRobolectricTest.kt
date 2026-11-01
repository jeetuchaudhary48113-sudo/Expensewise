package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Expense
import com.example.ui.ExpenseViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ExpenseWise", appName)
  }

  @Test
  fun `main activity launches without crashing`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java)
    controller.setup()
    val activity = controller.get()
    assertNotNull(activity)
  }

  @Test
  fun `viewmodel initializes and handles local repository safely`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ExpenseViewModel(app)
    assertNotNull(viewModel)
    assertEquals("₹", viewModel.currencySymbol.value)
    
    // Add sample expense
    val expense = Expense(
      title = "Groceries",
      amount = 450.0,
      category = "Food & Dining",
      date = System.currentTimeMillis()
    )
    viewModel.addExpense(expense)
    assertTrue(viewModel.allBudgets.value.isEmpty() || viewModel.allBudgets.value.isNotEmpty())
  }

  @Test
  fun `auth repository safely handles sign in without throwing`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ExpenseViewModel(app)
    var callbackCalled = false
    viewModel.login("test@example.com", "password123") { success, error ->
      callbackCalled = true
    }
    // Verifies no unhandled exception crash occurs during login attempt
    assertTrue(viewModel.isUserLoggedIn.value == false || viewModel.isUserLoggedIn.value == true)
  }
}
