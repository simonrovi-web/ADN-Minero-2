package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ADN Minero", appName)
  }

  @Test
  fun `verify initial mining viewmodel state and controls`() {
    val viewModel = com.example.viewmodel.MiningViewModel()
    val initialState = viewModel.uiState.value
    assertEquals(com.example.model.NavDestination.INICIO, initialState.currentDestination)
    assertEquals(false, initialState.isModoFaena)
    assertEquals(8, initialState.panels.size)

    // Test Modo Faena Toggle
    viewModel.toggleModoFaena()
    assertEquals(true, viewModel.uiState.value.isModoFaena)

    // Test Navigation
    viewModel.setDestination(com.example.model.NavDestination.VOZ)
    assertEquals(com.example.model.NavDestination.VOZ, viewModel.uiState.value.currentDestination)

    // Test Audio Speed
    viewModel.setAudioSpeed(1.5f)
    assertEquals(1.5f, viewModel.uiState.value.audioSpeed, 0.01f)
  }
}
