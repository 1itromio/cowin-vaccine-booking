package dev.romio.cowinvaccinebook.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.romio.cowinvaccinebook.constant.AppConstant
import dev.romio.cowinvaccinebook.data.preference.AppPreference
import dev.romio.cowinvaccinebook.util.HeaderInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {
    @Singleton
    @Provides
    fun providesRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(AppConstant.COWIN_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun providesMoshi() = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Singleton
    @Provides
    fun providesOkHttpClient(appPreference: AppPreference,
                             @ApplicationContext context: Context,
                             chuckerInterceptor: ChuckerInterceptor) = OkHttpClient().newBuilder()
        .addInterceptor(HeaderInterceptor(appPreference))
        .addInterceptor(chuckerInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
            val response: Response = chain.proceed(request)
            val url = request.url.toString()
            if(url.contains("api/v2/admin/location/states") || url.contains("api/v2/admin/location/districts/")) {
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=86400")
                    .build()
            } else {
                response
            }
        }
        .cache(Cache(
            directory = File(context.cacheDir, "http_cache"),
            maxSize = 5L * 1024L * 1024L // 5 MiB
        ))
        .build()

    @Singleton
    @Provides
    fun providesChuckerInterceptor(@ApplicationContext context: Context): ChuckerInterceptor {
        val chuckerCollector = ChuckerCollector(
            context = context,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.ONE_HOUR
        )
        return ChuckerInterceptor.Builder(context)
            .collector(chuckerCollector)
            .maxContentLength(250_000L)
            .alwaysReadResponseBody(true)
            .build()
    }
}