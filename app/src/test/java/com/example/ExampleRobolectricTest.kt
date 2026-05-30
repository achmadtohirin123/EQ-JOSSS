package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.AudioProcessingViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BRO EQ JOSSS", appName)
  }

  @Test
  fun `test viewModel initialization`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = AudioProcessingViewModel(application)
    assert(viewModel.selectedTab.value == "HOME")
  }
}
