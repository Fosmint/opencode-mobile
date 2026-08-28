package dev.opencode.mobile.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Trivial factory that wraps a lambda producing a ViewModel instance. */
class LambdaViewModelFactory<T : ViewModel>(private val create: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
}
