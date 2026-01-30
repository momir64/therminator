package rs.moma.therminator.di

import rs.moma.therminator.data.local.AndroidSecureStore
import rs.moma.therminator.data.local.SecureStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import rs.moma.therminator.ui.utils.AndroidToastService
import rs.moma.therminator.ui.utils.ToastService

val androidModule = module {
    single<SecureStore> { AndroidSecureStore(androidContext()) }
    single<ToastService> { AndroidToastService(androidContext()) }
}