package rs.moma.therminator.di

import rs.moma.therminator.data.remote.HttpClientManager
import rs.moma.therminator.viewmodels.AlarmListViewModel
import rs.moma.therminator.viewmodels.DisplayViewModel
import rs.moma.therminator.viewmodels.CameraViewModel
import rs.moma.therminator.viewmodels.AlarmViewModel
import rs.moma.therminator.viewmodels.FilesViewModel
import rs.moma.therminator.viewmodels.MainViewModel
import rs.moma.therminator.data.remote.RestApi
import org.koin.dsl.module

val sharedModule = module {
    single { MainViewModel() }
    single { CameraViewModel() }
    single { FilesViewModel() }
    single { AlarmListViewModel() }
    single { AlarmViewModel() }
    single { DisplayViewModel() }
    single { HttpClientManager() }
    single { get<HttpClientManager>().getHttpClient() }
    single { RestApi(get()) }
}