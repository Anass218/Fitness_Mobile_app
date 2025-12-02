    package com.example.fitness_kzo

    import com.google.gson.GsonBuilder
    import okhttp3.OkHttpClient
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory
    import java.util.concurrent.TimeUnit

    object RetrofitClient {
        private const val BASE_URL = "http://192.168.100.14/BackendServer_KZO_Mobile/"

        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(70, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        private val gson = GsonBuilder()
            .setLenient()
            .create()

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        val apiService: Api by lazy {
            retrofit.create(Api::class.java)
        }
    }