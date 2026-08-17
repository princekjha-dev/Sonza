cmake_minimum_required(VERSION 3.22.1)

project("sonza_native")

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(sonza_native SHARED
        spatial_audio.cpp
        ai_audio_processor.cpp
        limiter.cpp
        file_mapper.cpp
        recommendation_scorer.cpp
        secure_config.cpp)

find_library(log-lib log)

target_link_libraries(sonza_native
        ${log-lib}
        android)

# Enable 16 KB page size support for Android 15+ and Android 16
target_link_options(sonza_native PRIVATE "-Wl,-z,max-page-size=16384")
