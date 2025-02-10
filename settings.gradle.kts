pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/gradle-plugin/") }
        maven { url = uri("https://repo.huaweicloud.com/repository/gradle-plugin/") }
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "AndroidWebGet"
include(":app")
 