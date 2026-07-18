package com.soulhowling.solvelog_jetpackcompose.utils.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Guard component that controls touch input in sync with the screen lifecycle.
 * When isTargetScreen is true (the screen that is about to be displayed),
 * touch input is enabled from the STARTED state. When false (the screen moving to the background),
 * touch input is allowed only in the RESUMED state, effectively blocking input during the transition.
 */
@Composable
fun LifecycleTouchGuard(
    isTargetScreen: Boolean = true,
    content: @Composable () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentState by remember { mutableStateOf(lifecycleOwner.lifecycle.currentState) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            currentState = event.targetState
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val canTouch = if (isTargetScreen) {
        currentState.isAtLeast(Lifecycle.State.STARTED)
    } else {
        currentState == Lifecycle.State.RESUMED
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (!canTouch) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
            )
        }
    }
}
