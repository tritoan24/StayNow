import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id ("kotlin-parcelize")
    kotlin("kapt")
}



android {
    namespace = "com.ph32395.staynow_datn"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.ph32395.staynow_datn"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Thêm các thư viện Firebase
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation ("com.google.firebase:firebase-firestore")
    implementation (platform("com.google.firebase:firebase-bom:32.0.0")) // Hoặc phiên bản mới nhất
    implementation ("com.google.firebase:firebase-dynamic-links")

    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("io.github.maitrungduc1410:AVLoadingIndicatorView:2.1.4")
    implementation("com.airbnb.android:lottie:5.2.0")
    implementation ("com.github.denzcoskun:ImageSlideshow:0.1.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation ("com.google.firebase:firebase-storage:21.0.1")
    //
    //lấy ảnh từ thư viện và camera ImagePicker
    implementation ("com.github.Dhaval2404:imagepicker:2.1")
    // Thư viện xử lý ảnh
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
//Thư viện format tgian tạo phòng trọ
    implementation ("org.ocpsoft.prettytime:prettytime:5.0.2.Final")
//
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
//    thu vien zoom to anh
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    //calendar
    implementation(project(":horizontal_calendar"))


    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    //Thư Viện SweetAlertDialog
    implementation ("com.github.f0ris.sweetalert:library:1.6.2")

    implementation("io.github.ParkSangGwon:tedimagepicker:1.6.1")

    implementation ("com.google.android.gms:play-services-location:21.3.0")
    //Thư viện Places API
    implementation ("com.google.android.libraries.places:places:4.1.0")
    //Thu vien khoang gia
    implementation ("com.github.Jay-Goo:RangeSeekBar:3.0.0")

    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging:24.1.0")
    // Thư viện Volley
    implementation("com.android.volley:volley:1.2.1")

    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("androidx.camera:camera-core:1.2.2")
    implementation("androidx.camera:camera-camera2:1.2.2")
    implementation("androidx.camera:camera-lifecycle:1.2.2")
    implementation("androidx.camera:camera-view:1.2.2")

    implementation("com.itextpdf:itext7-core:7.2.3")
    implementation("com.afollestad.material-dialogs:core:3.3.0")

    //thư viện custom text
    implementation("jp.wasabeef:richeditor-android:2.0.0")

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Để tự động chuyển JSON thành object
    // Socket
    implementation ("io.socket:socket.io-client:2.0.0")
    implementation("androidx.room:room-runtime:2.4.3")
    kapt("androidx.room:room-compiler:2.4.3")
    implementation("androidx.room:room-ktx:2.4.3")

    // zalo sdk

    implementation(files("libs/zpdk-release-v3.1.aar"))

    implementation ("androidx.appcompat:appcompat:1.7.0")

    //generate qr
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")

    //progressdialog
    implementation("com.github.techinessoverloaded:progress-dialog:1.5.1")

    implementation ("com.github.aabhasr1:OtpView:v1.1.2-ktx")




    implementation ("com.aallam.openai:openai-client:3.3.0")
    implementation ("io.ktor:ktor-client-android:2.3.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    implementation ("com.google.dagger:hilt-android:2.45")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0")

    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
}
