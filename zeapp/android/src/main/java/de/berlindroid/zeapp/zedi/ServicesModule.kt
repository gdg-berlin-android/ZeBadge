package de.berlindroid.zeapp.zedi

import de.berlindroid.zeapp.zerepositories.ZeSlotRepository
import de.berlindroid.zeapp.zeservices.ZeBadgeConfigParser
import de.berlindroid.zeapp.zeservices.ZeBadgeManager
import de.berlindroid.zeapp.zeservices.ZeClipboardService
import de.berlindroid.zeapp.zeservices.ZeImageProviderService
import de.berlindroid.zeapp.zeservices.ZePreferencesService
import de.berlindroid.zeapp.zeservices.ZeWeatherService
import de.berlindroid.zeapp.zeservices.github.ZeContributorsService
import de.berlindroid.zeapp.zeservices.github.ZeReleaseService
import de.berlindroid.zeapp.zevm.GetTemplateConfigurations
import org.koin.dsl.module

val servicesModule = module {
    single {
        ZeImageProviderService(context = get())
    }
    single {
        ZeBadgeConfigParser()
    }
    single {
        ZeBadgeManager(
            context = get(),
            badgeConfigParser = get(),
        )
    }
    single {
        ZeClipboardService(
            context = get(),
        )
    }
    single {
        ZeWeatherService(
            zeWeatherApi = get(),
        )
    }
    single {
        ZePreferencesService(context = get())
    }
    single {
        ZeSlotRepository(
            zePreferencesService = get(),
            imageProviderService = get(),
        )
    }
    single {
        ZeContributorsService(
            githubApi = get(),
        )
    }
    single {
        ZeReleaseService(githubApi = get())
    }
    single {
        GetTemplateConfigurations(
            imageProviderService = get(),
        )
    }
}
