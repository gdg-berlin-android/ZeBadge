package de.berlindroid.zeapp.zedi

import de.berlindroid.zeapp.zealteregos.vm.AlterEgosVm
import de.berlindroid.zeapp.zeui.zeabout.ZeAboutViewModel
import de.berlindroid.zeapp.zeui.zehome.ZeDrawerViewModel
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import de.berlindroid.zeapp.zevm.ZePassVm
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::ZeBadgeViewModel)
    viewModelOf(::AlterEgosVm)
    viewModelOf(::ZeAboutViewModel)
    viewModelOf(::ZeDrawerViewModel)
    viewModelOf(::ZePassVm)
}
