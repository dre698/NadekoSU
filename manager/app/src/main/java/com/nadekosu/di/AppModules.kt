package com.nadekosu.di

import coil.ImageLoader
import com.nadekosu.BuildConfig
import com.nadekosu.data.AppSettingsRepository
import com.nadekosu.data.application.ApplicationControlRepository
import com.nadekosu.data.application.DynamicManagerRepository
import com.nadekosu.data.download.DownloadRepository
import com.nadekosu.data.file.ModuleFileRepository
import com.nadekosu.data.flash.FlashRepository
import com.nadekosu.data.kernel.KernelRepository
import com.nadekosu.data.kernel.UmountRepository
import com.nadekosu.data.logging.BugreportRepository
import com.nadekosu.data.logging.SulogRepository
import com.nadekosu.data.module.ModuleActionRepository
import com.nadekosu.data.module.ModuleCatalogRepository
import com.nadekosu.data.module.ModulePreferencesRepository
import com.nadekosu.data.module.ModuleRepository
import com.nadekosu.data.network.NetworkRequestRepository
import com.nadekosu.data.network.NetworkStatusRepository
import com.nadekosu.data.network.WebResourceRepository
import com.nadekosu.data.packageinfo.AppIconDataSource
import com.nadekosu.data.packageinfo.InstalledPackageCache
import com.nadekosu.data.packageinfo.InstalledPackageRepository
import com.nadekosu.data.packageinfo.RootServiceRepository
import com.nadekosu.data.packageinfo.SuperUserRepository
import com.nadekosu.data.profile.ProfileRepository
import com.nadekosu.data.profile.ProfileTemplateRepository
import com.nadekosu.data.settings.LocaleHelper
import com.nadekosu.data.settings.LocaleRepository
import com.nadekosu.data.settings.SettingsPlatformRepository
import com.nadekosu.data.shell.KsuCliRepository
import com.nadekosu.data.shell.ShortcutRepository
import com.nadekosu.data.startup.ApplicationInitializationRepository
import com.nadekosu.data.startup.StartupRepository
import com.nadekosu.data.susfs.SuSFSConfigHelper
import com.nadekosu.data.susfs.SuSFSRepository
import com.nadekosu.data.system.HomeRuntimeRepository
import com.nadekosu.data.system.HomeStateRepository
import com.nadekosu.data.text.HanziToPinyin
import com.nadekosu.data.theme.MonetCompatColorSource
import com.nadekosu.data.theme.ThemeRepository
import com.nadekosu.data.update.ManagerUpdateRepository
import com.nadekosu.data.webui.WebUiRepository
import com.nadekosu.domain.text.TextTransliterator
import com.nadekosu.domain.usecase.AddUmountPathUseCase
import com.nadekosu.domain.usecase.ApplyLanguageUseCase
import com.nadekosu.domain.usecase.BackupAllowlistUseCase
import com.nadekosu.domain.usecase.CalculateInstalledModuleSizeUseCase
import com.nadekosu.domain.usecase.CheckFlashModuleMountUseCase
import com.nadekosu.domain.usecase.CheckManagerUpdateUseCase
import com.nadekosu.domain.usecase.CleanSulogUseCase
import com.nadekosu.domain.usecase.ClearDynamicManagerUseCase
import com.nadekosu.domain.usecase.ConfigureSuLogUseCase
import com.nadekosu.domain.usecase.ControlAppUseCase
import com.nadekosu.domain.usecase.DeleteProfileTemplateUseCase
import com.nadekosu.domain.usecase.EnableSulogUseCase
import com.nadekosu.domain.usecase.EnqueueDownloadUseCase
import com.nadekosu.domain.usecase.EnqueueManagerUpdateUseCase
import com.nadekosu.domain.usecase.EnsureManagerInstalledUseCase
import com.nadekosu.domain.usecase.ExecuteFlashOperationUseCase
import com.nadekosu.domain.usecase.ExecuteModuleActionUseCase
import com.nadekosu.domain.usecase.ExportProfileTemplatesUseCase
import com.nadekosu.domain.usecase.ExtractModuleIdUseCase
import com.nadekosu.domain.usecase.ExtractModuleNameUseCase
import com.nadekosu.domain.usecase.FetchRemoteTextUseCase
import com.nadekosu.domain.usecase.GenerateBugreportUseCase
import com.nadekosu.domain.usecase.GetAppProfileUseCase
import com.nadekosu.domain.usecase.GetAppSepolicyUseCase
import com.nadekosu.domain.usecase.GetBooleanPreferenceUseCase
import com.nadekosu.domain.usecase.GetCatalogModuleUseCase
import com.nadekosu.domain.usecase.GetDefaultUmountModulesUseCase
import com.nadekosu.domain.usecase.GetHomeBasicInfoUseCase
import com.nadekosu.domain.usecase.GetHomeModuleOverviewUseCase
import com.nadekosu.domain.usecase.GetHomeSuperuserCountUseCase
import com.nadekosu.domain.usecase.GetInstallEnvironmentUseCase
import com.nadekosu.domain.usecase.GetKernelFeatureSettingsUseCase
import com.nadekosu.domain.usecase.GetKernelStatusUseCase
import com.nadekosu.domain.usecase.GetManagerRuntimeInfoUseCase
import com.nadekosu.domain.usecase.GetPlatformFeatureStatusUseCase
import com.nadekosu.domain.usecase.GetProfileTemplateUseCase
import com.nadekosu.domain.usecase.GetStringPreferenceUseCase
import com.nadekosu.domain.usecase.GetStringSetPreferenceUseCase
import com.nadekosu.domain.usecase.GetSuSFSStatusUseCase
import com.nadekosu.domain.usecase.GetSuperUserAppGroupUseCase
import com.nadekosu.domain.usecase.ImportAllowlistUseCase
import com.nadekosu.domain.usecase.ImportProfileTemplatesUseCase
import com.nadekosu.domain.usecase.InitializeApplicationUseCase
import com.nadekosu.domain.usecase.IsLateLoadModeUseCase
import com.nadekosu.domain.usecase.IsModuleUriAccessibleUseCase
import com.nadekosu.domain.usecase.IsNetworkAvailableUseCase
import com.nadekosu.domain.usecase.IsSystemLanguageSettingsUseCase
import com.nadekosu.domain.usecase.LaunchSystemLanguageSettingsUseCase
import com.nadekosu.domain.usecase.LoadSettingsPlatformUseCase
import com.nadekosu.domain.usecase.ObserveCatalogModulesUseCase
import com.nadekosu.domain.usecase.ObserveDownloadUseCase
import com.nadekosu.domain.usecase.ObserveDynamicManagerStateUseCase
import com.nadekosu.domain.usecase.ObserveInstalledModulesUseCase
import com.nadekosu.domain.usecase.ObserveKernelFlashUseCase
import com.nadekosu.domain.usecase.ObserveModuleCatalogOfflineUseCase
import com.nadekosu.domain.usecase.ObserveModuleCatalogRefreshingUseCase
import com.nadekosu.domain.usecase.ObserveProfileTemplateOfflineUseCase
import com.nadekosu.domain.usecase.ObserveProfileTemplateRefreshingUseCase
import com.nadekosu.domain.usecase.ObserveProfileTemplatesUseCase
import com.nadekosu.domain.usecase.ObserveStartupStateUseCase
import com.nadekosu.domain.usecase.ObserveSulogStateUseCase
import com.nadekosu.domain.usecase.ObserveSuperUserStateUseCase
import com.nadekosu.domain.usecase.ObserveUmountStateUseCase
import com.nadekosu.domain.usecase.RebootUseCase
import com.nadekosu.domain.usecase.RefreshDynamicManagerUseCase
import com.nadekosu.domain.usecase.RefreshInstalledModulesUseCase
import com.nadekosu.domain.usecase.RefreshModuleCatalogUseCase
import com.nadekosu.domain.usecase.RefreshProfileTemplatesUseCase
import com.nadekosu.domain.usecase.RefreshSulogUseCase
import com.nadekosu.domain.usecase.RefreshSuperUsersUseCase
import com.nadekosu.domain.usecase.RefreshUmountPathsUseCase
import com.nadekosu.domain.usecase.RemovePreferenceUseCase
import com.nadekosu.domain.usecase.RemoveUmountPathUseCase
import com.nadekosu.domain.usecase.SaveModuleActionLogUseCase
import com.nadekosu.domain.usecase.SaveProfileTemplateUseCase
import com.nadekosu.domain.usecase.SelectDynamicManagerUseCase
import com.nadekosu.domain.usecase.SetAppProfileUseCase
import com.nadekosu.domain.usecase.SetAppSepolicyUseCase
import com.nadekosu.domain.usecase.SetBooleanPreferenceUseCase
import com.nadekosu.domain.usecase.SetDefaultUmountModulesUseCase
import com.nadekosu.domain.usecase.SetKernelUmountEnabledUseCase
import com.nadekosu.domain.usecase.SetManualDynamicManagerUseCase
import com.nadekosu.domain.usecase.SetModuleEnabledUseCase
import com.nadekosu.domain.usecase.SetModuleRemovedUseCase
import com.nadekosu.domain.usecase.SetSelinuxHideEnabledUseCase
import com.nadekosu.domain.usecase.SetStringPreferenceUseCase
import com.nadekosu.domain.usecase.SetStringSetPreferenceUseCase
import com.nadekosu.domain.usecase.SetSuEnabledUseCase
import com.nadekosu.domain.usecase.StartKernelFlashUseCase
import com.nadekosu.domain.usecase.SuSFSConfigUseCase
import com.nadekosu.domain.usecase.TakeModuleUriPermissionUseCase
import com.nadekosu.domain.usecase.TransliterateTextUseCase
import com.nadekosu.domain.usecase.UpdateAppearanceUseCase
import com.nadekosu.domain.usecase.UpdateCachedModuleEnabledUseCase
import com.nadekosu.domain.usecase.UpdatePlatformSettingUseCase
import com.nadekosu.domain.usecase.ValidateSepolicyUseCase
import com.nadekosu.ui.activity.util.ThemeUtils
import com.nadekosu.ui.component.ZipFileDetector
import com.nadekosu.ui.theme.BackgroundManager
import com.nadekosu.ui.theme.CardConfig
import com.nadekosu.ui.theme.ThemeConfig
import com.nadekosu.ui.util.module.Shortcut
import com.nadekosu.ui.viewmodel.AppProfileViewModel
import com.nadekosu.ui.viewmodel.DynamicManagerViewModel
import com.nadekosu.ui.viewmodel.ExecuteModuleActionViewModel
import com.nadekosu.ui.viewmodel.FlashViewModel
import com.nadekosu.ui.viewmodel.HomeViewModel
import com.nadekosu.ui.viewmodel.InstallViewModel
import com.nadekosu.ui.viewmodel.KernelFlashViewModel
import com.nadekosu.ui.viewmodel.MainIntentViewModel
import com.nadekosu.ui.viewmodel.ModuleDetailViewModel
import com.nadekosu.ui.viewmodel.ModuleRepoViewModel
import com.nadekosu.ui.viewmodel.ModuleViewModel
import com.nadekosu.ui.viewmodel.SettingsViewModel
import com.nadekosu.ui.viewmodel.SuSFSViewModel
import com.nadekosu.ui.viewmodel.SulogViewModel
import com.nadekosu.ui.viewmodel.SuperUserViewModel
import com.nadekosu.ui.viewmodel.TemplateEditorViewModel
import com.nadekosu.ui.viewmodel.TemplateViewModel
import com.nadekosu.ui.viewmodel.UmountManagerScreenViewModel
import com.nadekosu.ui.webui.MonetColorsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.zhanghai.android.appiconloader.coil.AppIconFetcher
import me.zhanghai.android.appiconloader.coil.AppIconKeyer
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

val applicationScopeQualifier = named("applicationScope")

val coreModule = module {
    single<CoroutineScope>(applicationScopeQualifier) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
    single {
        OkHttpClient.Builder()
            .cache(Cache(File(androidApplication().cacheDir, "okhttp"), 10L * 1024L * 1024L))
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "NadekoSU/${BuildConfig.VERSION_CODE}")
                        .header("Accept-Language", Locale.getDefault().toLanguageTag())
                        .build()
                )
            }
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }
    single {
        val application = androidApplication()
        val iconSize = application.resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
        ImageLoader.Builder(application)
            .components {
                add(AppIconKeyer())
                add(AppIconFetcher.Factory(iconSize, false, application))
            }
            .build()
    }
}

val repositoryModule = module {
    single { KsuCliRepository(androidApplication()) }
    singleOf(::InstalledPackageCache)
    singleOf(::AppIconDataSource)
    singleOf(::RootServiceRepository)
    singleOf(::InstalledPackageRepository)
    single {
        SuperUserRepository(
            application = get(),
            cache = get(),
            installedPackageRepository = get(),
            profileRepository = get(),
            applicationScope = get(applicationScopeQualifier),
        )
    }
    single {
        AppSettingsRepository(
            context = androidApplication(),
            applicationScope = get(applicationScopeQualifier),
        )
    }
    singleOf(::StartupRepository)
    single {
        ApplicationInitializationRepository(
            application = get(),
            imageLoader = get(),
            applicationScope = get(applicationScopeQualifier),
            flashRepository = get(),
            ksuCliRepository = get(),
            monetCompatColorSource = get(),
        )
    }
    singleOf(::ManagerUpdateRepository)
    singleOf(::ApplicationControlRepository)
    singleOf(::DownloadRepository)
    single { FlashRepository(get(), get(applicationScopeQualifier), get(), get()) }
    singleOf(::KernelRepository)
    singleOf(::HomeRuntimeRepository)
    singleOf(::HomeStateRepository)
    singleOf(::NetworkStatusRepository)
    singleOf(::NetworkRequestRepository)
    singleOf(::DynamicManagerRepository)
    singleOf(::SulogRepository)
    singleOf(::BugreportRepository)
    singleOf(::UmountRepository)
    singleOf(::ModuleCatalogRepository)
    singleOf(::ModuleRepository)
    singleOf(::ModulePreferencesRepository)
    singleOf(::ModuleActionRepository)
    singleOf(::WebResourceRepository)
    singleOf(::WebUiRepository)
    singleOf(::ModuleFileRepository)
    singleOf(::ProfileRepository)
    singleOf(::ProfileTemplateRepository)
    singleOf(::SuSFSConfigHelper)
    singleOf(::SuSFSRepository)
    singleOf(::MonetCompatColorSource)
    singleOf(::ThemeRepository)
    single {
        val themeRepository = get<ThemeRepository>()
        ThemeConfig(themeRepository::defaultSeedColor)
    }
    singleOf(::CardConfig)
    singleOf(::BackgroundManager)
    singleOf(::ThemeUtils)
    singleOf(::LocaleHelper)
    singleOf(::LocaleRepository)
    singleOf(::SettingsPlatformRepository)
    singleOf(::ShortcutRepository)
    singleOf(::Shortcut)
    singleOf(::MonetColorsProvider)
    singleOf(::ZipFileDetector)
    single { HanziToPinyin.create() } bind TextTransliterator::class
}

val useCaseModule = module {
    factoryOf(::InitializeApplicationUseCase)
    factoryOf(::GetHomeBasicInfoUseCase)
    factoryOf(::GetHomeModuleOverviewUseCase)
    factoryOf(::GetHomeSuperuserCountUseCase)
    factoryOf(::IsNetworkAvailableUseCase)
    factoryOf(::LoadSettingsPlatformUseCase)
    factoryOf(::UpdateAppearanceUseCase)
    factoryOf(::UpdatePlatformSettingUseCase)
    factoryOf(::GetPlatformFeatureStatusUseCase)
    factoryOf(::CheckManagerUpdateUseCase)
    factoryOf(::EnsureManagerInstalledUseCase)
    factoryOf(::RebootUseCase)
    factoryOf(::EnqueueDownloadUseCase)
    factoryOf(::EnqueueManagerUpdateUseCase)
    factoryOf(::ObserveDownloadUseCase)
    factoryOf(::GetKernelStatusUseCase)
    factoryOf(::GetInstallEnvironmentUseCase)
    factoryOf(::ExecuteFlashOperationUseCase)
    factoryOf(::CheckFlashModuleMountUseCase)
    factoryOf(::GetManagerRuntimeInfoUseCase)
    factoryOf(::GetKernelFeatureSettingsUseCase)
    factoryOf(::SetSuEnabledUseCase)
    factoryOf(::SetKernelUmountEnabledUseCase)
    factoryOf(::ConfigureSuLogUseCase)
    factoryOf(::SetSelinuxHideEnabledUseCase)
    factoryOf(::SetDefaultUmountModulesUseCase)
    factoryOf(::IsLateLoadModeUseCase)
    factoryOf(::GetAppProfileUseCase)
    factoryOf(::SetAppProfileUseCase)
    factoryOf(::GetAppSepolicyUseCase)
    factoryOf(::SetAppSepolicyUseCase)
    factoryOf(::ControlAppUseCase)
    factoryOf(::ValidateSepolicyUseCase)
    factoryOf(::GetDefaultUmountModulesUseCase)
    factoryOf(::GetSuSFSStatusUseCase)
    factoryOf(::SuSFSConfigUseCase)
    factoryOf(::ApplyLanguageUseCase)
    factoryOf(::IsSystemLanguageSettingsUseCase)
    factoryOf(::LaunchSystemLanguageSettingsUseCase)
    factoryOf(::GenerateBugreportUseCase)
    factoryOf(::ObserveStartupStateUseCase)
    factoryOf(::GetSuperUserAppGroupUseCase)
    factoryOf(::ObserveCatalogModulesUseCase)
    factoryOf(::ObserveModuleCatalogRefreshingUseCase)
    factoryOf(::ObserveModuleCatalogOfflineUseCase)
    factoryOf(::RefreshModuleCatalogUseCase)
    factoryOf(::GetCatalogModuleUseCase)
    factoryOf(::ObserveProfileTemplatesUseCase)
    factoryOf(::ObserveProfileTemplateRefreshingUseCase)
    factoryOf(::ObserveProfileTemplateOfflineUseCase)
    factoryOf(::RefreshProfileTemplatesUseCase)
    factoryOf(::GetProfileTemplateUseCase)
    factoryOf(::SaveProfileTemplateUseCase)
    factoryOf(::DeleteProfileTemplateUseCase)
    factoryOf(::ImportProfileTemplatesUseCase)
    factoryOf(::ExportProfileTemplatesUseCase)
    factoryOf(::GetBooleanPreferenceUseCase)
    factoryOf(::SetBooleanPreferenceUseCase)
    factoryOf(::GetStringPreferenceUseCase)
    factoryOf(::SetStringPreferenceUseCase)
    factoryOf(::GetStringSetPreferenceUseCase)
    factoryOf(::SetStringSetPreferenceUseCase)
    factoryOf(::ObserveDynamicManagerStateUseCase)
    factoryOf(::RefreshDynamicManagerUseCase)
    factoryOf(::SelectDynamicManagerUseCase)
    factoryOf(::SetManualDynamicManagerUseCase)
    factoryOf(::ClearDynamicManagerUseCase)
    factoryOf(::ObserveSulogStateUseCase)
    factoryOf(::RefreshSulogUseCase)
    factoryOf(::EnableSulogUseCase)
    factoryOf(::CleanSulogUseCase)
    factoryOf(::ObserveUmountStateUseCase)
    factoryOf(::RefreshUmountPathsUseCase)
    factoryOf(::AddUmountPathUseCase)
    factoryOf(::RemoveUmountPathUseCase)
    factoryOf(::ObserveKernelFlashUseCase)
    factoryOf(::StartKernelFlashUseCase)
    factoryOf(::RemovePreferenceUseCase)
    factoryOf(::ObserveSuperUserStateUseCase)
    factoryOf(::RefreshSuperUsersUseCase)
    factoryOf(::BackupAllowlistUseCase)
    factoryOf(::ImportAllowlistUseCase)
    factoryOf(::FetchRemoteTextUseCase)
    factoryOf(::IsModuleUriAccessibleUseCase)
    factoryOf(::TakeModuleUriPermissionUseCase)
    factoryOf(::ExtractModuleNameUseCase)
    factoryOf(::ExtractModuleIdUseCase)
    factoryOf(::ObserveInstalledModulesUseCase)
    factoryOf(::RefreshInstalledModulesUseCase)
    factoryOf(::CalculateInstalledModuleSizeUseCase)
    factoryOf(::UpdateCachedModuleEnabledUseCase)
    factoryOf(::ExecuteModuleActionUseCase)
    factoryOf(::SaveModuleActionLogUseCase)
    factoryOf(::SetModuleEnabledUseCase)
    factoryOf(::SetModuleRemovedUseCase)
    factoryOf(::TransliterateTextUseCase)
}

val viewModelModule = module {
    viewModel { parameters ->
        AppProfileViewModel(
            uid = parameters[0],
            packageName = parameters[1],
            getAppGroup = get(),
            getProfile = get(),
            getDefaultUmountModules = get(),
            setProfile = get(),
            getSepolicy = get(),
            setSepolicy = get(),
            controlApp = get(),
            validateSepolicy = get(),
        )
    }
    viewModelOf(::HomeViewModel)
    viewModelOf(::InstallViewModel)
    viewModelOf(::MainIntentViewModel)
    viewModelOf(::KernelFlashViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ModuleViewModel)
    viewModelOf(::SuperUserViewModel)
    viewModelOf(::SuSFSViewModel)
    viewModelOf(::ModuleRepoViewModel)
    viewModel { parameters -> ModuleDetailViewModel(parameters[0], get()) }
    viewModelOf(::TemplateViewModel)
    viewModel { parameters ->
        TemplateEditorViewModel(
            templateId = parameters[0],
            readOnly = parameters[1],
            isCreation = parameters[2],
            getTemplate = get(),
            saveTemplate = get(),
            deleteTemplate = get(),
        )
    }
    viewModelOf(::SulogViewModel)
    viewModelOf(::DynamicManagerViewModel)
    viewModelOf(::FlashViewModel)
    viewModelOf(::UmountManagerScreenViewModel)
    viewModel { parameters ->
        ExecuteModuleActionViewModel(
            moduleId = parameters[0],
            executeModuleAction = get(),
            saveModuleActionLog = get(),
        )
    }
}

val appModules = listOf(coreModule, repositoryModule, useCaseModule, viewModelModule)
