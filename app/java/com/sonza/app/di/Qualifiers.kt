package com.sonza.app.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlayerDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DownloadDataSource

/** OkHttp client carrying the HQ Audio route interceptor. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HqAudioClient
