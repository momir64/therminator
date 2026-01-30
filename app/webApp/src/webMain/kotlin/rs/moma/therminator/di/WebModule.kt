package rs.moma.therminator.di

import rs.moma.therminator.data.local.WebSecureStore
import rs.moma.therminator.ui.utils.WebToastService
import rs.moma.therminator.data.local.SecureStore
import rs.moma.therminator.ui.utils.ToastService
import org.koin.dsl.module

val webModule = module {
    single<SecureStore> { WebSecureStore() }
    single<ToastService> { WebToastService() }
}