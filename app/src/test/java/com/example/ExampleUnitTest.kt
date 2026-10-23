package com.example

import com.example.hardware.ComputeAccelerationManager
import com.example.hardware.ComputeMode
import com.example.hardware.WorkloadType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun computeAccelerationManager_modeSwitchAndExecution() = runBlocking {
    ComputeAccelerationManager.setComputeMode(ComputeMode.AUTO_DYNAMIC)
    assertEquals(ComputeMode.AUTO_DYNAMIC, ComputeAccelerationManager.telemetry.value.mode)

    // Verify workload execution dispatches properly
    val result = ComputeAccelerationManager.executeWorkload(
      workloadType = WorkloadType.HEAVY_VISUAL,
      cpuTask = { "cpu" },
      gpuTask = { "gpu" }
    )
    assertEquals("gpu", result)

    // In CPU efficiency mode, tasks should run on CPU
    ComputeAccelerationManager.setComputeMode(ComputeMode.CPU_EFFICIENCY)
    val cpuResult = ComputeAccelerationManager.executeWorkload(
      workloadType = WorkloadType.AI_ORCHESTRATION,
      cpuTask = { "cpu_dispatched" },
      gpuTask = { "gpu_dispatched" }
    )
    assertEquals("cpu_dispatched", cpuResult)

    assertTrue(ComputeAccelerationManager.telemetry.value.cpuCores >= 1)
  }
}
